package pegasus.eventbus.apis.servicescaffold.client;

import java.util.Map.Entry;

import pegasus.eventbus.apis.servicescaffold.ServiceStatus;
import pegasus.eventbus.client.EventHandler;
import pegasus.eventbus.client.EventResult;

public class ConsoleServiceStatusHandler implements EventHandler<ServiceStatus> {

	@SuppressWarnings("unchecked")
	public Class<? extends ServiceStatus>[] getHandledEventTypes() {
		return new Class[]{ ServiceStatus.class };
	}

	public EventResult handleEvent(ServiceStatus event) {
		
		System.out.println(event.getServiceName());
		
		System.out.println(
				String.format("\tStatus [%s], Start Time [%s], Uptime [%s], Is Running [%s]", 
				  event.getStatus(), 
				  event.getStartTime(),
				  event.getUpTime(),
				  event.isRunning()));
		
		System.out.println("\tProperties:");
		
		for(Entry<String, String> e : event.getProperties().entrySet()){
			
			System.out.println(String.format("\t\t%s = %s", e.getKey(), e.getValue()));
		}
		
		return EventResult.Handled;
	}

}
