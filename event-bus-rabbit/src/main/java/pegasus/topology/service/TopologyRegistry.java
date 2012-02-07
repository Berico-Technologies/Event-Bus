package pegasus.topology.service;

import java.util.HashMap;
import java.util.Map;

import pegasus.eventbus.amqp.RoutingInfo;

public class TopologyRegistry {

    private Map<String, RoutingInfo>   eventMap    = new HashMap<String, RoutingInfo>();
    private Map<String, RoutingInfo[]> eventSetMap = new HashMap<String, RoutingInfo[]>();

    public boolean hasEventRoute(String topic) {
        return eventMap.containsKey(topic);
    }

    public RoutingInfo getEventRoute(String topic) {
        return eventMap.get(topic);
    }

    public void setEventRoute(String topic, RoutingInfo route) {
        eventMap.put(topic, route);
    }

    public boolean hasEventSetRoutes(String eventSetName) {
        return eventSetMap.containsKey(eventSetName);
    }

    public RoutingInfo[] getEventSetRoutes(String eventSetName) {
        return eventSetMap.get(eventSetName);
    }

    public void setEventSetRoutes(String eventSetName, RoutingInfo[] routes) {
        eventSetMap.put(eventSetName, routes);
    }

    public void setEventMap(Map<String, RoutingInfo> eventMap) {
        this.eventMap = eventMap;
    }

    public void setEventSetMap(Map<String, RoutingInfo[]> eventSetMap) {
        this.eventSetMap = eventSetMap;
    }

}
