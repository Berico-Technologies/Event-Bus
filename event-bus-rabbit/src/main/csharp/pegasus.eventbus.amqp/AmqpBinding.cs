using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace pegasus.eventbus.amqp
{
    public struct AmqpBinding : IEquatable<AmqpBinding>
    {
        public IExchange Exchange;
        public string Topic;


        public AmqpBinding(IExchange exchange, string topic)
        {
            this.Exchange = exchange;
            this.Topic = topic;
        }


        public bool Equals(AmqpBinding other)
        {
            bool isEqual = false;

            try
            {
                if ((this.Topic == other.Topic) &&
                    (this.Exchange.Equals(other.Exchange)))
                {
                    isEqual = true;
                }
            }
            catch (NullReferenceException) { }

            return isEqual;
        }

        public override bool Equals(object obj)
        {
            bool isEqual = false;

            if (obj is AmqpBinding)
            {
                isEqual = this.Equals((AmqpBinding)obj);
            }

            return isEqual;
        }

        public override int GetHashCode()
        {
            int hash = 17;

            unchecked
            {
                hash = hash * 23 + this.Topic.GetHashCode();
                hash = hash * 23 + this.Exchange.GetHashCode();
            }

            return hash;
        }
    }
}
