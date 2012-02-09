using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;


namespace pegasus.eventbus.client
{
    /// <summary>
    /// A token that can identify your subscription should you want to unsubscribe.
    /// </summary>
    public struct SubscriptionToken : IEquatable<SubscriptionToken>
    {
        /// <summary>
        /// A value that uniquely identifies a subscription.  Automatically generated.
        /// </summary>
        public int Value;

        /// <summary>
        /// The topic that was subscribed.
        /// </summary>
        public string Topic;


        public bool Equals(SubscriptionToken other)
        {
            return this.Value == other.Value;
        }

        public override bool Equals(object obj)
        {
            bool isEqual = false;

            if (obj is SubscriptionToken)
            {
                isEqual = this.Equals((SubscriptionToken)obj);
            }

            return isEqual;
        }

        public override int GetHashCode()
        {
            return base.GetHashCode();
        }

        public override string ToString()
        {
            return string.Format("{0}#{1}", this.Topic, this.Value);
        }
    }
}
