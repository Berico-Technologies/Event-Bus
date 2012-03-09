package pegasus.eventbus.services.auditor;

import pegasus.eventbus.client.EventHandler;
import pegasus.eventbus.client.EventResult;

public class AuditEventHandler implements EventHandler<String> {

	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends String>[] getHandledEventTypes() {
		
		return new Class[]{ String.class };
	}

	@Override
	public EventResult handleEvent(String event) {
		
		System.out.println("GOT EVENT: " + event);
		
		return EventResult.Handled;
	}

}
