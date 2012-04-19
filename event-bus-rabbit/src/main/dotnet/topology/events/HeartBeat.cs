using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace pegasus.eventbus.topology.events
{
    public class HeartBeat : Registration
    {
        public HeartBeat() { }
        public HeartBeat(string clientName) : base(clientName) { }
    }
}
