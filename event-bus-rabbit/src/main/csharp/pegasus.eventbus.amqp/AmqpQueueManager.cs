using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using log4net;

using pegasus.eventbus.client;


namespace pegasus.eventbus.amqp
{
    public class AmqpQueueManager : IQueueManager
    {
        public event AmqpEnvelopeHandler EnvelopeReceived;

        private static ILog LOG = LogManager.GetLogger(typeof(AmqpQueueManager));

        private IQueueFactory _queueFactory;
        private IExchangeLocator _exchangeLocator;
        private IDictionary<AmqpBinding, int> _bindingCount;
        private IDictionary<IExchange, IQueue> _boundQueues;


        public AmqpQueueManager(IExchangeLocator exchangeLocator, IQueueFactory queueFactory)
        {
            _exchangeLocator = exchangeLocator;
            _queueFactory = queueFactory;

            _bindingCount = new Dictionary<AmqpBinding, int>();
            _boundQueues = new Dictionary<IExchange, IQueue>();
        }


        public void AddSubscription(IEventSubscription subscription)
        {
            List<IExchange> exchanges = new List<IExchange>();

            if (subscription.IsWiretap())
            {
                exchanges.AddRange(_exchangeLocator.GetAllExchanges());
            }
            else
            {
                // find out which exchange we need to bind to
                exchanges.Add(_exchangeLocator.GetExchange(subscription.Topic));
            }

            exchanges.ForEach(ex =>
            {
                // now, get or declare the queue for this exchange
                IQueue queue = this.GetQueue(ex);

                // create a binding 
                AmqpBinding binding = new AmqpBinding(ex, subscription.Topic);

                // add the binding to the queue if it doesn't already exist
                if (false == queue.ContainsBinding(binding))
                {
                    queue.AddBinding(binding);
                }

                // ensure an entry for this binding exists
                if (false == _bindingCount.ContainsKey(binding))
                {
                    _bindingCount.Add(binding, 0);
                }

                // finally, increment the count of subscriptions for this binding
                _bindingCount[binding]++;
            });
        }

        public void RemoveSubscription(IEventSubscription subscription)
        {
            // the exchange to which this subscription refers
            IExchange exchange = _exchangeLocator.GetExchange(subscription.Topic);

            // get the queue that is bound to the exchange
            IQueue queue = this.GetQueue(exchange);

            // create a binding
            AmqpBinding binding = new AmqpBinding(exchange, subscription.Topic);

            // don't assume that a binding exists
            if (_bindingCount.ContainsKey(binding))
            {
                // decrement the count of bindings
                _bindingCount[binding]--;

                if (0 == _bindingCount[binding])
                {
                    // since there are no more subscriptions for this binding,
                    // we should actually remove the binding from the queue.
                    queue.RemoveBinding(binding);
                }
            }
        }


        public IQueue GetQueue(IExchange exchange)
        {
            IQueue theQ = null;

            lock (_boundQueues)
            {
                if (_boundQueues.ContainsKey(exchange))
                {
                    theQ = _boundQueues[exchange];
                }
                else
                {
                    theQ = _queueFactory.CreateQueue(exchange);
                    theQ.EnvelopeReceived += this.Handle_Envelope;

                    _boundQueues.Add(exchange, theQ);
                }
            }

            return theQ;
        }

        public void Handle_Envelope(AmqpEnvelope env)
        {
            if (null != this.EnvelopeReceived)
            {
                // why am I doing this?  If an event has multiple handlers and one handler 
                // throws an exception, the other handlers will not be raised.
                foreach (Delegate callback in this.EnvelopeReceived.GetInvocationList())
                {
                    try
                    {
                        callback.DynamicInvoke(env);
                    }
                    catch (Exception ex)
                    {
                        LOG.Warn(
                            "Caught an unhandled exception thrown from a client attempting to handle a new envelope", 
                            ex);
                    }
                }
            }
        }

        public void Dispose()
        {
            // dispose of all exchanges and queues
            _boundQueues.Keys.ToList().ForEach(ex => ex.Dispose());
            _boundQueues.Values.ToList().ForEach(q => q.Dispose());
        }

        public void Send(IEvent message)
        {
            // I won't send null events!
            if (null == message) { throw new ArgumentNullException("message"); }

            // wrap our message in an AMQP envelope
            AmqpEnvelope env = new AmqpEnvelope(message);

            // locate the correct exchange for this topic
            IExchange exchange = _exchangeLocator.GetExchange(env.Topic);

            // add the appropriate headers
            env.EventType = message.GetType().FullName;
            env.SetHeader(AmqpHeaders.SEND_DATETIME, DateTime.Now.ToString());
            env.SetHeader(AmqpHeaders.REPLY_TO_EXCHANGE, exchange.ToUri());

            // place the envelope on the exchange
            exchange.Publish(env);
        }
    }



    public static class IEventSubscriptionExtensions
    {
        public static bool IsWiretap(this IEventSubscription sub)
        {
            bool isWiretap = false;

            if (string.Equals(sub.Topic, WiretapSubscription.WIRETAP_TOPIC, StringComparison.OrdinalIgnoreCase))
            {
                isWiretap = true;
            }

            return isWiretap;
        }
    }
}
