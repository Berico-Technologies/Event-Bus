package pegasus.eventbus.client;

/**
 * An enumeration that defines the values that may be returned from the {@link EventHandler#handleEvent} 
 * 		and {FallbackHandler#HandleEnvelope} methods.
 */
public enum EventResult {
	/**
	 * Indicates that the event was successfully handled.
	 */
	Handled,
	
	/**
	 * Indicates that the event could not be handled and should not be retried. 
	 */
	Failed,
	
	/**
	 * Indicates that the event could not be handled at this time.  The event will be placed at 
	 * 		the back of the queue and re-raised when it reaches the front again.
	 */
	Retry
}