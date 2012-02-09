package pegasus.eventbus.client;

/**
 * Interface to be implemented by classes that listen for the {@link EventManager} subscribe event.
 */
public interface SubscribeListener {

    /**
     * This method will be invoked when the {@link EventManager} subscribes to an event.
     * 
     * @param subscription
     *            The subscription object EventManager generates to track the subscripton
     */
    void onSubscribe(Subscription subscription);

}
