package pegasus.eventbus.policy;

public interface PolicyManager {

	void start();
	
	void stop();
	
	void enforceOnStream(String eventSetName);
}
