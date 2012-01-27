using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using pegasus.eventbus.amqp;


namespace pegasus.eventbus.rabbit
{
    /// <summary>
    /// Component repsonsible for creating AMQP Queues, implemented by RabbitMQ.
    /// <remarks>
    /// Although in the AMQP model it's possible to have multiple queues connected
    /// to an exchange, our opinion is that one process participating in pub/sub
    /// only needs to have one queue per exchange.  If we allowed one process to 
    /// have a queue per message type 
    /// </remarks>
    /// </summary>
    public class RabbitQueueFactory : IQueueFactory
    {
        private IDictionary<IExchange, IQueue> _exchangeQueues;


        public RabbitQueueFactory()
        {
            _exchangeQueues = new Dictionary<IExchange, IQueue>();

        }


        public IQueue CreateQueue(IExchange exchange)
        {
            IQueue q = null;

            lock (_exchangeQueues)
            {
                if (_exchangeQueues.ContainsKey(exchange))
                {
                    q = _exchangeQueues[exchange];
                }
                else
                {
                    q = new RabbitQueue(exchange);
                    _exchangeQueues.Add(exchange, q);
                }
            }

            return q;
        }
    }
}
