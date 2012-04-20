package gov.ment.eventbus.topology.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.ment.eventbus.amqp.RoutingInfo;
import gov.ment.eventbus.client.EventHandler;
import gov.ment.eventbus.client.EventManager;
import gov.ment.eventbus.client.EventResult;
import gov.ment.eventbus.client.SubscriptionToken;

import gov.ment.eventbus.topology.TopologyRegistry;
import gov.ment.eventbus.topology.events.EventTypeRoutingInfo;
import gov.ment.eventbus.topology.events.GetEventTypeRoute;
import gov.ment.eventbus.topology.events.TopologyUpdate;

public class UnknownEventTypeHandler implements EventHandler<GetEventTypeRoute> {

  private static final Logger LOG = LoggerFactory.getLogger(UnknownEventTypeHandler.class);

  private EventManager eventManager;
  private TopologyRegistry topologyRegistry;
  private SubscriptionToken subscriptionToken;
  private String defaultExchangeName = "default";

  public EventManager getEventManager() {
    return eventManager;
  }

  public void setEventManager(EventManager eventManager) {
    this.eventManager = eventManager;
  }

  public TopologyRegistry getTopologyRegistry() {
    return topologyRegistry;
  }

  public void setTopologyRegistry(TopologyRegistry topologyRegistry) {
    this.topologyRegistry = topologyRegistry;
  }

  public String getDefaultExchangeName() {
    return defaultExchangeName;
  }

  public void setDefaultExchangeName(String defaultExchangeName) {
    this.defaultExchangeName = defaultExchangeName;
  }

  public void start() {
    LOG.debug("UnknownEventTypeHandler starting...");
    subscriptionToken = eventManager.subscribe(this);
    LOG.debug("UnknownEventTypeHandler started.");
  }

  public void stop() {
    LOG.debug("UnknownEventTypeHandler stopping...");
    eventManager.unsubscribe(subscriptionToken);
    LOG.debug("UnknownEventTypeHandler stopped.");
  }

  @Override
  @SuppressWarnings("unchecked")
  public Class<GetEventTypeRoute>[] getHandledEventTypes() {
    return new Class[] { GetEventTypeRoute.class };
  }

  @Override
  public EventResult handleEvent(GetEventTypeRoute event) {
    try {
      LOG.debug("Handling GetEventTypeRoute for event type: " + event.getEventTypeCanonicalName());
      String topic = event.getEventTypeCanonicalName();
      RoutingInfo route = new RoutingInfo(defaultExchangeName, topic);
      EventTypeRoutingInfo response = new EventTypeRoutingInfo(topic, route);
      LOG.trace("Sending EventTypeRoutingInfo for event type: " + event.getEventTypeCanonicalName());
      eventManager.respondTo(event, response);
      topologyRegistry.setEventRoute(topic, route);
      TopologyUpdate update = new TopologyUpdate();
      update.setTopologyRegistry(topologyRegistry);
      LOG.trace("Publishing TopologyUpdate after adding event type: "
              + event.getEventTypeCanonicalName());
      eventManager.publish(update);
      return EventResult.Handled;
    } catch (Exception e) {
      return EventResult.Failed;
    }
  }
}
