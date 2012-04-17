using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;

using log4net;

using pegasus.eventbus.client;
using pegasus.eventbus.amqp;
using pegasus.eventbus.topology.events;


namespace pegasus.eventbus.topology
{
    public class GlobalTopologyService : ITopologyService
    {
        private static readonly ILog LOG = LogManager.GetLogger(typeof(GlobalTopologyService));
        private static readonly string EXPECTED_TOPOLOGY_VERSION = "1.0";

        private string _clientName;
        private int _heartBeatIntervalSeconds;
        private IEventManager _eventMgr;
        private SubscriptionToken _subToken;
        private Timer _heartBeat;
        private object _eventLock = new object();

        private IDictionary<string, RoutingInfo> _eventRoute;
        private IDictionary<string, IEnumerable<RoutingInfo>> _eventSetRoutes;


        public GlobalTopologyService(string clientName, int heartBeatIntervalInSeconds)
        {
            _eventRoute = new Dictionary<string, RoutingInfo>();
            _eventSetRoutes = new Dictionary<string, IEnumerable<RoutingInfo>>();

            _clientName = clientName;
            _heartBeatIntervalSeconds = heartBeatIntervalInSeconds;
        }


        public void Start(IEventManager eventManager)
        {
            _eventMgr = eventManager;

            // subscribe to the topology service's topology updates
            _subToken = _eventMgr.Subscribe<TopologyUpdate>(this.Handle_TopologyUpdate);

            // and publish a RegisterClient event so it knows we exist
            _eventMgr.Publish(new RegisterClient(_clientName, EXPECTED_TOPOLOGY_VERSION));

            // finally, start the update heartbeat
            _heartBeat = new Timer(
                new TimerCallback(this.SendHeartBeat), 
                null, 
                _heartBeatIntervalSeconds * 1000, 
                _heartBeatIntervalSeconds * 1000);
        }

        public void Close()
        {
            using (_heartBeat) { _heartBeat.Change(Timeout.Infinite, Timeout.Infinite); }

            _eventMgr.Unsubscribe(_subToken);
            _eventMgr.Publish(new UnregisterClient(_clientName));

            LOG.Debug("Global topology service closed");
        }

        public RoutingInfo GetRoutingInfoForEventOfType(Type evType)
        {
            RoutingInfo route = null;

            lock (_eventLock)
            {
                if (_eventRoute.ContainsKey(evType.FullName))
                {
                    route = _eventRoute[evType.FullName];
                }
            }

            return route;
        }

        public IEnumerable<RoutingInfo> GetRoutingInfoForNamedEventSet(string eventSetName)
        {
            IEnumerable<RoutingInfo> routeSet = null;

            lock (_eventLock)
            {
                if (_eventSetRoutes.ContainsKey(eventSetName))
                {
                    routeSet = _eventSetRoutes[eventSetName];
                }
            }

            return routeSet;
        }


        protected virtual EventResult Handle_TopologyUpdate(TopologyUpdate update)
        {
            try
            {
                lock (_eventLock)
                {
                    if (null == update.TopologyRegistry.EventRoute)
                    {
                        LOG.Debug("A topology update arrived with null for its event routes.  Keeping old topology.");
                    }
                    else
                    {
                        _eventRoute = update.TopologyRegistry.EventRoute;
                        LOG.DebugFormat("Updating event routes.  New topology has {0} routes.", _eventRoute.Count);
                    }

                    if (null == update.TopologyRegistry.EventSetRoutes)
                    {
                        LOG.Debug("A topology update arrived with null for its event set routes.  Keeping old topology.");
                    }
                    else
                    {
                        _eventSetRoutes = update.TopologyRegistry.EventSetRoutes;
                        LOG.DebugFormat("Updating event set routes.  New topology has {0} set routes.", _eventSetRoutes.Count);
                    }
                }

                return EventResult.Handled;
            }
            catch { return EventResult.Failed; }
        }

        protected virtual void SendHeartBeat(object alwaysNull)
        {
            _eventMgr.Publish(new HeartBeat(_clientName));
        }
    }
}
