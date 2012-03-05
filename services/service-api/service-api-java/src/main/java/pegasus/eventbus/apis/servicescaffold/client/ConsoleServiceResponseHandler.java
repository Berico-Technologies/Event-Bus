package pegasus.eventbus.apis.servicescaffold.client;

import pegasus.eventbus.apis.servicescaffold.events.ServiceResponse;
import pegasus.eventbus.client.EventHandler;
import pegasus.eventbus.client.EventResult;

public class ConsoleServiceResponseHandler implements EventHandler<ServiceResponse> {

	@SuppressWarnings("unchecked")
	public Class<? extends ServiceResponse>[] getHandledEventTypes() {
		
		return new Class[]{ ServiceResponse.class };
	}

	public EventResult handleEvent(ServiceResponse event) {
		
		System.out.println(
			String.format("%s [%s]: %s", 
			  event.getServiceId(), 
			  event.getTimestamp(), 
			  event.getStatusMessage()));
		
		return EventResult.Handled;
	}
	
}
