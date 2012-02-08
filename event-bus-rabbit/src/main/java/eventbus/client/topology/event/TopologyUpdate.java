package eventbus.client.topology.event;

import eventbus.client.topology.TopologyRegistry;

public class TopologyUpdate {

    private TopologyRegistry topologyRegistry;

    public TopologyRegistry getTopologyRegistry() {
        return topologyRegistry;
    }

    public void setTopologyRegistry(TopologyRegistry topologyRegistry) {
        this.topologyRegistry = topologyRegistry;
    }

}
