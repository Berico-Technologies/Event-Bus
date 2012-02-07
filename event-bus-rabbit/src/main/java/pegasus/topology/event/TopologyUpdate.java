package pegasus.topology.event;

import pegasus.topology.service.TopologyRegistry;

public class TopologyUpdate {

    private TopologyRegistry topologyRegistry;

    public TopologyRegistry getTopologyRegistry() {
        return topologyRegistry;
    }

    public void setTopologyRegistry(TopologyRegistry topologyRegistry) {
        this.topologyRegistry = topologyRegistry;
    }

}
