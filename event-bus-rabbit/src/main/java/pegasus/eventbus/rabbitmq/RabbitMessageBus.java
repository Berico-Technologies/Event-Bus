package pegasus.eventbus.rabbitmq;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

import pegasus.eventbus.client.Envelope;
import pegasus.eventbus.client.EventManager;

/**
 * RabbitMQ implementation of our AmqpMessageBus interface.
 * 
 * @author Ken Baltrinic (Berico Technologies)
 */
public class RabbitMessageBus implements AmqpMessageBus {

    private static final Logger    LOG              = LoggerFactory.getLogger(RabbitMessageBus.class);

    final static String            TOPIC_HEADER_KEY = "pegasus.eventbus.event.topic";

    protected ConnectionParameters config;
    private Connection             connection;
    private Channel                commandChannel;

    /**
     * Initialize Rabbit with the given connection parameters,
     * 
     * @param connectionParameters
     *            Connection Parameters
     */
    public RabbitMessageBus(ConnectionParameters connectionParameters) {
        this.config = connectionParameters;
    }

    @Override
    public void start(EventManager eventManager) {

        LOG.trace("Starting the RabbitMessageBus");

        try {

            LOG.trace("Building the RabbitMQ Connection Factory.");

            ConnectionFactory connectionFactory = new ConnectionFactory();
            connectionFactory.setUsername(config.getUsername());
            connectionFactory.setPassword(config.getPassword());
            connectionFactory.setVirtualHost(config.getVirtualHost());
            connectionFactory.setHost(config.getHost());
            connectionFactory.setPort(config.getPort());

            LOG.trace("Grabbing the connection instance from the factory.");

            this.connection = connectionFactory.newConnection();
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

            LOG.trace("Pulling headers from message.");

            final BasicProperties props = receivedMessage.getProps();

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

            envelope.setBody(receivedMessage.getBody());
            envelope.setId(props.getMessageId() == null ? null : UUID.fromString(props.getMessageId()));
            envelope.setCorrelationId(props.getCorrelationId() == null ? null : UUID.fromString(props.getCorrelationId()));
            envelope.setEventType(props.getType());
            envelope.setReplyTo(props.getReplyTo());
            envelope.setTopic(headers.get(TOPIC_HEADER_KEY));

            // We don't want the topic key to be a Header property of the envelope.
            headers.remove(TOPIC_HEADER_KEY);

            LOG.debug("Returning message from queue [{}] with Envelope.", queueName);

            return new UnacceptedMessage(envelope, receivedMessage.getEnvelope().getDeliveryTag());

        } catch (IOException e) {

            LOG.error("Could not get message from queue [{}]", queueName);

            throw new RuntimeException("Failed to get message from queue: " + queueName + " Error: " + e.getMessage() + "See inner exception for details", e);
        }
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

}