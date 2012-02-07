package pegasus.topology.service;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pegasus.eventbus.amqp.RoutingInfo;
import pegasus.eventbus.client.EventManager;
import pegasus.topology.event.RegisterClient;
import pegasus.topology.event.UnregisterClient;
import pegasus.topology.event.TopologyUpdate;

public class StaticTopologyManager implements TopologyManager {

    protected static final Logger      LOG                       = LoggerFactory.getLogger(StaticTopologyManager.class);
    private static final String        DEFAULT_TOPOLOGY_EXCHANGE = "topology";

    private String                     topologyExchange          = DEFAULT_TOPOLOGY_EXCHANGE;
    private Map<String, RoutingInfo>   topologyEventRegistry     = new HashMap<String, RoutingInfo>();
    private Map<String, RoutingInfo[]> topologyEventSetRegistry  = new HashMap<String, RoutingInfo[]>();

    public StaticTopologyManager() {
        this.topologyExchange = DEFAULT_TOPOLOGY_EXCHANGE;
        initializeTopologyRegistries();
    }

    public StaticTopologyManager(String topologyExchange) {
        this.topologyExchange = topologyExchange;
        initializeTopologyRegistries();
    }

    private void initializeTopologyRegistries() {
        String registerClientTopic = RegisterClient.class.getCanonicalName();
        RoutingInfo registerClientRoute = new RoutingInfo(topologyExchange, RoutingInfo.ExchangeType.Topic, true, registerClientTopic);
        topologyEventRegistry.put(registerClientTopic, registerClientRoute);
        String unregisterClientTopic = UnregisterClient.class.getCanonicalName();
        RoutingInfo unregisterClientRoute = new RoutingInfo(topologyExchange, RoutingInfo.ExchangeType.Topic, true, unregisterClientTopic);
        topologyEventRegistry.put(unregisterClientTopic, unregisterClientRoute);
        String topologyUpdateTopic = TopologyUpdate.class.getCanonicalName();
        RoutingInfo topologyUpdateRoute = new RoutingInfo(topologyExchange, RoutingInfo.ExchangeType.Topic, true, topologyUpdateTopic);
        topologyEventRegistry.put(topologyUpdateTopic, topologyUpdateRoute);

        RoutingInfo[] allRoutes = { new RoutingInfo(topologyExchange, RoutingInfo.ExchangeType.Topic, true, "#") };
        topologyEventSetRegistry.put("ALL", allRoutes);
    }

    @Override
    public RoutingInfo getRoutingInfoForEvent(Class<?> eventType) {

        LOG.info("Looking for route for event type [{}] in static topology mapper.", eventType.getCanonicalName());

        RoutingInfo route = null;
        String topic = eventType.getCanonicalName();
        if (topologyEventRegistry.containsKey(topic)) {
            route = topologyEventRegistry.get(topic);

            LOG.info("Found route [{}] in static topology mapper.", route);
        }
        return route;
    }

    @Override
    public RoutingInfo[] getRoutingInfoForNamedEventSet(String eventSetName) {

        LOG.info("Looking for routes for event set name [{}] in static topology mapper.", eventSetName);

        RoutingInfo[] routes = null;
        if (topologyEventSetRegistry.containsKey(eventSetName)) {
            routes = topologyEventSetRegistry.get(eventSetName);

            LOG.info("Found routes [{}] in static topology mapper.", routes);
        }
        return routes;
    }

    @Override
    public void start(EventManager eventManager) {
        // ignore
    }

    @Override
    public void stop() {
        // ignore
    }

}
