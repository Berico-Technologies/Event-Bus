using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using pegasus.eventbus.client;

namespace pegasus.eventbus.amqp
{
    public interface IEventSubscription
    {
        string Topic { get; }

        Action<IEvent> Handler { get; }
    }
}
