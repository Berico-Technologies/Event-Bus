package pegasus.eventbus.amqp;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pegasus.eventbus.client.EnvelopeHandler;

/**
 * Watches a Queue for new messages on a background thread, calling the EnvelopeHandler when new messages arrive.
 */
class QueueListener  {

    
    /**
     *  A {@link Runnable} for creating queues and starting and stopping
     *  message consumption on them. Because it's {@link Runnable}, the {@link
     *  QueueListener} can invoke multiple ones in an {@link ExecutorService},
     *  and PEGA-315 says that's a Good Thing.
     */
    public final class QueueTPie implements Runnable {
        
        /**
         *  The unique identifier for this queue. The uniqueness has to be
         *  enforced by the caller.
         */
        private final String        queueName;
        
        /**
         *  The ID returned by {@link AmqpMessageBus#beginConsumingMessages(
         *  String, EnvelopeHandler)}, which is then passed to {@link
         *  AmqpMessageBus#stopConsumingMessages(String)} when {@link #stop()}
         *  is called.
         */
        private String              consumerTag;
        
        /**
         *  Construct the queue with the specified name.
         *  
         *	@param  qName           the name of the queue. It can't be {@code
         *                          null}, unless you hate all that is good and
         *                          proper.
         */
        public QueueTPie(final String qName) {
            if (qName != null) {
            queueName = qName;
            } else {
                throw new IllegalArgumentException(
                                "The queue name cannot be null. "
                                + "Didn't you read the documentation?");
            }
        }
        
        /**
         *  {@linkplain AmqpMessageBus#createQueue(String, RoutingInfo[],
         *  boolean) Create a queue on the message bus} and {@linkplain
         *  AmqpMessageBus#beginConsumingMessages(String, EnvelopeHandler)
         *  start consuming messages} on it.
         *  
         *	@see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            LOG.trace("Creating new queue [{}] (if not already existing).", queueName);
            messageBus.createQueue(queueName, routes, queueIsDurable);
            consumerTag = messageBus.beginConsumingMessages(queueName, envelopeHandler);            
            LOG.debug("Now consuming queue [" + queueName + "] with consumerTag [" + consumerTag + "].");
        }
        
        /**
         *  {@linkplain AmqpMessageBus#stopConsumingMessages(String) Stop
         *  consuming messages} on this queue.
         */
        public void stop() {
            LOG.debug("Stopping consume of queue [" + queueName + "], consumerTag [" + consumerTag + "].");    
            messageBus.stopConsumingMessages(consumerTag);            
            LOG.trace("Successfully stopped consume of queue [" + queueName + "], consumerTag [" + consumerTag + "].");
        }
        
    }
    
    protected final Logger         LOG;

    private final AmqpMessageBus   messageBus;
    private final String           queueBaseName;
	private final Boolean          queueIsDurable;
    private final RoutingInfo[]    routes;

    private EnvelopeHandler        envelopeHandler;
    
    private volatile boolean       currentlyListening;

    private final ExecutorService  executorService;
    private final int              numThreads;

    private final List<QueueTPie>  queues;

    /**
     * Start up an new Queue Listener bound on the supplied queue name, with the provided EnvelopeHander dealing with new messages.
     * 
     * @param queueName
     *            Name of the Queue to watch.
     * @param envelopeHandler
     *            EnvelopeHandler that deals with new messages.
     * @param amqpEventManager
     *            The EventManager that is managing the subscription.
     * @deprecated Use {@link #QueueListener(AmqpMessageBus,String,Boolean,RoutingInfo[],EnvelopeHandler,int)} instead
     */
    public QueueListener(
    		AmqpMessageBus messageBus, 
    		String queueName,
    		Boolean queueIsDurable,
    		RoutingInfo[] routes, 
    		EnvelopeHandler envelopeHandler){
                this(messageBus, queueName, queueIsDurable, routes,
                                envelopeHandler, 5);
            }

    /**
     * Start up an new Queue Listener bound on the supplied queue name, with the provided EnvelopeHander dealing with new messages.
     * @param queueName
     *            Name of the Queue to watch.
     * @param envelopeHandler
     *            EnvelopeHandler that deals with new messages.
     * @param numberOfThreads TODO
     * @param amqpEventManager
     *            The EventManager that is managing the subscription.
     */
    public QueueListener(
			AmqpMessageBus messageBus, 
			String queueName,
			Boolean queueIsDurable,
			RoutingInfo[] routes, 
			EnvelopeHandler envelopeHandler,
			int numberOfThreads){
		this.messageBus = messageBus;
		this.queueBaseName = queueName;
		this.queueIsDurable = queueIsDurable;
		this.routes = routes;
		this.envelopeHandler = envelopeHandler;
		
		if (numberOfThreads > 0) {
            this.numThreads = numberOfThreads;
		    executorService = Executors.newFixedThreadPool(numThreads);
		    queues = new ArrayList<QueueTPie>(numThreads);
		} else {
		    throw new IllegalArgumentException("The number of threads has to be 1 or greater.");
		}

        // Custom Logger for Each Queue Listener.
        //TODO: PEGA-727 Need to add tests to assert that this logger name is always valid (i.e. queue names with . and any other illegal chars are correctly mangled.)
        LOG = LoggerFactory.getLogger(String.format("%s$>%s", this.getClass().getCanonicalName(), queueName.replace('.', '_')));
    }

    /**
     * Begin listening for messages on the Queue.
     */
    public synchronized void beginListening() {
        for (int i = 0; i < numThreads; ++i) {
            String queueName = queueBaseName + "-" + i;
            QueueTPie queue = new QueueTPie(queueName);
            queues.add(queue);
            executorService.execute(queue);
        }
        
        currentlyListening = true;
    }

    /**
     * Command the QueueListener to stop listening on the queue, thereby stopping the background thread.
     */
    public synchronized void StopListening() {
        for (QueueTPie queue : queues) {
            queue.stop();
        }
        
        executorService.shutdown(); // or do we want shutdownNow()?
        currentlyListening = false;
    }

    /**
     * Is the QueueListener currently monitoring the Queue?
     * 
     * @return true is it is monitoring queue.
     */
    //TODO: PEGA-729 This method is no longer needed.  We presume that StopListening now blocks untill stopped.
    public synchronized boolean isCurrentlyListening() {
        return currentlyListening;
    }
}