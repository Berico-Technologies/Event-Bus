package pegasus.eventbus.policy;

import pegasus.eventbus.client.Envelope;
import pegasus.eventbus.client.EnvelopeHandler;
import pegasus.eventbus.client.EventResult;

public class EventStreamHandler implements EnvelopeHandler {

	protected final EventBuffer eventBuffer;
	protected final String namedEventSet;
	protected final Deserializer deserializer;

	public EventStreamHandler(
			EventBuffer eventBuffer,
			String namedEventSet, 
			Deserializer deserializer) {
		
		this.eventBuffer = eventBuffer;
		this.namedEventSet = namedEventSet;
		this.deserializer = deserializer;
	}
	
	public EventResult handleEnvelope(Envelope envelope) {
		
		try {
		
			Object deserializedObject = deserializer.deserialize(envelope);
			EventSubmission eventSubmission = new EventSubmission(envelope, deserializedObject);
			eventBuffer.addEvent(eventSubmission);
		
		} catch (Exception e){
		
			throw new RuntimeException(e);
		}
		
		return EventResult.Handled;
	}

	public String getEventSetName() {
		
		return this.namedEventSet;
	}

}
