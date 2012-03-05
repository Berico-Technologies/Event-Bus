package pegasus.eventbus.apis.servicescaffold;

import java.util.Map;

public interface Component {

	void start();
	
	void stop();
	
	long getStartTime();
	
	String getId();
	
	boolean isRunning();
	
	Map<String, String> getProperties();
}