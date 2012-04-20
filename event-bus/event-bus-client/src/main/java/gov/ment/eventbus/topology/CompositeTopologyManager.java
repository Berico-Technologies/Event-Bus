package gov.ment.eventbus.topology;

import java.util.PriorityQueue;

import gov.ment.eventbus.amqp.RoutingInfo;
import gov.ment.eventbus.amqp.TopologyManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.ment.eventbus.client.EventManager;

//TODO: This class need test coverage.
public class CompositeTopologyManager implements TopologyManager {

  private static final Logger LOG = LoggerFactory.getLogger(CompositeTopologyManager.class);

  private PriorityQueue<TopoWrapper> topologyWrappers = new PriorityQueue<TopoWrapper>();

  public CompositeTopologyManager() {

    LOG.info("Instantiating the Composite Topology Manager.");

  }

  @Override
  public void start(EventManager eventManager) {
    for (TopoWrapper topoWrapper : topologyWrappers) {
      topoWrapper.topologyManager.start(eventManager);
    }
  }

  @Override
  public void close() {
    for (TopoWrapper topoWrapper : topologyWrappers) {
      topoWrapper.topologyManager.close();
    }
  }

  @Override
  public RoutingInfo getRoutingInfoForEvent(Class<?> eventType) {
    RoutingInfo route = null;
    for (TopoWrapper topoWrapper : topologyWrappers) {
      route = topoWrapper.topologyManager.getRoutingInfoForEvent(eventType);
      if (route != null) {
        break;
      }
    }
    return route;
  }

  @Override
  public RoutingInfo[] getRoutingInfoForNamedEventSet(String eventSetName) {
    RoutingInfo[] routes = null;
    for (TopoWrapper topoWrapper : topologyWrappers) {
      routes = topoWrapper.topologyManager.getRoutingInfoForNamedEventSet(eventSetName);
      if (routes != null) {
        break;
      }
    }
    return routes;
  }

  public void addManager(TopologyManager topologyManager) {
    topologyWrappers.add(new TopoWrapper(topologyManager));
  }

  public void addManager(TopologyManager topologyManager, int priority) {
    topologyWrappers.add(new TopoWrapper(topologyManager, priority));
  }

  public void removeManager(TopologyManager topologyManager) {
    topologyWrappers.remove(topologyManager);
  }

  private class TopoWrapper implements Comparable<TopoWrapper> {

    private static final int DEFAULT_PRIORITY = 1;

    public int priority = DEFAULT_PRIORITY;
    public TopologyManager topologyManager;

    public TopoWrapper(TopologyManager topologyManager) {
      this.topologyManager = topologyManager;
    }

    public TopoWrapper(TopologyManager topologyManager, int priority) {
      this.topologyManager = topologyManager;
      this.priority = priority;
    }

    @Override
    public int compareTo(TopoWrapper topoWrapper) {
      if (priority < topoWrapper.priority) {
        return -1;
      } else if (priority > topoWrapper.priority) {
        return 1;
      }

      return 0;
    }
  }

}
