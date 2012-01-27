package pegasus.eventbus.rabbitmq;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.GetResponse;

import pegasus.eventbus.amqp.AmqpMessageBus;
import pegasus.eventbus.amqp.RoutingInfo;
import pegasus.eventbus.client.Envelope;

public class RabbitMessageBus implements AmqpMessageBus {

	final static String TOPIC_HEADER_KEY = "pegasus.eventbus.event.topic";
			
	protected ConnectionFactory config;
	private Connection connection;
	private Channel commandChannel;
	
	public RabbitMessageBus(ConnectionFactory conectionFactory) {
		this.config = conectionFactory;
		try {
			this.connection = conectionFactory.newConnection();
		} catch (IOException e) {
			throw new RuntimeException("Failed to open connection to RabbitMq: " + e.getMessage() + "See inner exception for details", e);
		}
		try {
			//TODO: Need to replace this with a channel per thread model.
			this.commandChannel = connection.createChannel();
		} catch (IOException e) {
			throw new RuntimeException("Failed to open AMQP channel: " + e.getMessage() + "See inner exception for details", e);
		}
	}
	
	@Override
	public void createExchange(RoutingInfo.Exchange exchange) {
		try {
			commandChannel.exchangeDeclare(
					exchange.getName(), 
					exchange.getType().toString().toLowerCase(), 
					exchange.isDurable());
		} catch (IOException e) {
			throw new RuntimeException("Failed to create exchange: " + e.getMessage() + "See inner exception for details", e);
		}
	}

	@Override
	public void createQueue(String name, RoutingInfo[] bindings, boolean durable) {
		try {
			commandChannel.queueDeclare(name, durable, false, false, null);
		} catch (IOException e) {
			throw new RuntimeException("Failed to create queue: " + e.getMessage() + "See inner exception for details", e);
		}
		for(RoutingInfo binding : bindings){
			try {
				commandChannel.queueBind(name, binding.getExchange().getName(), binding.getRoutingKey());
			} catch (IOException e) {
				throw new RuntimeException("Failed to create binding: " + binding.getRoutingKey() + " on queue: " + name + " See inner exception for details", e);
			}
		}
	}
	
	@Override
	public void deleteQueue(String queueName) {
		try {
			commandChannel.queueDelete(queueName);
		} catch (IOException e) {
			throw new RuntimeException("Failed to delete queue: " + e.getMessage() + "See inner exception for details", e);
		}
	}

	@Override
	public void publish(RoutingInfo route, Envelope message) {
		try {
			Map<String, Object> headersOut = new HashMap<String,Object>();
			
			if(message.getTopic() != null){
				headersOut.put(TOPIC_HEADER_KEY, message.getTopic());
			}
			
			final Map<String, String> headersIn = message.getHeaders();
			for(String key : headersIn.keySet()){
				headersOut.put(key, headersIn.get(key));
			}
			
			BasicProperties props = new BasicProperties.Builder()
				.messageId(message.getId() == null ? null : message.getId().toString())
				.correlationId(message.getCorrelationId() == null ? null : message.getCorrelationId().toString())
				.type(message.getEventType())
				.replyTo(message.getReplyTo())
				.headers(headersOut)
				.build();
			
			commandChannel.basicPublish(route.getExchange().getName(), route.getRoutingKey(), props, message.getBody());
		} catch (IOException e) {
			throw new RuntimeException("Failed to publish message: " + e.getMessage() + "See inner exception for details", e);
		}

	}

	@Override
	public UnacceptedMessage getNextMessageFrom(String queueName) {
		try {
			GetResponse receivedMessage = commandChannel.basicGet(queueName, false);
			
			if(receivedMessage == null)	return null;
			
			final BasicProperties props = receivedMessage.getProps();

			Envelope envelope = new Envelope();
			
			Map<String,String>headers = envelope.getHeaders();
			if(props.getHeaders() != null){
				for(String key : props.getHeaders().keySet()){
					headers.put(key, props.getHeaders().get(key).toString());
				}
			}
			
			envelope.setBody(receivedMessage.getBody());
			envelope.setId(props.getMessageId() == null ? null : UUID.fromString(props.getMessageId()));
			envelope.setCorrelationId(props.getCorrelationId() == null ? null :UUID.fromString(props.getCorrelationId()));
			envelope.setEventType(props.getType());
			envelope.setReplyTo(props.getReplyTo());
			envelope.setTopic(headers.get(TOPIC_HEADER_KEY));
			
			headers.remove(TOPIC_HEADER_KEY);
			
			return new UnacceptedMessage(envelope, receivedMessage.getEnvelope().getDeliveryTag());
		} catch (IOException e) {
			throw new RuntimeException("Failed to get message from queue: " + queueName + " Error: " + e.getMessage() + "See inner exception for details", e);
		}
	}
	
	@Override
	public void acceptMessage(UnacceptedMessage message) {
		try {
			commandChannel.basicAck(message.getAcceptanceToken(), false);
		} catch (IOException e) {
			throw new RuntimeException("Failed to get acknowledge message: " + e.getMessage() + "See inner exception for details", e);
		}
	}
	
	@Override
	public void rejectMessage(UnacceptedMessage message, boolean redeliverMessageLater) {
		try {
			commandChannel.basicReject(message.getAcceptanceToken(), redeliverMessageLater);
		} catch (IOException e) {
			throw new RuntimeException("Failed to get acknowledge message: " + e.getMessage() + "See inner exception for details", e);
		}
	}

	public void close(){
		try {
			if(commandChannel.isOpen())
				commandChannel.close();
			
			if(connection.isOpen())
				connection.close();
			
		} catch (IOException e) {}
	}
}
