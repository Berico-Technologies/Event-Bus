package pegasus.eventbus.policy;

public interface EventStreamRegistrar {

	void setEventBuffer(EventBuffer eventBuffer);
	
	void registerForStream(String eventSetName);

	void unregisterFromStream(String eventSetName);
}