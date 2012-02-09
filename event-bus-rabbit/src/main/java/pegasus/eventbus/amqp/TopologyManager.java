package pegasus.eventbus.amqp;

import pegasus.eventbus.client.EventManager;

public interface TopologyManager {

    RoutingInfo getRoutingInfoForEvent(Class<?> eventType);

    RoutingInfo[] getRoutingInfoForNamedEventSet(String eventSetName);

    void start(EventManager eventManager);

    void close();

}
