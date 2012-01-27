using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using pegasus.eventbus.client;


namespace pegasus.eventbus.amqp
{
    public class TypedEventSubscription<TEvent> : EventSubscription where TEvent : class
    {
        private Action<TEvent> _typedHandler;


        public TypedEventSubscription(string topic, Action<TEvent> handler)
        {
            base.Topic = topic;
            base.Handler = this.Handle_Event;

            _typedHandler = handler;
        }

        public TypedEventSubscription(Action<TEvent> handler)
        {
            base.Topic = typeof(TEvent).FullName;
            base.Handler = this.Handle_Event;

            _typedHandler = handler;
        }


        private void Handle_Event(IEvent baseEvent)
        {
            _typedHandler((TEvent)baseEvent);
        }
    }
}
