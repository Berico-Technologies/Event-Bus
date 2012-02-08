package eventbus.client.amqp;

import eventbus.client.api.EventManager;

public interface TopologyManager {

    RoutingInfo getRoutingInfoForEvent(Class<?> eventType);

    RoutingInfo[] getRoutingInfoForNamedEventSet(String eventSetName);

    void start(EventManager eventManager);

    void close();

}
