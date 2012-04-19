using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace pegasus.eventbus.topology.events
{
    public class UnregisterClient : Registration
    {
        public UnregisterClient() { }
        public UnregisterClient(string clientName) : base(clientName) { }
    }
}
