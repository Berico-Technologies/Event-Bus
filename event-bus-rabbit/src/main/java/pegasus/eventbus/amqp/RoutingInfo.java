package pegasus.eventbus.amqp;

public class RoutingInfo{

	protected Exchange exchange;
	protected String routingKey;
	
	public RoutingInfo(String exchangeName, ExchangeType exchangeType, boolean exchangeIsDurable, String routingKey) {
		this(new Exchange( exchangeName, exchangeType, exchangeIsDurable), routingKey);
	}
	
	public RoutingInfo(Exchange exchange, String routingKey) {
		this.exchange = exchange;
		this.routingKey = routingKey;
	}
	
	public Exchange getExchange() {
		return exchange;
	}
	
	public String getRoutingKey() {
		return routingKey;
	}
	
	@Override
	public String toString() {
		return String.format("RoutingInfo [exchangeName=%s, routingKey=%s]",
				exchange.getName(), routingKey);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((exchange == null) ? 0 : exchange.hashCode());
		result = prime * result
				+ ((routingKey == null) ? 0 : routingKey.hashCode());
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
		RoutingInfo other = (RoutingInfo) obj;
		if (exchange == null) {
			if (other.exchange != null)
				return false;
		} else if (!exchange.equals(other.exchange))
			return false;
		if (routingKey == null) {
			if (other.routingKey != null)
				return false;
		} else if (!routingKey.equals(other.routingKey))
			return false;
		return true;
	}

	public static class Exchange{

		protected String name;
		protected ExchangeType type;
		protected boolean isDurable;
		
		public Exchange(String name, ExchangeType type, boolean isDurable) {
			this.name = name;
			this.type = type;
			this.isDurable = isDurable;
		}
		
		public String getName() {
			return name;
		}
		
		public boolean isDurable() {
			return isDurable;
		}
		
		public ExchangeType getType(){
			return type;
		}
		
		@Override
		public String toString() {
			return String.format("Exchange [Name=%s]", name);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (isDurable ? 1231 : 1237);
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result + ((type == null) ? 0 : type.hashCode());
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
			Exchange other = (Exchange) obj;
			if (isDurable != other.isDurable)
				return false;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			if (type != other.type)
				return false;
			return true;
		}
	}

	public enum ExchangeType{
		Direct,
		Topic,
		Fanout
	}
}
