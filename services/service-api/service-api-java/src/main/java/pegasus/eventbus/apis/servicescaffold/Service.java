package pegasus.eventbus.apis.servicescaffold;

import java.util.Map;
import java.util.UUID;

import pegasus.eventbus.apis.servicescaffold.events.ServiceRequest;
import pegasus.eventbus.apis.servicescaffold.events.ServiceResponse;
import pegasus.eventbus.client.EventHandler;
import pegasus.eventbus.client.EventManager;
import pegasus.eventbus.client.EventResult;

/**
 * A very simple Service abstraction that allows the service to be
 * commanded from the Event Bus.
 * @author Richard Clayton (Berico Technologies)
 */
public abstract class Service implements EventHandler<ServiceRequest> {

	protected EventManager eventManager;
	protected long startTime = -1l;
	protected UUID serviceInstanceId = UUID.randomUUID();
	
	/**
	 * Instantiate the Service, supplying the EventManager
	 * @param eventManager EventManager instance
	 */
	public Service(EventManager eventManager){
		
		this.eventManager = eventManager;
		
		this.eventManager.subscribe(this);
	}
	
	/**
	 * Do whatever is necessary to start your service.
	 */
	protected abstract void doStart();
	
	/**
	 * Do whatever is necessary to stop your service.
	 * Please block any threads if necessary, ensuring
	 * the service is actually shutdown before the 
	 * method returns execution.
	 */
	protected abstract void doStop();
	
	/**
	 * Is the service running?
	 * @return true if it is running.
	 */
	public abstract boolean isRunning();
	
	/**
	 * Provide any arbitrary properties that may have been
	 * set for the service.
	 * @return At least an empty map please!
	 */
	public abstract Map<String, String> getProperties();
	
	/**
	 * Arbitrary status (up to you).
	 * @return A message you want to have displayed detailing status.
	 */
	protected abstract String getCustomStatus();
	
	/**
	 * The unique Service name for this instance.  This is 
	 * the class name plus a GUID by default.  You can override
	 * it if desired, but please ensure it is unique on the bus.
	 * @return Service Name.
	 */
	public String getServiceId(){
		
		return String.format(
			"%s:%s", 
			this.getClass().getName(), 
			serviceInstanceId.toString());
	}
	
	/**
	 * How long has the service been running?
	 * @return Current time minus start time, or zero if service is stopped
	 */
	protected long uptime(){
		return (isRunning())? System.currentTimeMillis() - this.startTime : 0l;
	}
	
	/**
	 * Called when a start request is received by the EventHandler
	 */
	public void start(){
		
		System.out.println("Starting");
		
		this.startTime = System.currentTimeMillis();
		
		this.doStart();
		
		publishServiceResponse("Service Started.");
	}
	
	/**
	 * Called when the stop event is received by the EventHandler
	 */
	public void stop(){
		
		this.doStop();
		
		if(isRunning()){
			
			publishServiceResponse("Service told to stop, but is still running (perhaps is waiting on a thread).");
		}
		else {
			
			publishServiceResponse("Service Stopped.");
		}
	}
	
	/**
	 * Called when the status event is received by the EventHandler
	 */
	public ServiceStatus getStatus(){
		
		ServiceStatus status = new ServiceStatus(
				getServiceId(),
				this.startTime, 
				uptime(), 
				isRunning(),
				getProperties(),
				getCustomStatus());
		
		this.eventManager.publish(status);
		
		return status;
	}
	
	/**
	 * Publish a response message
	 * @param message Message to publish back to anyone that cares.
	 */
	protected void publishServiceResponse(String message){
		
		System.out.println("Publishing");
		
		this.eventManager.publish(
				new ServiceResponse(getServiceId(), message));
	}
	
	/**
	 * Service handles ServiceRequest's
	 * @return Array of classes this service handles from the Bus
	 */
	@SuppressWarnings("unchecked")
	public Class<? extends ServiceRequest>[] getHandledEventTypes() {
		return new Class[]{ ServiceRequest.class };
	}

	/**
	 * Handle an incoming ServiceRequest
	 * @param request ServiceRequest to handle
	 * @return Always handled.
	 */
	public EventResult handleEvent(ServiceRequest request) {
		
		if(this.getServiceId().matches(
					request.getServiceIdOrPattern())){
		
			System.out.println(request.getAction());
			
			switch(request.getAction()){
				case Start: 
						start();
					break;
				case Stop: 
						stop();
					break;
				default: 
						getStatus();
					break;
			}
		}
		
		return EventResult.Handled;
	}
	
	
}
