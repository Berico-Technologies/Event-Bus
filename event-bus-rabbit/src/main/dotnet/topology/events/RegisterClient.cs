using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace pegasus.eventbus.topology.events
{
    public class RegisterClient : Registration
    {
        public string Version;


        public RegisterClient() { }
        public RegisterClient(string clientName, string version) 
            : base(clientName)
        {
            this.Version = version;
        }
    }
}
