package eventbus.client.api;

/**
 * Interface to be implemented by classes that listen for the {@link EventManager} subscribe event.
 */
public interface SubscribeListener {

    /**
     * This method will be invoked when the {@link EventManager} subscribes to an event.
     */
    void onSubscribe();

}
