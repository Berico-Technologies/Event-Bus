package pegasus.eventbus.routing;

import pegasus.eventbus.amqp.EventTypeToTopicMapper;

/**
 * EventTypeToTopicMapper implementation that uses the Canonical Name of an events class as the 
 * topic.  We intent to use this implementation until and unless it proves inadequate.
 */
public class BasicTypeToTopicMapper implements EventTypeToTopicMapper {

	@Override
	public String getTopicFor(Class<?> eventType) {
		return eventType.getCanonicalName();
	}

	@Override
	public Class<?> getEventTypeFor(String topic) {
		try {
			return Class.forName(topic);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Could not determin type for topic: " + topic +". See inner exception for details.", e);
		}
	}
}
