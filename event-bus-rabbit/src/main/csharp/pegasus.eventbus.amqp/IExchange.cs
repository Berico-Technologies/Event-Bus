using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace pegasus.eventbus.amqp
{
    public interface IExchange : IEquatable<IExchange>, IDisposable
    {
        string Name { get; }

        string Hostname { get; }

        string VirtualHost { get; }


        void Publish(AmqpEnvelope env);


        string ToUri();
    }
}
