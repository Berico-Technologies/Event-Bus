package pegasus.eventbus.topology.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pegasus.eventbus.amqp.RoutingInfo;
import pegasus.eventbus.client.EventHandler;
import pegasus.eventbus.client.EventManager;
import pegasus.eventbus.client.EventResult;
import pegasus.eventbus.client.SubscriptionToken;

import pegasus.eventbus.topology.TopologyRegistry;
import pegasus.eventbus.topology.events.EventTypeRoutingInfo;
import pegasus.eventbus.topology.events.GetEventTypeRoute;
import pegasus.eventbus.topology.events.TopologyUpdate;

public class UnknownEventTypeHandler implements EventHandler<GetEventTypeRoute> {

	protected static final Logger LOG = LoggerFactory.getLogger(UnknownEventTypeHandler.class);

	private EventManager eventManager;
	private TopologyRegistry topologyRegistry;
	private SubscriptionToken subscriptionToken;

	public UnknownEventTypeHandler(EventManager eventManager, TopologyRegistry topologyRegistry) {
		this.topologyRegistry = topologyRegistry;
		this.eventManager = eventManager;
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

	@SuppressWarnings("unchecked")
	public Class<GetEventTypeRoute>[] getHandledEventTypes() {
		return new Class[] { GetEventTypeRoute.class };
	}

	public EventResult handleEvent(GetEventTypeRoute event) {
		try {
			LOG.debug("Handling GetEventTypeRoute for event type: " + event.getEventTypeCanonicalName());
			String topic = event.getEventTypeCanonicalName();
			RoutingInfo route = new RoutingInfo("pegasus", topic);
			EventTypeRoutingInfo response = new EventTypeRoutingInfo(topic, route);
			LOG.trace("Sending EventTypeRoutingInfo for event type: " + event.getEventTypeCanonicalName());
			eventManager.respondTo(event, response);
			topologyRegistry.setEventRoute(topic, route);
			TopologyUpdate update = new TopologyUpdate();
			update.setTopologyRegistry(topologyRegistry);
			LOG.trace("Publishing TopologyUpdate after adding event type: " + event.getEventTypeCanonicalName());
			eventManager.publish(update);
			return EventResult.Handled;
		} catch (Exception e) {
			return EventResult.Failed;
		}
	}
}
