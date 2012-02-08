using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using pegasus.eventbus.client;


namespace pegasus.eventbus.amqp
{
    public interface IQueueManager : IDisposable
    {
        event AmqpEnvelopeHandler EnvelopeReceived;


        void AddSubscription(IEventSubscription subscription);

        void RemoveSubscription(IEventSubscription subscription);


        void Send(IEvent message);
    }
}
