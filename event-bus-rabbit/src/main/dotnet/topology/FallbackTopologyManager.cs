using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using pegasus.eventbus.client;
using pegasus.eventbus.amqp;


namespace pegasus.eventbus.topology
{
    public class FallbackTopologyManager : ITopologyService
    {
        private IEventManager _eventMgr;


        public void Start(IEventManager eventManager)
        {
            _eventMgr = eventManager;
        }

        public void Close()
        {
            throw new NotImplementedException();
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

            }

            return route;
        }

        public IEnumerable<RoutingInfo> GetRoutingInfoForNamedEventSet(string eventSetName)
        {
            return null;
        }
    }
}
