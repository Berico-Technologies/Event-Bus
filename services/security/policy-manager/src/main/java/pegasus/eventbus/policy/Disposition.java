package pegasus.eventbus.policy;

public enum Disposition {
	
	/**
	 * The Event has never been seen by the Event Bus.
	 */
	NotDetermined,
	
	/**
	 * Event is approved by the Policy Manager for 
	 * dissemination on the greater Event Bus.
	 */
	Approved,
	
	/**
	 * Event was rejected by the Policy Manager and
	 * will be removed from the Event Bus.
	 */
	Rejected,
	
	/**
	 * Event will not be approved on this pass,
	 * requeue the event on the Event Bus.
	 */
	Postponed
}
