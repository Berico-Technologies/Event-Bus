package pegasus.eventbus.client;

import java.util.concurrent.TimeoutException;

/**
 * The primary API to the event bus by which {@link Event}s are published and subscribed to, etc.
 */
public interface EventManager {

    /**
     * Starts the event manager and notifies all life cycle listeners
     */
    void start();

    /**
     * Closes the event manager and unsubscribes any non-durable subscriptions.
     */
    void close();

    /**
     * Publishes an event to the bus.
     * 
     * @param event
     *            The event to publish.
     */
    void publish(Object event);

    /**
     * Subscribes an {@link EventHandler} to receive one or more event types according to the value returned by the handler's {@link EventHandler#getHandledEventTypes()} method. Subscriptions created
     * with this overload are not durable.
     * 
     * @param handler
     *            The {@link EventHandler} implementation that will handle the subscribed events.
     * @return a {@link SubscriptionToken} that uniquely identifies this subscription and can be used when invoking {@link EnvelopeHandler#unsubscribe(SubscriptionToken)}.
     */
    SubscriptionToken subscribe(EventHandler<?> handler);

    /**
     * Subscribes an {@link EventHandler} to a named queue to receive one or more event types according to the value returned by the handler's {@link EventHandler#getHandledEventTypes()} method. Named
     * queues are necessary if a subscription is to be durable, in order to re-subscribe to the same queue of events. Named queues are also useful for establishing shared queues wherein multiple
     * processes need to share the responsibility for handling events in the same queue. This can be useful for load balancing for example. Each message that enters the queue is guaranteed to be
     * handled by only one of the shared subscribers. Subscriptions created with this overload are durable.
     * 
     * @param handler
     *            The {@link EventHandler} implementation that will handle the subscribed events.
     * @param queueName
     *            The name of the shared queue to subscribe to.
     * @return a {@link SubscriptionToken} that uniquely identifies this subscription and can be used when invoking {@link EnvelopeHandler#unsubscribe(SubscriptionToken)}.
     */
    SubscriptionToken subscribe(EventHandler<?> handler, String queueName);

    /**
     * Subscribes an {@link EventHandler} to receive one or more event types according to the value returned by the handler's {@link EventHandler#getHandledEventTypes()} method. Subscriptions created
     * with this overload are not durable.
     * 
     * @param handler
     *            The {@link EnvelopeHandler} implementation that will handle the subscribed events.
     * @return a {@link SubscriptionToken} that uniquely identifies this subscription and can be used when invoking {@link EnvelopeHandler#unsubscribe(SubscriptionToken)}.
     */
    SubscriptionToken subscribe(EnvelopeHandler handler);

    /**
     * Subscribes an {@link EventHandler} to a named queue to receive one or more event types according to the value returned by the handler's {@link EventHandler#getHandledEventTypes()} method. Named
     * queues are necessary if a subscription is to be durable, in order to re-subscribe to the same queue of events. Named queues are also useful for establishing shared queues wherein multiple
     * processes need to share the responsibility for handling events in the same queue. This can be useful for load balancing for example. Each message that enters the queue is guaranteed to be
     * handled by only one of the shared subscribers. Subscriptions created with this overload are durable.
     * 
     * @param handler
     *            The {@link EnvelopeHandler} implementation that will handle the subscribed events.
     * @param queueName
     *            The name of the shared queue to subscribe to.
     * @return a {@link SubscriptionToken} that uniquely identifies this subscription and can be used when invoking {@link EnvelopeHandler#unsubscribe(SubscriptionToken)}.
     */
    SubscriptionToken subscribe(EnvelopeHandler handler, String queueName);

    /**
     * Subscribes using a {@link Subscription} instance to describe the subscription. Used for advanced subscription scenarios.
     * 
     * @param subscription
     *            The {@link Subscription} instance that provides the details of the subscription.
     * @return a {@link SubscriptionToken} that uniquely identifies this subscription and can be used when invoking {@link EnvelopeHandler#unsubscribe(SubscriptionToken)}.
     */
     SubscriptionToken subscribe(Subscription subscription);

