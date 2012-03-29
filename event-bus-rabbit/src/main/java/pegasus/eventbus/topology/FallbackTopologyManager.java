package pegasus.eventbus.topology;

import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pegasus.eventbus.amqp.RoutingInfo;
import pegasus.eventbus.amqp.TopologyManager;
import pegasus.eventbus.client.EventManager;
import pegasus.eventbus.topology.event.EventTypeRoutingInfo;
import pegasus.eventbus.topology.event.GetEventTypeRoute;

//TODO: PEGA-720 This class need test coverage.
public class FallbackTopologyManager implements TopologyManager {

	   protected static final Logger LOG              = LoggerFactory.getLogger(FallbackTopologyManager.class);

	   private EventManager          eventManager;

	@Override
	public RoutingInfo getRoutingInfoForEvent(Class<?> eventType) {
		GetEventTypeRoute request = new GetEventTypeRoute(eventType.getCanonicalName());
		try {
			@SuppressWarnings("unchecked")
			EventTypeRoutingInfo response = eventManager.getResponseTo(request, 1000, EventTypeRoutingInfo.class);
			return response.getRouteInfo();
		} catch (InterruptedException e) {
			LOG.warn("Thread interrupted while waiting for route info for event type: " + eventType.getCanonicalName(), e);
		} catch (TimeoutException e) {
			LOG.warn("Timed out while waiting for route info for event type: " + eventType.getCanonicalName(), e);
		}
		return null;
	}

	@Override
	public RoutingInfo[] getRoutingInfoForNamedEventSet(String eventSetName) {
		return null;
	}

	@Override
	public void start(EventManager eventManager) {

        LOG.trace("Fallback Topology Service Manager starting.");

        this.eventManager = eventManager;
	}

	@Override
	public void close() {

	}

}
