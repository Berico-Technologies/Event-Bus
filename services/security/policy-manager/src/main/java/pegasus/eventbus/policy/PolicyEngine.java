package pegasus.eventbus.policy;

import java.util.Collection;

public interface PolicyEngine {

	void performAdjudication(Collection<EventSubmission> eventSubmissions);
	
	void setAdjudicationHandler(PolicyAdjudicator adjudicationHandler);
}