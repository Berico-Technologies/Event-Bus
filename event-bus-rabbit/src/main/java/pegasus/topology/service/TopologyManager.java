package pegasus.topology.service;

import pegasus.eventbus.amqp.RoutingInfo;
import pegasus.eventbus.client.EventManager;

public interface TopologyManager {

    RoutingInfo getRoutingInfoForEvent(Class<?> eventType);

    RoutingInfo[] getRoutingInfoForNamedEventSet(String eventSetName);

    void start(EventManager eventManager);

    void stop();

}
