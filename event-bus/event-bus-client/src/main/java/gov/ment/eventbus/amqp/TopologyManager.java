package gov.ment.eventbus.amqp;

import gov.ment.eventbus.client.EventManager;

public interface TopologyManager {

  RoutingInfo getRoutingInfoForEvent(Class<?> eventType);

  RoutingInfo[] getRoutingInfoForNamedEventSet(String eventSetName);

  void start(EventManager eventManager);

  void close();

}
