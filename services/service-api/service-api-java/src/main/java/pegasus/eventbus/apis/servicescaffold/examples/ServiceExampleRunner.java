package pegasus.eventbus.apis.servicescaffold.examples;

import pegasus.eventbus.amqp.AmqpConfiguration;
import pegasus.eventbus.amqp.AmqpConnectionParameters;
import pegasus.eventbus.amqp.AmqpEventManager;
import pegasus.eventbus.client.EventManager;

public class ServiceExampleRunner {

	public static void main(String[] args){
		
		AmqpConfiguration config = AmqpConfiguration.getDefault(
				"svc-example", 
				new AmqpConnectionParameters(
					"amqp://guest:guest@localhost:5672/"));
    	
    	EventManager em = new AmqpEventManager(config);
    	
    	em.start();
		
    	new ServiceExample(em);
	}
	
	
}
