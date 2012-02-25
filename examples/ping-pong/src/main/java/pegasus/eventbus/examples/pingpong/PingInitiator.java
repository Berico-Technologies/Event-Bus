package pegasus.eventbus.examples.pingpong;

import pegasus.eventbus.amqp.AmqpConfiguration;
import pegasus.eventbus.amqp.AmqpConnectionParameters;
import pegasus.eventbus.amqp.AmqpEventManager;
import pegasus.eventbus.client.EventManager;

/**
 * Initiates the "Ping-Pong" sequence between the PingService and PongService.
 * @author Richard Clayton (Berico Technologies)
 */
public class PingInitiator {

	/**
	 * Instantiates the Event Manager, fires a "Ping" message onto the
	 * bus, and then closes the Event Manager.
	 * @param args Ignored
	 */
	public static void main(String[] args) {
		
    	AmqpConfiguration config = AmqpConfiguration.getDefault(
				"ping-initiator", 
				new AmqpConnectionParameters(
					"amqp://guest:guest@localhost:5672/"));
    	
    	EventManager em = new AmqpEventManager(config);
    	
    	em.start();
    	
    	em.publish(new Ping("Ping Initiator"));
    	
    	em.close();
	}

}
