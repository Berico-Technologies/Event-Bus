package pegasus.eventbus.amqp;

import pegasus.eventbus.client.Envelope;
import pegasus.eventbus.client.EnvelopeHandler;

/**
 * A source of indirection between a specific AMQP client (like RabbitMQ) and the AmqpEventManager.
 * 
 * @author Ken Baltrinic (Berico Technologies)
 */
public interface AmqpMessageBus {

    /**
     * Start the bus and open the connections.
     * 
     * @param eventManager
     */
    void start();

    /**
     * Close the underlying connection the bus.
     */
    void close();
    
    /**
     * Create and Exchange on the Bus.
     * 
     * @param exchange
     *            Exchange Info
     */
    void createExchange(RoutingInfo.Exchange exchange);

    /**
     * Create a Queue on the Bus.
     * 
     * @param name
     *            Name of the Queue to be created.
     * @param bindings
     *            Bindings between that Queue and an Exchange(s)
     * @param durable
     *            Is the Queue durable
     */
    void createQueue(String name, RoutingInfo[] bindings, boolean durable);

    /**
     * Delete a Queue on the Bus.
     * 
     * @param queueName
     *            Name of the Queue to remove.
     */
    void deleteQueue(String queueName);

    /**
     * Publish a Message on the Queue, using the provided Routing Info
     * 
     * @param route
     *            Routing Info that designates the Exchange to publish the event on.
     * @param message
     *            Message being published
     */
    void publish(RoutingInfo route, Envelope message);

    /**
     * Pull the next message from a given queue.
     * 
     * @param queueName
     *            Name (id) of the Queue
     * @return Message that needs to be acknowledged
     */
    @Deprecated
    UnacceptedMessage getNextMessageFrom(String queueName);

    /**
     * Reject a message received from a queue.
     * 
     * @param message
     *            Message to be rejected
     * @param redeliverMessageLater
     *            Should the message be redelivered?
     */
    @Deprecated
    void rejectMessage(UnacceptedMessage message, boolean redeliverMessageLater);

    /**
     * Begins consuming messages off of the specified queue
     * 
     * @param queueName 
     * 			  The name of the queue
     * @param consumer
     *            The EnvelopeHandler that will consume the messages.
     * @return
     * 	          A tag that must be used when calling stopConsumingMessages(tag)
     */
    String beginConsumingMessages(String queueName, EnvelopeHandler consumer);
    
    /**
     * Stops consuming messages that are being consumed as a result of a call to beginConsumingMessages.
     * @param consumerTag
     *            The tag returned by beginConsumingMessages
     */
    void stopConsumingMessages(String consumerTag);
    
    /**
     * Acknowledge receipt of a message.
     * 
     * @param message
     *            Message to acknowledge
     */
    @Deprecated
    void acceptMessage(UnacceptedMessage message);

    /**
     * Represents a Message that has been received by the bus, but not accepted (meaning that we have not told the AMQP broker that the message has been successfully delivered).
     * 
     * @author Ken Baltrinic (Berico Technologies)
     */
    @Deprecated
    public static class UnacceptedMessage {
        private final Envelope envelope;
        private final long     acknowledgementToken;

        /**
         * Instantiate the Unaccepted Message with the Envelope and unique identifier.
         * 
         * @param envelope
         * @param acknowledgementToken
         */
        public UnacceptedMessage(Envelope envelope, long acknowledgementToken) {

            this.envelope = envelope;
            this.acknowledgementToken = acknowledgementToken;
        }

        /**
         * Get the Envelope (essentially the message plus headers)
         * 
         * @return Envelope received by the bus.
         */
        public Envelope getEnvelope() {
            return envelope;
        }

        /**
         * Unique Id that distinguishes this message from all others received on by this client.
         * 
         * @return
         */
        public long getAcceptanceToken() {
            return acknowledgementToken;
        }
    }
}
