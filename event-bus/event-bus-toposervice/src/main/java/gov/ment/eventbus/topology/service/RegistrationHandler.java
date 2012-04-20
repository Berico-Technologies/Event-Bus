package gov.ment.eventbus.topology.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.ment.eventbus.client.EventHandler;
import gov.ment.eventbus.client.EventManager;
import gov.ment.eventbus.client.EventResult;
import gov.ment.eventbus.client.SubscriptionToken;

import gov.ment.eventbus.topology.TopologyRegistry;
import gov.ment.eventbus.topology.events.RegisterClient;
import gov.ment.eventbus.topology.events.Registration;
import gov.ment.eventbus.topology.events.TopologyUpdate;
import gov.ment.eventbus.topology.events.UnregisterClient;

public class RegistrationHandler implements EventHandler<Registration> {

  private static final Logger LOG = LoggerFactory.getLogger(RegistrationHandler.class);

  private EventManager eventManager;
  private ClientRegistry clientRegistry;
  private TopologyRegistry topologyRegistry;
  private SubscriptionToken subscriptionToken;

  public EventManager getEventManager() {
    return eventManager;
  }

  public void setEventManager(EventManager eventManager) {
    this.eventManager = eventManager;
  }

  public ClientRegistry getClientRegistry() {
    return clientRegistry;
  }

  public void setClientRegistry(ClientRegistry clientRegistry) {
    this.clientRegistry = clientRegistry;
  }

  public TopologyRegistry getTopologyRegistry() {
    return topologyRegistry;
  }

  public void setTopologyRegistry(TopologyRegistry topologyRegistry) {
    this.topologyRegistry = topologyRegistry;
  }

  public void start() {
    subscriptionToken = eventManager.subscribe(this);
  }

  public void stop() {
    eventManager.unsubscribe(subscriptionToken);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Class<Registration>[] getHandledEventTypes() {
    return new Class[] { RegisterClient.class, UnregisterClient.class };
  }

  @Override
  public EventResult handleEvent(Registration event) {
    try {
      String eventType = event.getClass().getName();
      if (eventType.equals(RegisterClient.class.getName())) {
        // register the client
        RegisterClient registerEvent = (RegisterClient) event;

        LOG.info("Received RegisterClient event [{}]", registerEvent);

        clientRegistry.registerClient(registerEvent);
        // respond with topology registry
        TopologyUpdate topologyUpdateEvent = new TopologyUpdate();
        topologyUpdateEvent.setTopologyRegistry(topologyRegistry);

        LOG.info("Responding to RegisterClient event with TopologyUpdate event [{}]",
                topologyUpdateEvent);

        eventManager.respondTo(event, topologyUpdateEvent);
        return EventResult.Handled;
      } else if (eventType.equals(UnregisterClient.class.getName())) {
        // unregister the client
        UnregisterClient unregisterEvent = (UnregisterClient) event;

        LOG.info("Received UnregisterClient event [{}]", unregisterEvent);

        clientRegistry.unregisterClient(unregisterEvent);
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
