package eventbus.esp.metric;

import pegasus.eventbus.client.EventManager;

public class EventBusBroker implements Broker {

    protected EventManager eventManager;

    public EventManager getEventManager() {
        return eventManager;
    }

    public synchronized void publish(Object message) {
        eventManager.publish(message);
    }

    public void setEventManager(EventManager eventManager) {
        this.eventManager = eventManager;
        eventManager.start();
    }
}
