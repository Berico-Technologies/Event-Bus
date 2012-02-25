package pegasus.eventbus.examples.pingpong;

import pegasus.eventbus.amqp.AmqpConfiguration;
import pegasus.eventbus.amqp.AmqpConnectionParameters;
import pegasus.eventbus.amqp.AmqpEventManager;
import pegasus.eventbus.client.EventHandler;
import pegasus.eventbus.client.EventManager;
import pegasus.eventbus.client.EventResult;

/**
 * PingService simply fires a "Pong" event whenever
 * it receives a "Ping".
 * 
 * @author Richard Clayton (Berico Technologies)
 */
public class PingService implements EventHandler<Ping> {

	EventManager eventManager = null;
	
	/**
	 * Initialize the PingService
	 * @param eventManager EventManager instance
	 */
	public PingService(EventManager eventManager){
		
		this.eventManager = eventManager;
	}
	
	/**
	 * Get the event types handled by this listener, in this
	 * case, the "Ping" event.
	 * @return an Array of Classes with on element (Ping.class)
	 */
	@SuppressWarnings("unchecked")
	public Class<? extends Ping>[] getHandledEventTypes() {

		return new Class[]{ Ping.class };
	}

	/**
	 * Handle the Ping event by printing the event to the console,
	 * and responding with a "Pong" event.
	 * @param ping Ping event
	 * @return Always returns "Handled"
	 */
	public EventResult handleEvent(Ping ping) {
		
		System.out.println(ping);
		
		eventManager.publish(new Pong("PingService", ping));
		
		return EventResult.Handled;
	}

	/**
	 * Initialize the PingService
	 * @param args Ignored
	 */
	public static void main(String[] args){
		
		//Manually configure the EventManager
    	AmqpConfiguration config = AmqpConfiguration.getDefault(
				"ping-svc", 
				new AmqpConnectionParameters(
					"amqp://guest:guest@localhost:5672/"));
    	
    	//Initialize the EventManager
    	EventManager em = new AmqpEventManager(config);
    	
    	//Start the EventManager
    	em.start();
    	
    	//Start the ping service
    	PingService pingService = new PingService(em);
    	
    	//Register the PingService as a subscriber
    	em.subscribe(pingService);
	}
	
}
