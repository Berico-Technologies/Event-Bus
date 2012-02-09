package pegasus.topology.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pegasus.eventbus.client.EventHandler;
import pegasus.eventbus.client.EventManager;
import pegasus.eventbus.client.EventResult;
import pegasus.eventbus.client.SubscriptionToken;

import pegasus.eventbus.topology.TopologyRegistry;
import pegasus.eventbus.topology.event.RegisterClient;
import pegasus.eventbus.topology.event.Registration;
import pegasus.eventbus.topology.event.UnregisterClient;
import pegasus.eventbus.topology.event.TopologyUpdate;

public class RegistrationHandler implements EventHandler<Registration> {

    protected static final Logger LOG = LoggerFactory.getLogger(RegistrationHandler.class);

    private EventManager          eventManager;
    private ClientRegistry        clientRegistry;
    private TopologyRegistry      topologyRegistry;
    private SubscriptionToken     subscriptionToken;

    public RegistrationHandler(EventManager eventManager, ClientRegistry clientRegistry, TopologyRegistry topologyRegistry) {
        this.clientRegistry = clientRegistry;
        this.topologyRegistry = topologyRegistry;
        this.eventManager = eventManager;
    }

    public void start() {
        eventManager.start();
        subscriptionToken = eventManager.subscribe(this);
    }

    public void stop() {
        eventManager.unsubscribe(subscriptionToken);
    }

    @SuppressWarnings("unchecked")
    public Class<Registration>[] getHandledEventTypes() {
        return new Class[] { RegisterClient.class, UnregisterClient.class };
    }

    public EventResult handleEvent(Registration event) {
        try {
            String eventType = event.getClass().getName();
            if (eventType.equals(RegisterClient.class.getName())) {
                // register the client
                RegisterClient registerEvent = (RegisterClient) event;

                LOG.info("Received RegisterClient event [{}]", registerEvent);

                clientRegistry.handleRegisterClientEvent(registerEvent);
                // respond with topology registry
                TopologyUpdate topologyUpdateEvent = new TopologyUpdate();
                topologyUpdateEvent.setTopologyRegistry(topologyRegistry);

                LOG.info("Responding to RegisterClient event with TopologyUpdate event [{}]", topologyUpdateEvent);

                eventManager.respondTo(event, topologyUpdateEvent);
                return EventResult.Handled;
            } else if (eventType.equals(UnregisterClient.class.getName())) {
                // unregister the client
                UnregisterClient unregisterEvent = (UnregisterClient) event;

                LOG.info("Received UnregisterClient event [{}]", unregisterEvent);

                clientRegistry.handleUnregisterClientEvent(unregisterEvent);
                return EventResult.Handled;
            } else {
                // unknown event type
                return EventResult.Failed;
            }
        } catch (Exception e) {
            return EventResult.Failed;
        }
    }
}
