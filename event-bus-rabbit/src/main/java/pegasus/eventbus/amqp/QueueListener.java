package pegasus.eventbus.amqp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import pegasus.eventbus.client.EnvelopeHandler;

/**
 * Watches a Queue for new messages on a background thread, calling the EnvelopeHandler when new messages arrive.
 * 
 * @author Ken Baltrinic (Berico Technologies)
 */
class QueueListener  {

    protected final Logger         LOG;

    private final AmqpMessageBus   messageBus;
    private final String           queueName;
	private final Boolean          queueIsDurable;
    private final RoutingInfo[]    routes;

    private EnvelopeHandler        envelopeHandler;

    private String                 consumerTag;
    
    private volatile boolean       currentlyListening;

    /**
     * Start up an new Queue Listener bound on the supplied queue name, with the provided EnvelopeHander dealing with new messages.
     * 
     * @param queueName
     *            Name of the Queue to watch.
     * @param envelopeHandler
     *            EnvelopeHandler that deals with new messages.
     * @param amqpEventManager
     *            TODO
     */
    public QueueListener(
			AmqpMessageBus messageBus, 
			String queueName,
			Boolean queueIsDurable,
			RoutingInfo[] routes, 
			EnvelopeHandler envelopeHandler){
		this.messageBus = messageBus;
		this.queueName = queueName;
		this.queueIsDurable = queueIsDurable;
		this.routes = routes;
		this.envelopeHandler = envelopeHandler;

        // Custom Logger for Each Queue Listener.
        //TODO: Need to add tests to assert that this logger name is always valid (i.e. queue names with . and any other illegal chars are correctly mangled.)
        LOG = LoggerFactory.getLogger(String.format("%s$>%s", this.getClass().getName(), queueName.replace('.', '_')));
    }

    /**
     * Begin listening for messages on the Queue.
     */
    public void beginListening() {

        LOG.trace("Creating new queue [{}] (if not already existing).", queueName);

        messageBus.createQueue(queueName, routes, queueIsDurable);

        consumerTag = messageBus.beginConsumingMessages(queueName, envelopeHandler);
        
        LOG.debug("Now consuming queue [" + queueName + "] with consumerTag [" + consumerTag + "].");

        currentlyListening = true;
    }

    /**
     * Command the QueueListener to stop listening on the queue, thereby stopping the background thread.
     */
    public void StopListening() {

        LOG.debug("Stopping consume of queue [" + queueName + "], consumerTag [" + consumerTag + "].");

        messageBus.stopConsumingMessages(consumerTag);
        
        LOG.trace("Successfully stopped consume of queue [" + queueName + "], consumerTag [" + consumerTag + "].");

        currentlyListening = false;
    }

    /**
     * Is the QueueListener currently monitoring the Queue?
     * 
     * @return true is it is monitoring queue.
     */
    //TODO: This method is no longer needed.  We presume that StopListening now blocks untill stopped.
    public boolean isCurrentlyListening() {
        return currentlyListening;
    }
}