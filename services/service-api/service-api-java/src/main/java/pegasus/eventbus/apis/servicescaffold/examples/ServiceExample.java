package pegasus.eventbus.apis.servicescaffold.examples;

import java.util.HashMap;
import java.util.Map;

import pegasus.eventbus.apis.servicescaffold.Service;
import pegasus.eventbus.client.EventManager;

public class ServiceExample extends Service {

	BackgroundTask task = new BackgroundTask();
	
	public ServiceExample(EventManager eventManager) {
		super(eventManager);
	}

	@Override
	protected void doStart() {
		
		if(!isRunning()){
			new Thread(task).start();
		}
	}

	@Override
	protected void doStop() {
		
		task.shouldContinue = false;
	}

	@Override
	public boolean isRunning() {
		
		return task.shouldContinue;
	}

	@Override
	public Map<String, String> getProperties() {
		
		Map<String, String> props = new HashMap<String, String>();
		
		props.put("Task Iteration", Long.toString(task.iteration));
		
		return props;
	}

	@Override
	protected String getCustomStatus() {
		
		return (isRunning())? "Humming along..." : "Waiting to be started.";
	}

	/**
	 * A simple example of a Background Task that runs independent of the
	 * service.
	 * @author Richard Clayton (Berico Technologies)
	 */
	public static class BackgroundTask implements Runnable {

		public boolean shouldContinue = false;
		long iteration = 0;
		
		public void run() {
			
			shouldContinue = true;
			
			while(shouldContinue){
			
				try {
					
					Thread.sleep(5000);
					
				} catch (InterruptedException e) {
					
					e.printStackTrace();
				}
				
				System.out.println(
					String.format(
						"Background Task iteration %s", 
						++iteration));
			}
		}
		
	}
	
}
