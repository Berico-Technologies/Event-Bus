using System;

namespace pegasus.eventbus.client
{
	public class SubscriptionToken : IEquatable<SubscriptionToken>
	{
		public SubscriptionToken ()
		{
		}
		
		
		public override bool Equals (object obj)
		{
			return object.ReferenceEquals(this, obj);
		}
		
		public bool Equals (SubscriptionToken other)
		{
			return object.ReferenceEquals(this, other);
		}
	}
}

