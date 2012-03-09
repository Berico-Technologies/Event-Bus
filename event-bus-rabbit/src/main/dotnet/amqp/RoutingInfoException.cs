using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.Serialization;
using System.Text;

namespace pegasus.eventbus.amqp
{
    public class RoutingInfoException : Exception
    {
        public RoutingInfoException() : base() { }

        public RoutingInfoException(string message) : base(message) { }

        public RoutingInfoException(string message, Exception innerException) : base(message, innerException) { }

        public RoutingInfoException(SerializationInfo info, StreamingContext context) : base(info, context) {}
    }
}
