using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;


namespace pegasus.eventbus.topology.events
{
	public class TopologyUpdate
	{
        public TopologyRegistry TopologyRegistry;


        public TopologyUpdate() { }

        public TopologyUpdate(TopologyRegistry topoRegistry) { this.TopologyRegistry = topoRegistry; }
	}
}
