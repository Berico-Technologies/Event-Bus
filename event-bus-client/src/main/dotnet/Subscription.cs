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


        /// <summary>
        /// Subscribes an <see cref="IEventHandler"/> to receive one or more event types according to the value 
        /// returned by the handler's <see cref="IEventHandler.GetHandledEventTypes()"/> method.
        /// </summary>
        /// <param name="eventHandler"></param>
        public Subscription(IEventHandler eventHandler)
        {
            if (null == eventHandler) { throw new ArgumentNullException("eventHandler"); }

            _eventHandler = eventHandler;
        }

        public Subscription(IEventHandler eventHandler, string queueName)
        {
        }
    }
}
