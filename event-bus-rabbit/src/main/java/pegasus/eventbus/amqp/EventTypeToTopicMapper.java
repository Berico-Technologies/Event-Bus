package pegasus.eventbus.amqp;

public interface EventTypeToTopicMapper {
	String getTopicFor(Class<?> eventType);
	Class<?> getEventTypeFor(String topic);
}