    /**
     * Publishes an event and awaits for a single responding event. If more than one response is received, only the first is returned. Use {@link EventHandler#getResponseTo(Event, EventHandler<?
     * extends Event>) to receive more than one response. Responses must be sent by means of the {@link EventHandler#respondTo(Event, Event)} method.
     * 
     * @param event
     *            The event to publish
     * @param timeoutMills
     *            A timeout value in milliseconds to wait for a response before throwing an exception. Pass a value less than 1 to wait indefinitely.
     * @param responseTypes
     *            The list of eventTypes that may be received as responses.
     * @return the first event received in response to this event.
     * @throws InterruptedException
     *             if the thread is interrupted while waiting for a response.
     * @throws TimeoutException
     *             thrown if the timeout period elapses before a response is received.
     */
    <TResponse> TResponse getResponseTo(Object event, int timeoutMills, Class<? extends TResponse>... responseTypes) throws InterruptedException, TimeoutException;

    /**
     * Publishes an event provides an event handler to accept responses asynchronously. Responses must be sent by means of the {@link EventHandler#respondTo(Event, Event)} method.
     * 
     * @param event
     *            The event to publish.
     * @param handler
     *            The @{link EventHandler} instance that will handle responses.
     * @return an {@link SubscriptionToken} that should be passed to {@link EnvelopeHandler#unsubscribe(SubscriptionToken)} after all expected responses are received.
     */
    SubscriptionToken getResponseTo(Object event, EventHandler<?> handler);

    /**
     * Publishes an event in response to events published by either of the getResponseTo methods.
     * 
     * @param orginalRequest
     *            The original event published by getResponseTo.
     * @param response
     *            The event being published as a response.
     * @throws an
     *             illegal operation exception if originalRequest as not published using getResponseTo.
     */
    void respondTo(Object orginalRequest, Object response);

/**
	 * Unsubscribes an event handler from receiving events.  If the subscription is named, does not
	 * 		delete the queue.  Used {@link EventHandler.unsubscribe(SubscriptionToken, boolean) with
	 * 		true to delete a named queue.
	 * @param token The token returned by subscribe or getResponseTo.
	 */
    void unsubscribe(SubscriptionToken token);

    /**
     * Unsubscribes an event handler from receiving events and optionally deletes the queue if it is durable.
     * 
     * @param token
     *            The token returned by subscribe or getResponseTo.
     * @param deleteQueue
     *            indicates if the queue should be deleted. Only applicable for durable queues. Ignored if the queue is not durable.
     */
    void unsubscribe(SubscriptionToken token, boolean deleteQueue);

    /**
     * Add a listener for the {@link EventManager} start event.
     * 
     * @param listener
     *            The start listener.
     */
    void addStartListener(StartListener listener);

    /**
     * Add a listener for the {@link EventManager} close event.
     * 
     * @param listener
     *            The close listener.
     */
    void addCloseListener(CloseListener listener);

    /**
     * Add a listener for the {@link EventManager} subscribe event.
     * 
     * @param listener
     *            The subscribe listener.
     */
    void addSubscribeListener(SubscribeListener listener);

    /**
     * Add a listener for the {@link EventManager} unsubscribe event.
     * 
     * @param listener
     *            The unsubscribe listener.
     */
    void addUnsubscribeListener(UnsubscribeListener listener);

    /**
     * Remove a listener for the {@link EventManager} start event.
     * 
     * @param listener
     *            The start listener.
     */
    void removeStartListener(StartListener listener);

    /**
     * Remove a listener for the {@link EventManager} close event.
     * 
     * @param listener
     *            The close listener.
     */
    void removeCloseListener(CloseListener listener);

    /**
     * Remove a listener for the {@link EventManager} subscribe event.
     * 
     * @param listener
     *            The subscribe listener.
     */
    void removeSubscribeListener(SubscribeListener listener);

    /**
     * Remove a listener for the {@link EventManager} unsubscribe event.
     * 
     * @param listener
     *            The unsubscribe listener.
     */
    void removeUnsubscribeListener(UnsubscribeListener listener);

}
