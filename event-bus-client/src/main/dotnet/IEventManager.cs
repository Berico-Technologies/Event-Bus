using System;

namespace pegasus.eventbus.client
{
	/// <summary>
	/// The primary API to the event bus by which Events are published and consumed (via subscription).
	/// </summary>
	public interface IEventManager
	{
		/// <summary>
		/// Starts the event manager and notifies all life cycle listeners
		/// </summary>
		void Start();
		
		/// <summary>
		/// Closes the event manager and unsubscribes any non-durable subscriptions.
		/// </summary>
		void Close();
		
		/// <summary>
		/// Publishes an event to the bus.
		/// <param name='ev'>The event to publish</param>
		/// </summary>
		void Publish(object ev);
		
		/// <summary>
		/// Subscribes an <see cref='IEventHandler' /> to receive one or more event types according to the value returned by 
		/// the handler's <code>EventHandler.GetHandledEventTypes()</code> method. Subscriptions created with this overload 
		/// are not durable.
		/// </summary>
		/// <param name='handler'>
		/// The <see cref='IEventHandler' /> implementation that will handle the subscribed events.
		/// </param>
		/// <returns>
		/// A <see cref='SubscriptionToken' /> that uniquely identifies this subscription and can be used when invoking 
		/// <code>EnvelopeHandler.Unsubscribe(SubscriptionToken)</code>.
		/// </returns>
		SubscriptionToken Subscribe(IEventHandler handler);
		
		/// <summary>
     	/// Subscribes an <see cref='IEventHandler'/> to a named queue to receive one or more event types according to 
     	/// the value returned by the handler's <code>EventHandler.GetHandledEventTypes()</code> method. Named queues 
     	/// are necessary if a subscription is to be durable, in order to re-subscribe to the same queue of events. 
     	/// Named queues are also useful for establishing shared queues wherein multiple processes need to share the 
     	/// responsibility for handling events in the same queue. This can be useful for load balancing for example. 
     	/// Each message that enters the queue is guaranteed to be handled by only one of the shared subscribers. 
     	/// Subscriptions created with this overload are durable.
     	/// </summary>
     	/// <param name='handler'>
     	/// The <see cref='EventHandler'/> implementation that will handle the subscribed events.
     	/// </param>
     	/// <param name='queueName'>The name of the shared queue to subscribe to.</para>
     	/// <returns>
     	/// A <see cref='SubscriptionToken'/> that uniquely identifies this subscription and can be used when invoking 
     	/// <code>EnvelopeHandler.Unsubscribe(SubscriptionToken)</code>.
     	/// </returns>
	    SubscriptionToken Subscribe(IEventHandler handler, string queueName);

		/// <summary>
		/// Subscribes an <see cref='IEventHandler'/> to receive one or more event types according to the value 
		/// returned by the handler's <code>EventHandler.GetHandledEventTypes()</code> method. Subscriptions 
		/// created with this overload are not durable.
		/// </summary>
		/// <param name='handler'>
		/// The <see cref='IEnvelopeHandler'/> implementation that will handle the subscribed events.
		/// </param>
		/// <returns>
		/// A <see cref='SubscriptionToken'/> that uniquely identifies this subscription and can be used when invoking 
		/// <code>EnvelopeHandler.Unsubscribe(SubscriptionToken)</code>.
		/// </returns>
	    SubscriptionToken Subscribe(EnvelopeHandler handler);

	    /// <summary>
	    /// Subscribes an <see cref='EventHandler'/> to a named queue to receive one or more event types according to the 
	    /// value returned by the handler's <code>EventHandler.GetHandledEventTypes()</code> method. Named queues are 
	    /// necessary if a subscription is to be durable, in order to re-subscribe to the same queue of events. Named 
	    /// queues are also useful for establishing shared queues wherein multiple processes need to share the 
	    /// responsibility for handling events in the same queue. This can be useful for load balancing for example. 
	    /// Each message that enters the queue is guaranteed to be handled by only one of the shared subscribers. 
	    /// Subscriptions created with this overload are durable.
     	/// </summary>
     	/// <param name='handler'>
	    /// The <see cref='EnvelopeHandler'/> implementation that will handle the subscribed events.
	    /// </param>
	    /// <param name='queueName'>
	    /// The name of the shared queue to subscribe to.
	    /// </param>
	    /// <returns>
	    /// A <see cref='SubscriptionToken'/> that uniquely identifies this subscription and can be used when invoking 
	    /// <code>EnvelopeHandler.Unsubscribe(SubscriptionToken)</code>.
	    /// </returns>
	    SubscriptionToken Subscribe(EnvelopeHandler handler, string queueName);
	
