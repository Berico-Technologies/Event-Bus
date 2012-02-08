package eventbus.client.api;


public class Subscription {

    private EventHandler<?> eventHandler;
    private EnvelopeHandler envelopeHandler;
    private String          queueName;
    private boolean         isDurable;
    private int             numberOfThreads = 1;

    /**
     * Subscribes an {@link EventHandler} to receive one or more event types according to the value returned by the handler's {@link EventHandler#getHandledEventTypes()} method.
     * 
     * @param handler
     *            The {@link EventHandler} implementation that will handle the subscribed events.
     * 
     * @return a {@link SubscriptionToken} that uniquely identifies this subscription and can be used when invoking {@link EnvelopeHandler#unsubscribe(SubscriptionToken)}.
     */
    public Subscription(EventHandler<?> handler) {
        if (handler == null) {
            throw new IllegalArgumentException("Handler cannot be null.");
        }
        this.eventHandler = handler;
    }

    /**
     * Subscribes an {@link EventHandler} to a named queue to receive one or more event types according to the value returned by the handler's {@link EventHandler#getHandledEventTypes()} method. Named
     * queues are necessary if a subscription is to be durable, in order to re-subscribe to the same queue of events. Named queues are also useful for establishing shared queues wherein multiple
     * processes need to share the responsibility for handling events in the same queue. This can be useful for load balancing for example. Each message that enters the queue is guaranteed to be
     * handled by only one of the shared subscribers.
     * 
     * @param handler
     *            The {@link EventHandler} implementation that will handle the subscribed events.
     * @param queueName
     *            The name of the queue to subscribe to.
     */
    public Subscription(EventHandler<?> handler, String queueName) {
        this(handler);
        setQueueName(queueName);
        this.isDurable = true;
    }

    /**
     * Subscribes an {@link EventHandler} to receive one or more event types according to the value returned by the handler's {@link EventHandler#getHandledEventTypes()} method.
     * 
     * @param handler
     *            The {@link EnvelopeHandler} implementation that will handle the subscribed events.
     * 
     * @return a {@link SubscriptionToken} that uniquely identifies this subscription and can be used when invoking {@link EnvelopeHandler#unsubscribe(SubscriptionToken)}.
     */
    public Subscription(EnvelopeHandler handler) {
        if (handler == null) {
            throw new IllegalArgumentException("Handler cannot be null.");
        }
        this.envelopeHandler = handler;
    }

    /**
     * Subscribes an {@link EventHandler} to a named queue to receive one or more event types according to the value returned by the handler's {@link EventHandler#getHandledEventTypes()} method. Named
     * queues are necessary if a subscription is to be durable, in order to re-subscribe to the same queue of events. Named queues are also useful for establishing shared queues wherein multiple
     * processes need to share the responsibility for handling events in the same queue. This can be useful for load balancing for example. Each message that enters the queue is guaranteed to be
     * handled by only one of the shared subscribers.
     * 
     * @param handler
     *            The {@link EnvelopeHandler} implementation that will handle the subscribed events.
     * @param queueName
     *            The name of the queue to subscribe to.
     */
    public Subscription(EnvelopeHandler handler, String queueName) {
        this(handler);
        setQueueName(queueName);
        this.isDurable = true;
    }

    // /**
    // * Subscribes an {@link EnvelopeHandler} to a named subscription to receive all events defined for the named event set. This enables handling of disparate event types in an abstract manner by
    // * providing access to the raw event data. No attempt to cast the serialized event data to its statically defined event type is made.
    // *
    // * @param eventsetName
    // * The name of the event set to subscribe to.
    // * @param handler
    // * The envelop handler that will handle the events.
    // */
    // public Subscription(String eventsetName, EnvelopeHandler handler) {
    // if (handler == null)
    // throw new IllegalArgumentException("Handler cannot be null.");
    // this.envelopeHandler = handler;
    // setEventsetName(eventsetName);
    // }

    /**
     * The {@link EventHandler} implementation that will handle the subscribed events. Will be null if the subscription is an {@link EnvelopeHandler} based subscription.
     */
    public EventHandler<?> getEventHandler() {
        return this.eventHandler;
    }

