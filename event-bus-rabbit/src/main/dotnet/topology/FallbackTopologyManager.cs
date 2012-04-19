using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using log4net;

using pegasus.eventbus.client;
using pegasus.eventbus.amqp;
using pegasus.eventbus.topology.events;


namespace pegasus.eventbus.topology
{
    public class FallbackTopologyManager : ITopologyService
    {
        private static readonly ILog LOG = LogManager.GetLogger(typeof(FallbackTopologyManager));

        private IEventManager _eventMgr;


        public void Start(IEventManager eventManager)
        {
            _eventMgr = eventManager;
        }

        public void Close()
        {
        }

        public RoutingInfo GetRoutingInfoForEventOfType(Type evType)
        {
            GetEventTypeRoute request = new GetEventTypeRoute(evType.FullName);
            RoutingInfo route = null;

            try
            {
                EventTypeRoutingInfo response = _eventMgr.GetResponseTo<EventTypeRoutingInfo>(request, 5 * 1000);
                route = response.RouteInfo;
            }
            catch (Exception ex)
            {
                LOG.WarnFormat("Failed to get routing information for events of type {0}", evType.FullName);
            }

            return route;
        }

        public IEnumerable<RoutingInfo> GetRoutingInfoForNamedEventSet(string eventSetName)
        {
            return null;
        }
    }
}
