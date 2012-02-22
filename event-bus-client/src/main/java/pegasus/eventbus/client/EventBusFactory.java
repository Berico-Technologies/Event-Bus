package pegasus.eventbus.client;

public interface EventBusFactory {

    public EventManager getEventManager(String clientName, EventBusConnectionParameters connectionParameters);

}