    /**
     * The {@link EnvelopeHandler} that will be called to handle each received event. Will be null if the subscription is an {@link EventHandler} based subscription.
     */
    public EnvelopeHandler getEnvelopeHandler() {
        return this.envelopeHandler;
    }

    /**
     * The name of the queue to subscribe to. If null, an arbitrary name will be used.
     * 
     * Named queues are necessary if a subscription is to be durable, in order to re-subscribe to the same queue of events. Named queues are also useful for establishing shared queues wherein multiple
     * processes need to share the responsibility for handling events in the same queue. This can be useful for load balancing for example. Each message that enters the queue is guaranteed to be
     * handled by only one of the shared subscribers.
     */
    public String getQueueName() {
        return this.queueName;
    }

    /**
     * The name of the queue to subscribe to. If null, an arbitrary name will be used.
     * 
     * Named queues are necessary if a subscription is to be durable, in order to re-subscribe to the same queue of events. Named queues are also useful for establishing shared queues wherein multiple
     * processes need to share the responsibility for handling events in the same queue. This can be useful for load balancing for example. Each message that enters the queue is guaranteed to be
     * handled by only one of the shared subscribers.
     */
    public void setQueueName(String queueName) {
        if (queueName == "") {
            throw new IllegalArgumentException("QueueName cannot be set to an empty string, use null instead.");
        }

        this.queueName = queueName;
    }

    // /**
    // * Named event sets are a means to subscribe to a set of events with needing to specify the exact event types in code. The events contained within a named event set are determined
    // * administratively.
    // *
    // * @return the event set name if any.
    // */
    // public String getEventsetName() {
    // return eventsetName;
    // }

    // /**
    // * Named event sets are a means to subscribe to a set of events with needing to specify the exact event types in code. The events contained within a named event set are determined
    // * administratively.
    // */
    // public void setEventsetName(String eventsetName) {
    // if (envelopeHandler != null) {
    // if (eventsetName == "" || eventsetName == null)
    // throw new IllegalArgumentException("EventsetName cannot be null or a zero length string when creating an envelope-based subscription.");
    // } else if (eventsetName == "")
    // throw new IllegalArgumentException("EventsetName cannot be set to an empty string, use null instead.");
    //
    // this.eventsetName = eventsetName;
    // }

    /**
     * Indicates if the subscription should survive the {@link EventManager} being closed. Non-durable subscriptions are "unsubscribed" when the EventManager closes. Durable subscriptions will not
     * have their events delivered to a handler while the EventManager is closed but the events will continue to accumulate in the queue and will be delivered as soon a new event handler is subscribed
     * for the same named queue.
     * 
     * This value defaults to true if the queueName is specified (not null). Otherwise it is forced to false;
     */
    public boolean getIsDurable() {
        return this.isDurable && queueName != null;
    }

    /**
     * Indicates if the subscription should survive the {@link EventManager} being closed. Non-durable subscriptions are "unsubscribed" when the EventManager closes. Durable subscriptions will not
     * have their events delivered to a handler while the EventManager is closed but the events will continue to accumulate in the queue and will be delivered as soon a new event handler is subscribed
     * for the same named queue.
     * 
     * This value defaults to true if the queueName is specified (not null). Otherwise it is forced to false;
     */
    public void setIsDurable(boolean isDurable) {
        if (isDurable && queueName == null) {
            throw new IllegalArgumentException("isDurable may not be set to true unless a queueName has been specified.");
        }
        this.isDurable = isDurable;
    }

    /**
     * Indicates the number of threads used to listen for events. The default is 1, which ensures that the handler will not be called on multiple threads and need not be thread safe. If the handler is
     * thread safe, then setting this value to a number greater than 1 can improve performance.
     */
    public int getNumberOfThreads() {
        return this.numberOfThreads;
    }

    /**
     * Indicates the number of threads used to listen for events. The default is 1, which ensures that the handler will not be called on multiple threads and need not be thread safe. If the handler is
     * thread safe, then setting this value to a number greater than 1 can improve performance.
     */
    public void setNumberOfThreads(int threads) {
        this.numberOfThreads = threads;
    }
}
