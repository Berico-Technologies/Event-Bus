package pegasus.eventbus.client;

/**
 * SubscriptionTokens are the return value of all {@link EventManager} subscribe methods.  
 * They are provided to the client as a means of maintaining a reference to a subscription
 * for the purposes of being able to call {@link EventManager#Unsubscribe(SubscriptionToken)}.
 */
public class SubscriptionToken {

	private static int valueSeed = 1;
	
	private final int value;

	public SubscriptionToken() {
		this.value = valueSeed++;
	}
	
	public boolean equals(SubscriptionToken token){
		return token.value == value;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + value;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		return equals((SubscriptionToken)obj);
	}

	@Override
	public String toString() {
		return "SubscriptionToken " + value;
	}	
}
