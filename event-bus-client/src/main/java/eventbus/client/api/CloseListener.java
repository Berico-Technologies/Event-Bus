package eventbus.client.api;

/**
 * Interface to be implemented by classes that listen for the {@link EventManager} close event.
 */
public interface CloseListener {

    /**
     * This method will be invoked when the {@link EventManager} closes.
     */
    void onClose();

}
