package pegasus.eventbus.rabbitmq;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pegasus.eventbus.amqp.AmqpMessageBus;
import pegasus.eventbus.amqp.ConnectionParameters;
import pegasus.eventbus.amqp.RoutingInfo;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;

import pegasus.eventbus.client.Envelope;
import pegasus.eventbus.client.EnvelopeHandler;

/**
 * RabbitMQ implementation of our AmqpMessageBus interface.
 * 
 * @author Ken Baltrinic (Berico Technologies)
 */
public class RabbitMessageBus implements AmqpMessageBus, ShutdownListener {

    private static final Logger    LOG              = LoggerFactory.getLogger(RabbitMessageBus.class);

    final static String            TOPIC_HEADER_KEY = "pegasus.eventbus.event.topic";

    protected final ConnectionParameters config;
    private final ConnectionFactory connectionFactory;
    private Connection             connection;
    private Channel                commandChannel;
    private Map<String,Channel>    consumerChannels = new HashMap<String,Channel>();

    private volatile boolean isClosing;
    private volatile boolean isInConnectionErrorState;
    /**
     * Initialize Rabbit with the given connection parameters,
     * 
     * @param connectionParameters
     *            Connection Parameters
     */
    public RabbitMessageBus(ConnectionParameters connectionParameters) {
        this.config = connectionParameters;

        LOG.trace("Building the RabbitMQ Connection Factory.");

        connectionFactory = new ConnectionFactory();
        connectionFactory.setUsername(config.getUsername());
        connectionFactory.setPassword(config.getPassword());
        connectionFactory.setVirtualHost(config.getVirtualHost());
        connectionFactory.setHost(config.getHost());
        connectionFactory.setPort(config.getPort());
    }
	
	@Override
    public void start() {

    	isClosing = false;
    	
        LOG.trace("Starting the RabbitMessageBus");

        openConnectionToBroker();

        isInConnectionErrorState = false;
       
    }

	private void openConnectionToBroker() {
		try {

            LOG.trace("Grabbing the connection instance from the factory.");

            this.connection = connectionFactory.newConnection();
            
            LOG.trace("Adding ShutdownListener to connection.");

            this.connection.addShutdownListener(this);
                        
        } catch (IOException e) {

            LOG.error("Could not connect to RabbitMQ", e);

            throw new RuntimeException("Failed to open connection to RabbitMq: " + e.getMessage() + "See inner exception for details", e);
        }

		try {

            LOG.debug("Creating channel to AMQP broker.");

            // TODO: Need to replace this with a channel per thread model.
            this.commandChannel = connection.createChannel();

        } catch (IOException e) {

            LOG.error("Could not open an AMQP channel.", e);

            throw new RuntimeException("Failed to open AMQP channel: " + e.getMessage() + "See inner exception for details", e);
        }
	 }

    /**
     * Close the active AMQP connection.
     */
    public void close() {

    	isClosing = true;
    	
        LOG.info("Closing connection to the AMQP broker.");

        try {

            if (commandChannel.isOpen()) {

                LOG.trace("Closing command channel.");

                commandChannel.close();
            }

            if (connection.isOpen()) {

                LOG.trace("Closing connection.");

                connection.close();
            }

        } catch (IOException e) {

            LOG.error("Error occurred when trying to close connection to AMQP broker.", e);
        }
    }

    private final HashSet<BusStatusListener> busStatusListeners = new HashSet<BusStatusListener>();
    
	@Override
	public void attachBusStatusListener(BusStatusListener listener) {
		busStatusListeners.add(listener);
		
	}
	
