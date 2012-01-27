using System;
using System.Collections.ObjectModel;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using pegasus.eventbus.client;


namespace pegasus.eventbus.amqp
{
    public interface IQueue : IDisposable
    {
        event AmqpEnvelopeHandler EnvelopeReceived;


        void AddBinding(AmqpBinding binding);

        void RemoveBinding(AmqpBinding binding);

        bool ContainsBinding(AmqpBinding binding);
    }
}
