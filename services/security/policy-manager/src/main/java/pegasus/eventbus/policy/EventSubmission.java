package pegasus.eventbus.policy;

import pegasus.eventbus.client.Envelope;

/**
 * Represents an Event that has been submitted to 
 * the "unapproved exchange" that is waiting to be
 * adjudicated by the Policy Manager
 * @author Richard Clayton (Berico Technologies)
 */
public class EventSubmission {

	protected final Envelope envelope;
	
	protected Object event = null;
	
	protected Disposition disposition = Disposition.NotDetermined;
	
	public EventSubmission(Envelope envelope, Object deserializedObject){
		
		this.envelope = envelope;
		this.event = deserializedObject;
	}

	public Object getEvent() {
		return event;
	}

	public void setEvent(Object deserializedBody) {
		this.event = deserializedBody;
	}

	public Envelope getEnvelope() {
		return envelope;
	}

	public Disposition getDisposition() {
		return disposition;
	}

	/**
	 * Set the disposition of this Event.  There is a guard
	 * on this method that does not allow an Event to have
	 * it's disposition changed once it has been declared
	 * "Rejected".
	 * @param disposition  Disposition of the Event.
	 */
	public void setDisposition(Disposition disposition) {
		
		if(this.disposition != Disposition.Rejected){
			this.disposition = disposition;
		}
	}
}