	@Override
	public void dettachBusStatusListener(BusStatusListener listener) {
		busStatusListeners.remove(listener);
	}
    /**
     * Implementation for ShutdownListener interface.
     */
	@Override
	public void shutdownCompleted(ShutdownSignalException signal) {
		if(signal == null)
			LOG.info("AMQP Connection shutdown notice received.");
		else
			LOG.error("AMQP Connection shutdown exception received.", signal);
			
		if(isClosing || isInConnectionErrorState) return;
		
        LOG.trace("Setting isInConnectionErrorState to true.");
		isInConnectionErrorState = true;
		
		StopWatch watch = new StopWatch();
		watch.start();
		try{
			
			while(watch.getTime() < 30000){
				LOG.info("Attempting to reopen connection.");
				try{
					openConnectionToBroker();
					LOG.info("Connection successfully reopened.");
					isInConnectionErrorState = false;
					try{
						notifyListenersOfConnectionClose(true);
					} catch (Exception e){
						LOG.warn("notifyListenersOfConnectionClose threw an error: " + e.getMessage(), e);	
					}
					return;
				} catch (Exception e) {
					LOG.error("Attempt to reopen connection failed with error: " + e.getMessage(), e);
					try {
						Thread.sleep(100);
					} catch (InterruptedException e1) {
						LOG.warn("Attempt to reopen connection canceled because thread has been interrupted.");
						//should we notify listeners in this case?
						return;
					}
				}
			}
			
			LOG.warn("Attempt to reopen connection permanently failed.");
			notifyListenersOfConnectionClose(false);

		} finally {
			watch.stop();
		}
	}

	private void notifyListenersOfConnectionClose(boolean connectionSuccessfullyReopened) {
		for (BusStatusListener listener : busStatusListeners){
	        LOG.trace("Invoking notifyUnexpectedConnectionClose(" + connectionSuccessfullyReopened + ") on listener [" + listener.toString() + "]");
			try{
				listener.notifyUnexpectedConnectionClose(connectionSuccessfullyReopened);
			} catch(Exception e) {
		        LOG.error("Invoking notifyUnexpectedConnectionClose(" + connectionSuccessfullyReopened + ") on listener [" + listener.toString() + "] threw an exception: " + e.getMessage(), e);
			}
		}
	}

	/**
     * Create a new AMQP exchange
     * 
     * @param exchange
     *            The exchange information
     */
    @Override
    public void createExchange(RoutingInfo.Exchange exchange) {

        LOG.debug("Creating the [{}] exchange.", exchange.getName());

        try {
            commandChannel.exchangeDeclare(exchange.getName(), exchange.getType().toString().toLowerCase(), exchange.isDurable());
        } catch (IOException e) {

            LOG.error("Could not create the [{}] exchange.", exchange.getName(), e);

            throw new RuntimeException("Failed to create exchange: " + e.getMessage() + "See inner exception for details", e);
        }
    }

    /**
     * Create a new AMQP queue
     * 
     * @param name
     *            Name of the queue
     * @param bindings
     *            The information necessary to bind the queue to exchanges
     * @param durable
     *            Is the queue durable?
     */
    @Override
    public void createQueue(String name, RoutingInfo[] bindings, boolean durable) {

        LOG.debug("Declaring queue [{}]; durable? = {}", name, durable);

        try {

            commandChannel.queueDeclare(name, durable, false, false, null);

        } catch (IOException e) {

            LOG.error("Could not declare queue {}", name, e);

            throw new RuntimeException("Failed to create queue: " + e.getMessage() + "See inner exception for details", e);
        }
        for (RoutingInfo binding : bindings) {

            LOG.debug("Binding queue [{}] to exchange [{}] with the routing key [{}]", new Object[] { name, binding.getExchange().getName(), binding.getRoutingKey() });

            try {

                commandChannel.queueBind(name, binding.getExchange().getName(), binding.getRoutingKey());

            } catch (IOException e) {

                LOG.error("Could not create binding for queue [{}] on exchange [{}] with expression [{}]", new Object[] { name, binding.getExchange().getName(), binding.getRoutingKey() }, e);

                throw new RuntimeException("Failed to create binding: " + binding.getRoutingKey() + " on queue: " + name + " See inner exception for details", e);
            }
        }
    }

