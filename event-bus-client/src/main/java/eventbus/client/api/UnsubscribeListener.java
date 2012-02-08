package eventbus.client.api;

/**
 * Interface to be implemented by classes that listen for the {@link EventManager} unsubscribe event.
 */
public interface UnsubscribeListener {

    /**
     * This method will be invoked when the {@link EventManager} unsubscribes an event.
     */
    void onUnsubscribe();

}