	    /// <summary>
		/// Subscribes using a <see cref='ISubscription'/> instance to describe the subscription. Used for advanced 
		/// subscription scenarios.
		/// </summary>
		/// <param name='subscription'>
		/// The <see cref='ISubscription'/> instance that provides the details of the subscription.
		/// </param>
		/// <returns>
		/// A <see cref='SubscriptionToken'/> that uniquely identifies this subscription and can be used when 
		/// invoking <code>EnvelopeHandler.Unsubscribe(SubscriptionToken)</code>.
		/// </returns>
	    SubscriptionToken Subscribe(Subscription subscription);
	
	    /// <summary>
		/// Publishes an event and awaits for a single responding event. If more than one response is received, only 
		/// the first is returned. Use <code>EventHandler.GetResponseTo(Event, IEventHandler)</code> to receive more 
		/// than one response. Responses must be sent by means of the <code>EventHandler.RespondTo(Event, Event)</code>
		/// method.
		/// </summary>
		/// <param name='event'>The event to publish</param>
		/// <param name='timeoutMills'>
		/// A timeout value in milliseconds to wait for a response before throwing an exception. Pass a value less 
		/// than 1 to wait indefinitely.
		/// </param>
		/// <param name='responseTypes'>The list of eventTypes that may be received as responses.</param>
		/// <returns>The first event received in response to this event.</returns>
		/// <exception cref="InterruptedException">
		/// If the thread is interrupted while waiting for a response.
		/// </exception>
		/// <exception cref="TimeoutException">
		/// Thrown if the timeout period elapses before a response is received.
		/// </exception>
		TResponse GetResponseTo<TResponse>(object ev, int timeoutMills);
	
		/// <summary>
		/// Publishes an event provides an event handler to accept responses asynchronously. Responses must be sent by 
		/// means of the <code>EventHandler.RespondTo(Event, Event)</code> method.
		/// </summary>
		/// <param name="event">The event to publish.</param>
		/// <param name="handler">
		/// The <see cref="T:IEventHandler"/> instance that will handle responses.
		/// </param>
		/// <returns>
		/// An <see cref="SubscriptionToken"/> that should be passed to 
		/// <code>EnvelopeHandler.Unsubscribe(SubscriptionToken)</code> after all expected responses are received.
		/// </returns>
	    SubscriptionToken GetResponseTo(object ev, IEventHandler handler);
	
		/// <summary>
		/// Publishes an event in response to events published by either of the getResponseTo methods.
		/// </summary>
		/// <param name='orginalRequest'>The original event published by getResponseTo.</param>
		/// <param name='response'>The event being published as a response.</param>
		/// <exception cref='IllegalOperationException'>
		/// If originalRequest was not published using getResponseTo.
		/// </exception>
	    void RespondTo(object orginalRequest, object response);
	
		/// <summary>
		/// Unsubscribes an event handler from receiving events.  If the subscription is named, does not delete the 
		/// queue.  Use <code>EventHandler.Unsubscribe(SubscriptionToken, boolean)</code> with <code>true</code> to 
		/// delete a named queue.
		/// </summary>
		/// <param name='token'>The token returned by subscribe or getResponseTo.</param>
	    void Unsubscribe(SubscriptionToken token);
	
		/// <summary>
		/// Unsubscribes an event handler from receiving events and optionally deletes the queue if it is durable.
		/// </summary>
		/// <param name='token'>The token returned by subscribe or getResponseTo.</param>
		/// <param name='deleteQueue'>
		/// Indicates if the queue should be deleted. Only applicable for durable queues. Ignored if the queue is not 
		/// durable.
		/// </param>
	    void Unsubscribe(SubscriptionToken token, bool deleteQueue);
	}
}

