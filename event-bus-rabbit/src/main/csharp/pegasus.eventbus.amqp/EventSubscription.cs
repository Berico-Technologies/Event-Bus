using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using pegasus.eventbus.client;

namespace pegasus.eventbus.amqp
{
    public class EventSubscription : IEventSubscription
    {
        public string Topic
        {
            get;
            protected set;
        }

        public Action<IEvent> Handler
        {
            get;
            protected set;
        }


        public EventSubscription(string topic, Action<IEvent> handler)
        {
            this.Topic = topic;
            this.Handler = handler;
        }

        protected EventSubscription() { }
    }
}
