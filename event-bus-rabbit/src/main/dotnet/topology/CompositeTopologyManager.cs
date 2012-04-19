using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using log4net;

using pegasus.eventbus.client;
using pegasus.eventbus.amqp;


namespace pegasus.eventbus.topology
{
    public class CompositeTopologyManager : ITopologyService
    {
        private static readonly ILog LOG = LogManager.GetLogger(typeof(CompositeTopologyManager));

        private Queue<ITopologyService> _topologyManagers;


        public CompositeTopologyManager()
        {
            _topologyManagers = new Queue<ITopologyService>();
        }


        public void Append(ITopologyService topologyService)
        {
            LOG.DebugFormat("Appending topology service {0} to the composite topology service", topologyService.GetHashCode());

            _topologyManagers.Enqueue(topologyService);
        }

        public void Start(IEventManager eventManager)
        {
            LOG.Debug("Composite Topology Service is starting");

            _topologyManagers.ToList().ForEach(mgr => mgr.Start(eventManager));
        }

        public void Close()
        {
            LOG.Debug("Composite Topology Service is closing");

            _topologyManagers.ToList().ForEach(mgr => mgr.Close());
        }

        public RoutingInfo GetRoutingInfoForEventOfType(Type evType)
        {
            RoutingInfo route = null;

            _topologyManagers.FirstOrDefault(svc =>
            {
                route = svc.GetRoutingInfoForEventOfType(evType);
                return (null != route);
            });

            if (null != route)
                LOG.DebugFormat("Returning route {0}:{1} for event of type {2}", route.RoutingKey, route.Exchange, evType.FullName);
            else
                LOG.DebugFormat("No route for event of type {0}", evType.FullName);

            return route;
        }

        public IEnumerable<RoutingInfo> GetRoutingInfoForNamedEventSet(string eventSetName)
        {
            IEnumerable<RoutingInfo> routeSet = null;

            _topologyManagers.FirstOrDefault(svc =>
            {
                routeSet = svc.GetRoutingInfoForNamedEventSet(eventSetName);
                return (null != routeSet);
            });

            if (null != routeSet)
                LOG.DebugFormat("Returning {0} routes for event set {1}", routeSet.Count(), eventSetName);
            else
                LOG.DebugFormat("No routes for event set {0}", eventSetName);

            return routeSet;
        }
    }
}
