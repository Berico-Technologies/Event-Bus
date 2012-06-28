using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using pegasus.eventbus.amqp;


namespace pegasus.eventbus.topology
{
    public class TopologyRegistry
    {
        private static readonly string VERSION = "1.0";

        public Dictionary<string, RoutingInfo> EventRoute = new Dictionary<string, RoutingInfo>();
        public Dictionary<string, IEnumerable<RoutingInfo>> EventSetRoutes = new Dictionary<string, IEnumerable<RoutingInfo>>();
    }
}