    /**
     * Delete a Queue
     * 
     * @param queueName
     *            Name of the Queue to remove
     */
    @Override
    public void deleteQueue(String queueName) {

        LOG.debug("Deleting queue [{}]", queueName);

        try {

            commandChannel.queueDelete(queueName);

        } catch (IOException e) {

            LOG.error("Could not delete queue [{}].", queueName, e);

            throw new RuntimeException("Failed to delete queue: " + e.getMessage() + "See inner exception for details", e);
        }
    }

    /**
     * Publish a message using the provided route.
     * 
     * @param route
     *            Information used to route the message
     * @param message
     *            Message to publish
     */
    @Override
    public void publish(RoutingInfo route, Envelope message) {

        LOG.debug("Publishing message of type [{}] on exchange [{}]", message.getEventType(), route.getExchange().getName());

        try {

            LOG.trace("Creating AMQP headers for the message being published.");

            Map<String, Object> headersOut = new HashMap<String, Object>();

            if (message.getTopic() != null) {
                headersOut.put(TOPIC_HEADER_KEY, message.getTopic());
            }

            final Map<String, String> headersIn = message.getHeaders();

            for (String key : headersIn.keySet()) {
                headersOut.put(key, headersIn.get(key));
            }

            LOG.trace("Building AMQP property set for the message being published.");

            BasicProperties props = new BasicProperties.Builder().messageId(message.getId() == null ? null : message.getId().toString())
                    .correlationId(message.getCorrelationId() == null ? null : message.getCorrelationId().toString()).type(message.getEventType()).replyTo(message.getReplyTo()).headers(headersOut)
                    .build();

            LOG.trace("Publishing the message on the bus.");

            commandChannel.basicPublish(route.getExchange().getName(), route.getRoutingKey(), props, message.getBody());

        } catch (IOException e) {

            LOG.error("Could not publish message on bus.", e);

            throw new RuntimeException("Failed to publish message: " + e.getMessage() + "See inner exception for details", e);
        }

    }

    /**
     * Get the next message off the specified queue.
     * 
     * @param queueName
     *            Name of the Queue.
     * @return Message, if one exists on the queue (or null).
     */
    @Override
    public UnacceptedMessage getNextMessageFrom(String queueName) {

        LOG.debug("Retrieving message from queue [{}]", queueName);

        try {

            LOG.trace("Getting responses off the channel for queue [{}]", queueName);

            GetResponse receivedMessage = commandChannel.basicGet(queueName, false);

            LOG.trace("Message received for queue [{}]?: {}", queueName, receivedMessage != null);

            if (receivedMessage == null)
                return null;

            Envelope envelope = createEnvelope(receivedMessage.getProps(), receivedMessage.getBody());

            LOG.debug("Returning message from queue [{}] with Envelope.", queueName);

            return new UnacceptedMessage(envelope, receivedMessage.getEnvelope().getDeliveryTag());

        } catch (IOException e) {

            LOG.error("Could not get message from queue [{}]", queueName);

            throw new RuntimeException("Failed to get message from queue: " + queueName + " Error: " + e.getMessage() + "See inner exception for details", e);
        }
    }

	static Envelope createEnvelope(final BasicProperties props, byte[] body) {
		LOG.trace("Creating the Envelope.");

		Envelope envelope = new Envelope();

		LOG.trace("Placing the headers from the message into the Envelope.");

		Map<String, String> headers = envelope.getHeaders();

		if (props.getHeaders() != null) {
		    for (String key : props.getHeaders().keySet()) {
		        headers.put(key, props.getHeaders().get(key).toString());
		    }
		}

		LOG.trace("Mapping AMQP specific properties to Envelope properties.");

		envelope.setBody(body);
		envelope.setId(props.getMessageId() == null ? null : UUID.fromString(props.getMessageId()));
		envelope.setCorrelationId(props.getCorrelationId() == null ? null : UUID.fromString(props.getCorrelationId()));
		envelope.setEventType(props.getType());
		envelope.setReplyTo(props.getReplyTo());
		envelope.setTopic(headers.get(TOPIC_HEADER_KEY));

		// We don't want the topic key to be a Header property of the envelope.
		headers.remove(TOPIC_HEADER_KEY);
		return envelope;
	}

