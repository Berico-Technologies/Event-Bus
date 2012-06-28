using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace pegasus.eventbus.client
{
    public class Subscription
    {
        private IEventHandler _eventHandler;
        private IEnvelopeHandler _envelopeHandler;
        private string _queueName;
        private bool _isDurable;
        private int numberOfThreads = 1;


        public bool IsDurable
        {
            get { return _isDurable; }
            set { _isDurable = value; }
        }


        /// <summary>
        /// Subscribes an <see cref="IEventHandler" /> to receive one or more event types according to the value 
        /// returned by the handler's <see cref="IEventHandler.HandledEventTypes"/> property.
        /// </summary>
        /// <param name="eventHandler"></param>
        public Subscription(IEventHandler eventHandler)
        {
            if (null == eventHandler) { throw new ArgumentNullException("eventHandler"); }

            _eventHandler = eventHandler;
        }

        /// <summary>
        /// Subscribes an <see cref="EventHandler"> to a named queue to receive one or more event types according to 
        /// the value returned by the handler's <see cref="EventHandler.HandledEventTypes" /> property. Named queues 
        /// are necessary if a subscription is to be durable, in order to re-subscribe to the same queue of events. 
        /// Named queues are also useful for establishing shared queues wherein multiple processes need to share the 
        /// responsibility for handling events in the same queue. This can be useful for load balancing for example. 
        /// Each message that enters the queue is guaranteed to be handled by only one of the shared subscribers.
        /// </summary>
        /// <param name="eventHandler">
        /// The <see cref="EventHandler"/> implementation that will handle the subscribed events.
        /// </param>
        /// <param name="queueName">The name of the queue to subscribe to.</param>
        public Subscription(IEventHandler eventHandler, string queueName)
        {
            _eventHandler = eventHandler;
            _queueName = queueName;
            _isDurable = true;
        }

        /// <summary>
        /// Subscribes an <see cref="EventHandler"/> to receive one or more event types according to the value 
        /// returned by the handler's <see cref="EventHandler.HandledEventTypes"/> property.
        /// </summary>
        /// <param name="envelopeHandler">
        /// The <cref see="EnvelopeHandler" /> implementation that will handle the subscribed events.
        /// </param>
        public Subscription(IEnvelopeHandler envelopeHandler)
        {
            if (null == envelopeHandler) throw new ArgumentNullException("envelopeHandler");

            _envelopeHandler = envelopeHandler;
        }

        /// <summary>
        /// Subscribes an <see cref="EventHandler" /> to a named queue to receive one or more event types according to 
        /// the value returned by the handler's <see cref="EventHandler.HandledEventTypes" /> method. Named queues are 
        /// necessary if a subscription is to be durable, in order to re-subscribe to the same queue of events. Named 
        /// queues are also useful for establishing shared queues wherein multiple processes need to share the 
        /// responsibility for handling events in the same queue. This can be useful for load balancing for example. 
        /// Each message that enters the queue is guaranteed to be handled by only one of the shared subscribers.
        /// </summary>
        /// <param name="envelopeHandler"></param>
        /// <param name="queueName"></param>
        public Subscription(IEnvelopeHandler envelopeHandler, string queueName)
            : this(envelopeHandler)
        {
            _queueName = queueName;
            _isDurable = true;
        }
    }
}
