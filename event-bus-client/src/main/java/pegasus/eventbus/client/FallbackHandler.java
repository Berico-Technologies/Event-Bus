package pegasus.eventbus.client;

/**
 * Interface to be implemented by classes which wish to provide fallback event handling services
 * 		on the occasion that an {@link EventHandler} fails to handle the event or the event could
 * 		not be deserialized or cast to the type required by the handler.
 */
public interface FallbackHandler {
	/**
	 * This method will be invoked when a received event cannot be handed to the registered event
	 * 		handler or when the event handler returns {@Link EventResult.Failed} or throws an 
	 * 		exception.
	 * @param envelope  The envelope representing the headers and raw byte stream of the the event.
	 * @param details	A {@link FallbackDetails} object that give information as to why the 
	 * 		fallback occurred.
	 * @return  an {@link EventResult} indicating the final disposition of the event.
	 */
	EventResult handleEnvelope(Envelope envelope, FallbackDetails details);
}