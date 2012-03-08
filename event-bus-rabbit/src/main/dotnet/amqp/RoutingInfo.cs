using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace pegasus.eventbus.amqp
{
    public class RoutingInfo
    {
        public Exchange Exchange { get; set; }
        public string RoutingKey { get; set; }


        public RoutingInfo(string exchangeName, string routingKey)
            : this(new Exchange(exchangeName, ExchangeType.Topic, true), routingKey)
        {
        }

        public RoutingInfo(string exchangeName, ExchangeType exchangeType, bool isDurable, string routingKey)
            : this(new Exchange(exchangeName, exchangeType, isDurable), routingKey)
        {
        }

        public RoutingInfo(Exchange exchange, string routingKey)
        {
            this.Exchange = exchange;
            this.RoutingKey = routingKey;
        }
    }
}
