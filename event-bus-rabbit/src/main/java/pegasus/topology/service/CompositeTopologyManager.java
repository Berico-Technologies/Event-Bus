package pegasus.topology.service;

import java.util.PriorityQueue;

import pegasus.eventbus.amqp.RoutingInfo;
import pegasus.eventbus.client.EventManager;

public class CompositeTopologyManager implements TopologyManager {

    private PriorityQueue<TopoWrapper> topologyWrappers = new PriorityQueue<TopoWrapper>();

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

    @Override
    public void start(EventManager eventManager) {
        for (TopoWrapper topoWrapper : topologyWrappers) {
            topoWrapper.topologyManager.start(eventManager);
        }
    }

    @Override
    public void stop() {
        for (TopoWrapper topoWrapper : topologyWrappers) {
            topoWrapper.topologyManager.stop();
        }
    }

    private class TopoWrapper implements Comparable<TopoWrapper> {

        private static final int DEFAULT_PRIORITY = 1;

        public int               priority         = DEFAULT_PRIORITY;
        public TopologyManager   topologyManager;

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
