package pegasus.eventbus.client;

/**
 * This class defined a structure for providing information to a {@link FallbackHandler} pertaining
 * 		to why the fallback occurred.
 */
public interface FallbackDetails{

	/**
	 * Gets a value indicating why the fallback handler is being invoked.
	 * @return a {@link FallbackReason} enumeration value.
	 */
	public FallbackDetails.FallbackReason getReason();
	
	/**
	 * If fallback is the result of an exception at any point in the handing process.  (i.e. during
	 * 		deserialization, during handling by the subscribed handler, etc.) getException will 
	 * 		return the exception that caused the fallback.
	 * @return
	 */
	public Exception getException();
	
	public enum FallbackReason{
		/**
		 * The event could not be deserialized.
		 */
		DeserializationError,
		
		/**
		 * The event is not of a type handled by the event handler.
		 */
		EventNotOfHandledType,
		
		/**
		 * The {@EventHandler} threw an exception while handling the event.
		 */
		EventHandlerThrewException,
		
		/**
		 * The {@EventHandler} returned {@EventResult#Failed}.
		 */
		EventHandlerReturnedFailure
	}
}