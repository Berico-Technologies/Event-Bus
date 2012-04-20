package gov.ment.eventbus.topology.events;

import gov.ment.eventbus.topology.TopologyRegistry;

public class TopologyUpdate {

  private TopologyRegistry topologyRegistry;

  public TopologyRegistry getTopologyRegistry() {
    return topologyRegistry;
  }

  public void setTopologyRegistry(TopologyRegistry topologyRegistry) {
    this.topologyRegistry = topologyRegistry;
  }

}
