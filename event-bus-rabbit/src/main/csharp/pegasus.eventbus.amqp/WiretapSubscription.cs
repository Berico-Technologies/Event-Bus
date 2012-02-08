using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using pegasus.eventbus.client;


namespace pegasus.eventbus.amqp
{
    public class WiretapSubscription : IEventSubscription
    {
        public static readonly string WIRETAP_TOPIC = "#";


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


        public WiretapSubscription(Action<IEvent> wiretapHandler)
        {
            this.Topic = WIRETAP_TOPIC;
            this.Handler = wiretapHandler;
        }
    }
}
