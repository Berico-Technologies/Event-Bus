using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using pegasus.eventbus.amqp;


namespace pegasus.eventbus.topology.events
{
    public class EventTypeRoutingInfo
    {
        public string EventTypeCannonicalName;
        public RoutingInfo RouteInfo;


        public EventTypeRoutingInfo() { }
        public EventTypeRoutingInfo(string eventType, RoutingInfo routeInfo)
        {
            this.EventTypeCannonicalName = eventType;
            this.RouteInfo = routeInfo;
        }
    }
}
