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
     * Attaches a listener that can react to changes in the bus status.
     * 
     * @param listener
     *            The listener to attach. If listener is already attached, the call is ignored.
     */
    void attachBusStatusListener(BusStatusListener listener);

    /**
     * Attaches a listener that can react to changes in the bus status.
     * 
     * @param listener
     *            The listener to detach. If the listener is not attached, the call is ignored.
     */
    void dettachBusStatusListener(BusStatusListener listener);

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
     * Begins consuming messages off of the specified queue
     * 
     * @param queueName
     *            The name of the queue
     * @param consumer
     *            The EnvelopeHandler that will consume the messages.
     * @return A tag that must be used when calling stopConsumingMessages(tag)
     */
    String beginConsumingMessages(String queueName, EnvelopeHandler consumer);

    /**
     * Stops consuming messages that are being consumed as a result of a call to beginConsumingMessages.
     * 
     * @param consumerTag
     *            The tag returned by beginConsumingMessages
     */
    void stopConsumingMessages(String consumerTag);

    public interface BusStatusListener {
        /**
         * Called when the bus's connection to the AMQP broker is unexpectedly lost.
         * 
         * @param connectionSuccessfullyReopened
         *            Indicates if the bus was able to successfully reopen the connection.
         */
        void notifyUnexpectedConnectionClose(boolean connectionSuccessfullyReopened);
    }
    
}
