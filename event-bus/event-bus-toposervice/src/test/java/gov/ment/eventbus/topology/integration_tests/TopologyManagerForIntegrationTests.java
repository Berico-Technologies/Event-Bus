package gov.ment.eventbus.topology.integration_tests;

import gov.ment.eventbus.amqp.RoutingInfo;
import gov.ment.eventbus.amqp.TopologyManager;
import gov.ment.eventbus.client.EventManager;

public class TopologyManagerForIntegrationTests implements TopologyManager {

  @Override
  public RoutingInfo getRoutingInfoForEvent(Class<?> eventType) {
    return new RoutingInfo("test", RoutingInfo.ExchangeType.Topic, true,
            eventType.getCanonicalName());
  }

  @Override
  public RoutingInfo[] getRoutingInfoForNamedEventSet(String eventSetName) {
    RoutingInfo[] routes = { new RoutingInfo("test", RoutingInfo.ExchangeType.Topic, true, "#") };
    return routes;
  }

  @Override
  public void start(EventManager eventManager) {

  }

  @Override
  public void close() {

  }

}
