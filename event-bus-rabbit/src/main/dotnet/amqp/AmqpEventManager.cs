using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;

using log4net;

using pegasus.eventbus.client;

namespace pegasus.eventbus.amqp
{
    public class AmqpEventManager : IEventManager
    {
        public event Action OnStart;
        public event Action<bool> OnClose;
        public event Action<SubscriptionToken> OnSubscribe;
        public event Action<SubscriptionToken> OnUnsubscribe;

        private static readonly ILog LOG = LogManager.GetLogger(typeof(AmqpEventManager));
        private static readonly string AMQP_ROUTE_SEGMENT_DELIMITER = ".";
        private static readonly string AMQP_ROUTE_SEGMENT_WILDCARD = "#";
		
		private string _clientName;
		private IAmqpMessageBus _messageBus;
        private ITopologyService _topologySvc;
        private IEventSerializer _serializer;

		private IDictionary<SubscriptionToken, ActiveSubscription> _activeSubscriptions;
		private IDictionary<object, Envelope> _envelopesBeingHandled;
        private IList<string> _exchangesKnownToExist;

        private object _subLock = new object();


        public AmqpEventManager(AmqpConfiguration configuration)
        {
            _topologySvc = configuration.TopologyService;
            _messageBus = configuration.MessageBus;
            _serializer = configuration.EventSerializer;
			
			_activeSubscriptions = new Dictionary<SubscriptionToken, ActiveSubscription>();
			_envelopesBeingHandled = new Dictionary<object, Envelope>();
            _exchangesKnownToExist = new List<string>();

            _messageBus.UnexpectedConnectionClose += this.MessageBus_UnexpectedConnectionClose;
        }


        public void Start()
        {
            LOG.Info("The event bus client is starting");

            _messageBus.Start();
            _topologySvc.Start();

            this.RaiseStartEvent();

            LOG.Info("The event bus client is started");
        }

        public void Close()
        {
            LOG.Info("The event bus client is stopping");

            this.RaiseCloseEvent(false);

            _topologySvc.Close();

            IEnumerable<ActiveSubscription> subs = null;

            lock (_subLock)
            {
                subs = _activeSubscriptions.Values;
            }

            this.UnsubscribeActiveSubscriptions(subs, false);
            _activeSubscriptions.Clear();

            _messageBus.Close();

            LOG.Info("The event bus client is stopped");
        }


        public void Publish(object ev)
        {
            if (null == ev) { throw new ArgumentNullException("ev"); }

            this.InternalPublish(ev, null, false);
        }


        public SubscriptionToken Subscribe<TEvent>(Func<TEvent, EventResult> handler) where TEvent : class
        {
            return this.Subscribe(handler, this.GetNewQueueName(), false);
        }

        public SubscriptionToken Subscribe<TEvent>(Func<TEvent, EventResult> handler, string queueName) where TEvent : class
        {
            return this.Subscribe(handler, queueName, false);
        }

        public SubscriptionToken Subscribe<TEvent>(Func<TEvent, EventResult> handler, string queueName, bool isDurable) where TEvent : class
        {
            return this.Subscribe(new Subscription(new TypedEventHandler<TEvent>(handler), queueName));
        }

        public SubscriptionToken Subscribe(IEnvelopeHandler handler)
        {
            throw new NotImplementedException();
        }

        public SubscriptionToken Subscribe(IEnvelopeHandler handler, string queueName)
        {
            throw new NotImplementedException();
        }

        public SubscriptionToken Subscribe(Subscription subscription)
        {
            throw new NotImplementedException();
        }


        public TResponse GetResponseTo<TResponse>(object ev, int timeoutMills)
        {
            throw new NotImplementedException();
        }

        public SubscriptionToken GetResponseTo(object ev, IEventHandler handler)
        {
            throw new NotImplementedException();
        }


        public void RespondTo(object orginalRequest, object response)
        {
            throw new NotImplementedException();
        }


        public void Unsubscribe(SubscriptionToken token)
        {
            throw new NotImplementedException();
        }

        public void Unsubscribe(SubscriptionToken token, bool deleteQueue)
        {
            throw new NotImplementedException();
        }



