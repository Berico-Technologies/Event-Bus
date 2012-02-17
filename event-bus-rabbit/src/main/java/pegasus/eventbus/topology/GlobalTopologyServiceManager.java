package pegasus.eventbus.topology;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pegasus.eventbus.amqp.RoutingInfo;
import pegasus.eventbus.amqp.TopologyManager;
import pegasus.eventbus.topology.event.RegisterClient;
import pegasus.eventbus.topology.event.TopologyUpdate;
import pegasus.eventbus.topology.event.UnregisterClient;

import pegasus.eventbus.client.EventHandler;
import pegasus.eventbus.client.EventManager;
import pegasus.eventbus.client.EventResult;
import pegasus.eventbus.client.SubscriptionToken;

public class GlobalTopologyServiceManager implements TopologyManager {

    protected static final Logger LOG              = LoggerFactory.getLogger(GlobalTopologyServiceManager.class);

    private TopologyRegistry      topologyRegistry = new TopologyRegistry();
    private String                clientName;
    private EventManager          eventManager;
    private SubscriptionToken     subscriptionToken;

    public GlobalTopologyServiceManager(String clientName) {
        this.clientName = clientName;
    }

    @Override
    public void start(EventManager eventManager) {

        LOG.trace("Global Topology Service Manager starting.");

        this.eventManager = eventManager;

        LOG.trace("Subscribing to topology update events.");

        subscriptionToken = eventManager.subscribe(new TopologyUpdateHandler());

        RegisterClient registerClientEvent = new RegisterClient(clientName, topologyRegistry.getVersion());
        try {

            LOG.trace("Registering client {} with Global Topology Service.", clientName);

            @SuppressWarnings("unchecked")
            TopologyUpdate topologyUpdateResponseEvent = eventManager.getResponseTo(registerClientEvent, 30000, TopologyUpdate.class);
            topologyRegistry = topologyUpdateResponseEvent.getTopologyRegistry();
        } catch (Exception e) {

            LOG.error("Error starting Global Topology Service.", e);

        }
    }

    @Override
    public void close() {
        LOG.trace("Global Topology Service Manager closing.");
        eventManager.unsubscribe(subscriptionToken);
        LOG.trace("Unsubscribed TopologyUpdateHandler.");
        UnregisterClient unregisterClientEvent = new UnregisterClient(clientName);
        LOG.trace("Unregistered client.");
        eventManager.publish(unregisterClientEvent);
        LOG.trace("Global Topology Service Manager closed.");
    }

    @Override
    public RoutingInfo getRoutingInfoForEvent(Class<?> eventType) {

        LOG.trace("Looking for route for event type [{}] in global topology service.", eventType.getCanonicalName());

        RoutingInfo route = null;
        String topic = eventType.getCanonicalName();
        if (topologyRegistry.hasEventRoute(topic)) {
            route = topologyRegistry.getEventRoute(topic);

            LOG.trace("Found route [{}] in global topology service.", route);
        }
        return route;
    }

    @Override
    public RoutingInfo[] getRoutingInfoForNamedEventSet(String eventSetName) {

        LOG.trace("Looking for routes for event set name [{}] in global topology service.", eventSetName);

        RoutingInfo[] routes = null;
        if (topologyRegistry.hasEventSetRoutes(eventSetName)) {
            routes = topologyRegistry.getEventSetRoutes(eventSetName);

            LOG.trace("Found routes [{}] in global topology service.", routes);
        }
        return routes;
    }

    public class TopologyUpdateHandler implements EventHandler<TopologyUpdate> {

        @SuppressWarnings("unchecked")
        @Override
        public Class<? extends TopologyUpdate>[] getHandledEventTypes() {
            return new Class[] { TopologyUpdate.class };
        }

        @Override
        public EventResult handleEvent(TopologyUpdate event) {

            LOG.trace("Received topology update.");

            topologyRegistry = event.getTopologyRegistry();
            return EventResult.Handled;
        }

    }

}
