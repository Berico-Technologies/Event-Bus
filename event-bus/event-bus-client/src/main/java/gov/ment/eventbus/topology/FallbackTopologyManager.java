package gov.ment.eventbus.topology;

import java.util.concurrent.TimeoutException;

import gov.ment.eventbus.amqp.RoutingInfo;
import gov.ment.eventbus.amqp.TopologyManager;
import gov.ment.eventbus.topology.events.EventTypeRoutingInfo;
import gov.ment.eventbus.topology.events.GetEventTypeRoute;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.ment.eventbus.client.EventManager;

//TODO: This class need test coverage.
public class FallbackTopologyManager implements TopologyManager {

  private static final Logger LOG = LoggerFactory.getLogger(FallbackTopologyManager.class);

  private EventManager eventManager;

  @Override
  public RoutingInfo getRoutingInfoForEvent(Class<?> eventType) {
    GetEventTypeRoute request = new GetEventTypeRoute(eventType.getCanonicalName());
    try {
      @SuppressWarnings("unchecked")
      EventTypeRoutingInfo response =
              eventManager.getResponseTo(request, 1000, EventTypeRoutingInfo.class);
      return response.getRouteInfo();
    } catch (InterruptedException e) {
      LOG.warn(
              "Thread interrupted while waiting for route info for event type: "
                      + eventType.getCanonicalName(), e);
    } catch (TimeoutException e) {
      LOG.warn(
              "Timed out while waiting for route info for event type: "
                      + eventType.getCanonicalName(), e);
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
