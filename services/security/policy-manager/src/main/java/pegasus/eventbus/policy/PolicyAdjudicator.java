package pegasus.eventbus.policy;

public interface PolicyAdjudicator {

	void approve(EventSubmission event);
	
	void reject(EventSubmission event);
	
	void wait(EventSubmission event, long timeToWaitInMs);
}
