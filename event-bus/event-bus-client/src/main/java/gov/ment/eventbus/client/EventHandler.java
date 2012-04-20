package gov.ment.eventbus.client;

/**
 * Interface to be implemented by classes which provide event handling services.
 */
public interface EventHandler<TEvent> {

  /**
   * @return an array of all the event types which the event handler should
   *         receive. Most implementations will simply return a one element
   *         array containing TEvent.class. However, if TEvent is an abstract
   *         class or a class that has subclasses, the array must contain each
   *         concrete subclass for which the handler wishes to receive events.
   */
  Class<? extends TEvent>[] getHandledEventTypes();

  /**
   * This method will be invoked when an properly deserialized event of a
   * subscribed type is received.
   * 
   * @param event
   *          The event to be handled.
   * @returnan {@link EventResult} indicating the result of the event handling
   *           process.
   */
  EventResult handleEvent(TEvent event);
}
