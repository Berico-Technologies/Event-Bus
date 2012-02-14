package pegasus.eventbus.rabbitmq;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pegasus.eventbus.client.Envelope;
import pegasus.eventbus.client.EnvelopeHandler;
import pegasus.eventbus.client.EventResult;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.AMQP.BasicProperties;

class EnvelopeHandlerBasedConsumer extends DefaultConsumer {

	private final Logger    		LOG;
	private final String 			queueName; 
	private final EnvelopeHandler 	consumer;
	
	public EnvelopeHandlerBasedConsumer(Channel channel, String queueName, EnvelopeHandler handler) {
		super(channel);
		this.queueName = queueName;
		this.consumer = handler;
	
		LOG = LoggerFactory.getLogger(String.format("%s$>%s", this.getClass().getName(), queueName.replace('.', '_')));
	}

	@Override
	public void handleDelivery(String consumerTag,
			com.rabbitmq.client.Envelope amqpEnvelope,
			BasicProperties properties, 
			byte[] body)
			throws IOException {
		
		super.handleDelivery(consumerTag, amqpEnvelope, properties, body);
		
		Channel channel = this.getChannel();
		
		LOG.trace("Handling delivery for ConsumerTag [{}].", consumerTag);
		
		long deliveryTag  = amqpEnvelope.getDeliveryTag();
        
		LOG.trace("DeliveryTag is [{}] for message on ConsumerTag [{}]", deliveryTag, consumerTag);
		
		Envelope envelope = RabbitMessageBus.createEnvelope(properties, body);
		
		LOG.trace("Envelope create for DeliveryTag [{}].", deliveryTag);

		EventResult result;
		try {

			LOG.trace("Handling envelope for DeliveryTag [{}].", deliveryTag);

			//TODO: what if handler incorrectly returns null? I think we should assume Failed to be safe.
            result = consumer.handleEnvelope(envelope);
            
        } catch (Exception e) {

        	result = EventResult.Failed;

            String id;

            try {

                id = envelope.getId().toString();

            } catch (Exception ee) {

                id = "<message id not available>";
            }

            LOG.error("Envelope handler of type " + consumer.getClass().getCanonicalName() + " on queue " + queueName + " threw exception of type " + e.getClass().getCanonicalName()
                    + " handling message " + id + ", DeliveryTag: " + deliveryTag, e);
        }

        LOG.trace("Determining how to handle EventResult [{}]", result);

        switch (result) {
            case Handled:

                LOG.trace("Accepting DeliveryTag [{}]", deliveryTag);

                channel.basicAck(deliveryTag, false);

                break;
            case Failed:

                LOG.trace("Rejecting DeliveryTag [{}]", deliveryTag);

                channel.basicReject(deliveryTag, false);

                break;
            case Retry:

                LOG.trace("Retrying DeliveryTag [{}]", deliveryTag);

                channel.basicReject(deliveryTag, true);

                break;
        }
	}
}
