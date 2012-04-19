using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace pegasus.eventbus.topology.events
{
    public class GetEventTypeRoute
    {
        public string EventTypeCannonicalName;

        public GetEventTypeRoute() { }
        public GetEventTypeRoute(string eventType) { this.EventTypeCannonicalName = eventType; }
    }
}
