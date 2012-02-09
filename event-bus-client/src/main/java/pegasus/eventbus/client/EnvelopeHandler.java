package pegasus.eventbus.client;

/**
 * Interface to be implemented by classes which provide handling of raw envelopes to be used in conjunction with the {@link Subscription} constructor(s) that accept envelope handlers in lieu of event
 * handlers.
 */
public interface EnvelopeHandler {

    /**
     * This method will be invoked when a received event cannot be handed to the registered event handler or when the event handler returns {@Link EventResult.Failed} or throws an exception.
     * 
     * @param envelope
     *            The envelope representing the headers and raw byte stream of the the event.
     * @return an {@link EventResult} indicating the final disposition of the event.
     */
    EventResult handleEnvelope(Envelope envelope);

    /**
     * The EventSetName getter.
     * 
     * @return String
     */
    String getEventSetName();

    /**
     * The EventSetName setter.
     * 
     * @param String
     *            The event set name value;
     */
    void setEventSetName(String eventSetName);

}