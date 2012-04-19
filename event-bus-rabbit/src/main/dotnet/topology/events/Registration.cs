using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace pegasus.eventbus.topology.events
{
    public class Registration
    {
        public string ClientName;


        public Registration() { }
        public Registration(string clientName) { this.ClientName = clientName; }
    }
}
