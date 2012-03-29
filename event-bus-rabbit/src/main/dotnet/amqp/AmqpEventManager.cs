using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using log4net;

using pegasus.eventbus.client;

namespace pegasus.eventbus.amqp
{
    public class AmqpEventManager : IEventManager
    {
        private static readonly ILog LOG = LogManager.GetLogger(typeof(AmqpEventManager));
        private static readonly string AMQP_ROUTE_SEGMENT_DELIMITER = ".";
        private static readonly string AMQP_ROUTE_SEGMENT_WILDCARD = "#";
		
		private string _clientName;
		private IAmqpMessageBus _messageBus;
        private ITopologyService _topologySvc;
        private IEventSerializer _serializer;

		private IDictionary<SubscriptionToken, ActiveSubscription> _activeSubscriptions;
		private IDictionary<object, Envelope> _envelopesBeingHandled;
		private IList<StartListener> _startListeners;
		private IList<CloseListener> _closeListeners;
		private IList<SubscribeListener> _subscribeListeners;
		private IList<UnsubscribeListener> _unsubscribeListeners;
		
		
        public AmqpEventManager(
            ITopologyService topologyService,
            IAmqpMessageBus messageBus,
            IEventSerializer serializer)
        {
            _topologySvc = topologyService;
            _messageBus = messageBus;
            _serializer = serializer;
			
			_activeSubscriptions = new Dictionary<SubscriptionToken, ActiveSubscription>();
			_envelopesBeingHandled = new Dictionary<object, Envelope>();
        }


        public void Start()
        {
            LOG.Info("The event bus client is starting");

            _topologySvc.Start();

            LOG.Info("The event bus client is started");
        }

        public void Close()
        {
            LOG.Info("The event bus client is stopping");

            _topologySvc.Close();

            LOG.Info("The event bus client is stopped");
        }

        public void Publish(object ev)
        {
            if (null == ev) { throw new ArgumentNullException("ev"); }

            this.InternalPublish(ev, null, false);
        }

        public SubscriptionToken Subscribe<TEvent>(Action<TEvent> handler, params Type[] types)
        {
            throw new NotImplementedException();
        }

        public SubscriptionToken Subscribe(Action<object> handler, string queueName)
        {
            throw new NotImplementedException();
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


        internal void InternalPublish(object ev, string replyToQueue, bool sendToReplyToQueue)
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

                //route.AddReplyToQueue(replyToQueue, AmqpEventManager.AMQP_ROUTE_SEGMENT_DELIMITER);
            }
            else
            {
                
            }
        }
    }
}
