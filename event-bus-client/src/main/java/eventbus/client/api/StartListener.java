package eventbus.client.api;

/**
 * Interface to be implemented by classes that listen for the {@link EventManager} start event.
 */
public interface StartListener {

    /**
     * This method will be invoked when the {@link EventManager} starts.
     */
    void onStart();

}
