package pegasus.eventbus.policy;

import java.util.Collection;

public interface EventBuffer {

	void addEvent(EventSubmission event);
	
	Collection<EventSubmission> drain(int maxCapacity);
	
}
