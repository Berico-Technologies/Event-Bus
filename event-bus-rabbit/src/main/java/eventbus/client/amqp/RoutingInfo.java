package eventbus.client.amqp;

/**
 * Contains the Routing Information necessary to publish messages to or bind queues onto an exchange.
 * 
 * @author Ken Baltrinic (Berico Technologies)
 */
public class RoutingInfo {

    protected Exchange exchange;
    protected String   routingKey;

    /**
     * Instantiate an instance with the required properties
     * 
     * @param exchangeName
     *            Name of the Exchange
     * @param routingKey
     *            The routing key (or binding expression)
     */
    public RoutingInfo(String exchangeName, String routingKey) {
        this(new Exchange(exchangeName, ExchangeType.Topic, true), routingKey);
    }

    /**
     * Instantiate an instance with the required properties
     * 
     * @param exchangeName
     *            Name of the Exchange
     * @param exchangeType
     *            Type of Exchange
     * @param exchangeIsDurable
     *            Is the Exchange Durable (persistent)?
     * @param routingKey
     *            The routing key (or binding expression)
     */
    public RoutingInfo(String exchangeName, ExchangeType exchangeType, boolean exchangeIsDurable, String routingKey) {
        this(new Exchange(exchangeName, exchangeType, exchangeIsDurable), routingKey);
    }

    /**
     * Bare minimum properties required to instantiate a Routing Info instance.
     * 
     * @param exchange
     *            Name of the Exchange
     * @param routingKey
     *            The routing key (or binding expression)
     */
    public RoutingInfo(Exchange exchange, String routingKey) {
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    /**
     * Get the Exchange or this route
     * 
     * @return Exchange
     */
    public Exchange getExchange() {
        return exchange;
    }

    /**
     * Get the routing key (also called "binding expression").
     * 
     * @return Routing Key
     */
    public String getRoutingKey() {
        return routingKey;
    }

    /**
     * Get the object's representation as a string
     */
    @Override
    public String toString() {
        return String.format("RoutingInfo [exchangeName=%s, routingKey=%s]", exchange.getName(), routingKey);
    }

    /**
     * Hashcode overridden for purposes of determining equality
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((exchange == null) ? 0 : exchange.hashCode());
        result = prime * result + ((routingKey == null) ? 0 : routingKey.hashCode());
        return result;
    }

    /**
     * Does this instance equal another object?
     */
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

    /**
     * More formally describes an Exchange, at least how the Event Bus API utilizes them.
     * 
     * @author Ken Baltrinic (Berico Technologies)
     */
    public static class Exchange {

        protected String       name;
        protected ExchangeType type;
        protected boolean      isDurable;

        /**
         * Instantiate an Exchange with the supplied Exchange properties.
         * 
         * @param name
         *            Name of the Exchange
         * @param type
         *            Type of the Exchange
         * @param isDurable
         *            Is the Exchange durable?
         */
        public Exchange(String name, ExchangeType type, boolean isDurable) {
            this.name = name;
            this.type = type;
            this.isDurable = isDurable;
        }

        /**
         * Get the name of the Exchange
         * 
         * @return Name
         */
        public String getName() {
            return name;
        }

        /**
         * Is the Exchange Durable (persistent)?
         * 
         * @return True if Durable
         */
        public boolean isDurable() {
            return isDurable;
        }

        /**
         * Get the Exchange Type
         * 
         * @return Type of Exchange
         */
        public ExchangeType getType() {
            return type;
        }

        /**
         * Return the string representation of the Exchange
         */
        @Override
        public String toString() {
            return String.format("Exchange [Name=%s]", name);
        }

        /**
         * Overridden for equality purposes
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (isDurable ? 1231 : 1237);
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            result = prime * result + ((type == null) ? 0 : type.hashCode());
            return result;
        }

        /**
         * Is this instance equal to the supplied object?
         */
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

    /**
     * Describes the Types of Exchanges
     * 
     * @author Ken Baltrinic (Berico Technologies)
     */
    public enum ExchangeType {
        Direct, Topic, Fanout
    }
}
