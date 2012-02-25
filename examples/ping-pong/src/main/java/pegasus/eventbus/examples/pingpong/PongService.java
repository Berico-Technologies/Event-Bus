package pegasus.eventbus.examples.pingpong;

import pegasus.eventbus.amqp.AmqpConfiguration;
import pegasus.eventbus.amqp.AmqpConnectionParameters;
import pegasus.eventbus.amqp.AmqpEventManager;
import pegasus.eventbus.client.EventHandler;
import pegasus.eventbus.client.EventManager;
import pegasus.eventbus.client.EventResult;

/**
 * PongService simply fires a "Ping" event whenever
 * it receives a "Pong".
 * 
 * @author Richard Clayton (Berico Technologies)
 */
public class PongService implements EventHandler<Pong> {

	EventManager eventManager = null;
	
	/**
	 * Initialize the PongService
	 * @param eventManager EventManager instance
	 */
	public PongService(EventManager eventManager){
		
		this.eventManager = eventManager;
	}
	
	/**
	 * Get the event types handled by this listener, in this
	 * case, the "Pong" event.
	 * @return an Array of Classes with on element (Pong.class)
	 */
	@SuppressWarnings("unchecked")
	public Class<? extends Pong>[] getHandledEventTypes() {
		
		return new Class[]{ Pong.class };
	}

	/**
	 * Handle the Pong event by printing the event to the console,
	 * and responding with a "Ping" event.
	 * @param pong Pong event
	 * @return Always returns "Handled"
	 */
	public EventResult handleEvent(Pong pong) {
		
		System.out.println(pong);
		
		eventManager.publish(new Ping("PongService", pong));
		
		return EventResult.Handled;
	}

	/**
	 * Initialize the PongService
	 * @param args Ignored
	 */
	public static void main(String[] args){
		
		//Manually configure the EventManager
    	AmqpConfiguration config = AmqpConfiguration.getDefault(
				"pong-svc", 
				new AmqpConnectionParameters(
					"amqp://guest:guest@localhost:5672/"));
    	
    	//Initialize the EventManager
    	EventManager em = new AmqpEventManager(config);
    	
    	//Start the EventManager
    	em.start();
    	
    	//Start the pong service
    	PongService pongService = new PongService(em);
    	
    	//Register the PongService as a subscriber
    	em.subscribe(pongService);
	}
}