        internal virtual void InternalPublish(object ev, string replyToQueue, bool sendToReplyToQueue)
        {
            Type evType = ev.GetType();
            LOG.DebugFormat("publish - type: {0}, replyQueue: {1}, sendTo: {2}", evType, sendToReplyToQueue);

            RoutingInfo route = _topologySvc.GetRoutingInfoForEventOfType(evType);

            if (null == route)
            {
                string msg = string.Format("No route found for event of type {0}", evType);

                LOG.ErrorFormat(msg);
                throw new RoutingInfoException(msg);
            }

            if (sendToReplyToQueue)
            {
                LOG.Debug("Creating routing info for the replyTo queue");

                route = new RoutingInfo(route.Exchange, route.RoutingKey + AMQP_ROUTE_SEGMENT_DELIMITER + replyToQueue);
            }
            else
            {
                if (false == _exchangesKnownToExist.Contains(route.Exchange.Name))
                {
                    _messageBus.CreateExchange(route.Exchange);
                    _exchangesKnownToExist.Add(route.Exchange.Name);
                }
            }

            byte[] body = _serializer.Serialize(ev);

            Envelope env = new Envelope()
            {
                Body = body,
            };

            env.SetId(Guid.NewGuid());
            env.SetTopic(route.RoutingKey);
            env.SetEventType(ev.GetType().FullName);
            env.SetReplyTo(replyToQueue);
            env.SetSendTime(DateTime.Now);

            _messageBus.Publish(route, env);
        }

        protected virtual void MessageBus_UnexpectedConnectionClose(bool successfullyReopened)
        {
            if (successfullyReopened)
            {
                this.ResubscribeActiveSubscriptions();
            }
            else
            {
                // TODO: we should invoke the close listeners with a flag to let them no that this is an unplanned close
                // and otherwise do any needed cleanup.
                LOG.Warn("The message bus unexpectedly closed a connection and was unable to reopen it.");
            }
        }

        protected virtual void ResubscribeActiveSubscriptions()
        {
            LOG.Debug("Resubscribing active subscriptions");

            lock (_subLock)
            {
                foreach (ActiveSubscription sub in _activeSubscriptions.Values)
                {
                    sub.Listener.StartListening();
                }
            }
        }

        protected virtual void UnsubscribeActiveSubscriptions(IEnumerable<ActiveSubscription> subscriptions, bool deleteDurableQueues)
        {
            LOG.Debug("Deactivating subscriptions, stopping all listeners.");

            foreach (ActiveSubscription subscription in subscriptions)
            {
                subscription.Listener.StopListening();
            }

            IEnumerable<ActiveSubscription> slowSubs = subscriptions.Where(sub => sub.Listener.IsListening);

            while (0 != slowSubs.Count()) 
            {
                LOG.Debug("Some of the subscriptions are taking a while to shutdown.  Sleeping for 50ms.");
                Thread.Sleep(50);

                slowSubs = slowSubs.Where(sub => sub.Listener.IsListening);
            }

            if (deleteDurableQueues)
            {
                LOG.Info("Deleting all queues provided in the deactivated subscriptions list.");
            }

            foreach (ActiveSubscription subscription in subscriptions)
            {
                if (deleteDurableQueues || !subscription.IsForDurableQueue)
                {
                    LOG.DebugFormat("Deleting queue [{0}]", subscription.QueueName);

                    _messageBus.DeleteQueue(subscription.QueueName);
                }
            }
        
            // TODO: PEGA-730 BUG! We are not closing the subscription-specific amqp channel!
        }

        protected virtual void RaiseStartEvent()
        {
            if (null != this.OnStart)
            {
                foreach (Delegate action in this.OnStart.GetInvocationList())
                {
                    try { action.DynamicInvoke(); }
                    catch (Exception ex) { LOG.Warn("The event manager caught an unhandled exception: " + ex); }
                }
            }
        }

        protected virtual void RaiseCloseEvent(bool unexpected)
        {
            if (null != this.OnClose)
            {
                foreach (Delegate action in this.OnClose.GetInvocationList())
                {
                    try { action.DynamicInvoke(unexpected); }
                    catch (Exception ex) { LOG.Warn("The event manager caught an unhandled exception: " + ex); }
                }
            }
        }

        protected virtual string GetNewQueueName()
        {
            return string.Format("{0}:{1}", _clientName, Guid.NewGuid().ToString());
        }
    }
}