    /**
     * Inform the bus that the message has been delivered to the client.
     * 
     * @param message
     *            Message being acknowledged
     */
    @Override
    public void acceptMessage(UnacceptedMessage message) {

        LOG.debug("Accepting Message for Event Type [{}] with ID [{}]", message.getEnvelope().getEventType(), message.getAcceptanceToken());

        try {

            commandChannel.basicAck(message.getAcceptanceToken(), false);

        } catch (IOException e) {

            LOG.error("Could not acknowledge receipt of the message with ID [{}]", message.getAcceptanceToken(), e);

            throw new RuntimeException("Failed to get acknowledge message: " + e.getMessage() + "See inner exception for details", e);
        }
    }

    /**
     * Inform the bus that the message is being rejected by the client, and optionally, whether the bus should retry to deliver the message at a later time.
     * 
     * @param message
     *            Message to reject
     * @param redeliverMessageLater
     *            Attempt to redeliver the message later?
     */
    @Override
    public void rejectMessage(UnacceptedMessage message, boolean redeliverMessageLater) {

        LOG.debug("Rejecting Message for Event Type [{}] with ID [{}], redeliver? = {}", new Object[] { message.getEnvelope().getEventType(), message.getAcceptanceToken(), redeliverMessageLater });

        try {

            commandChannel.basicReject(message.getAcceptanceToken(), redeliverMessageLater);

        } catch (IOException e) {

            LOG.error("Could not reject the message with ID [{}]", message.getAcceptanceToken(), e);

            throw new RuntimeException("Failed to get acknowledge message: " + e.getMessage() + "See inner exception for details", e);
        }
    }

	@Override
	public String beginConsumingMessages(final String queueName, final EnvelopeHandler consumer) {
		
		LOG.trace("Begin consuming messages for queue [{}] with an EnvelopeHandler of type [{}].", queueName, consumer.getClass().getCanonicalName());

		String consumerTag = queueName + ":" + UUID.randomUUID().toString();

		LOG.trace("ConsumerTag set to [{}].", consumerTag);

		final Channel consumerChannel;
		try {
		
			LOG.trace("Opening dedicated channel for ConsumerTag [{}].", consumerTag);
			
			consumerChannel = connection.createChannel();
			
			LOG.trace("Successfully opened dedicated channel for ConsumerTag [{}].", consumerTag);
			
			consumerChannels.put(consumerTag, consumerChannel);

		} catch (IOException e) {
            LOG.error("Could not create channel to consume messages on queue: [{}]", queueName, e);

            throw new RuntimeException("Could not create channel to consume messages on queue: " + queueName, e);
		}
		
		try {
			
			LOG.trace("Beginning basicConsume for ConsumerTag [{}].", consumerTag);
			
			consumerChannel.basicConsume(queueName, false, consumerTag, 
					new EnvelopeHandlerBasedConsumer(consumerChannel, consumerTag, consumer));
			
			LOG.trace("Begun basicConsume for ConsumerTag [{}].", consumerTag);

		} catch (IOException e) {
            LOG.error("Failed to initiate basicConsume ConsumerTag [{}].", consumerTag, e);

            throw new RuntimeException("Failed to initiate basicConsume ConsumerTag: "+ consumerTag, e);
		}
		
		return consumerTag;
	}

	@Override
	public void stopConsumingMessages(String consumerTag) {
		synchronized (consumerChannels) {
			Channel channel = consumerChannels.get(consumerTag);
			if(channel == null) return;
			consumerChannels.remove(consumerTag);
			try {
				channel.basicCancel(consumerTag);
			} catch (IOException e) {
	            LOG.error("Failed to cancel basicConsume for ConsumerTag: [{}]", consumerTag, e);

	            throw new RuntimeException("Failed to cancel basicConsume for ConsumerTag: " + consumerTag, e);
			}
		}
		
	}
}
