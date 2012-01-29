package pegasus.eventbus.amqp;

/**
 * Maps Event Types (typically canonical class name) to a Topic,
 * and vice versa.  This is necessary for finding the correct
 * routing information for publishing and subscribing to topics
 * using the object model (instead of manually specifying the topic).
 * @author Ken Baltrinic (Berico Technologies)
 */
public interface EventTypeToTopicMapper {
	
	/**
	 * Get the topic for a given class
	 * @param eventType class of a particular event
	 * @return Topic
	 */
	String getTopicFor(Class<?> eventType);
	
	/**
	 * Get the class (of an Event) given a Topic
	 * @param topic Topic
	 * @return class (of the corresponding event for that topic)
	 */
	Class<?> getEventTypeFor(String topic);
}
