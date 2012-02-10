package pegasus.eventbus.client;

/**
 * Interface to be implemented by classes that listen for the {@link EventManager} unsubscribe event.
 */
public interface UnsubscribeListener {

    /**
     * This method will be invoked when the {@link EventManager} unsubscribes an event.
     * 
     * @param subscription
     *            The subscription object EventManager generates to track the subscripton
     */
    void onUnsubscribe();

}
