package pegasus.eventbus.services.auditor;

import pegasus.eventbus.amqp.AmqpEventManager;
import pegasus.eventbus.amqp.Configuration;
import pegasus.eventbus.amqp.ConnectionParameters;
import pegasus.eventbus.client.EventHandler;
import pegasus.eventbus.client.EventManager;
import pegasus.eventbus.client.EventResult;

/**
 * 
 * @author Richard Clayton (Berico Technologies)
 */
public class AuditorService 
{
	
	EventManager eventManager = null;
	
	public AuditorService(EventManager eventManager){
		
		this.eventManager = eventManager;
		
		this.eventManager.publish("Hi Mom!");
		
		this.eventManager.subscribe(new EventHandler<String>() {

			@Override
			public Class<? extends String>[] getHandledEventTypes() {
				
				return new Class[]{ String.class };
			}

			@Override
			public EventResult handleEvent(String message) {
				
				System.out.println(message);
				
				return EventResult.Handled;
			}
			
		});
	}
	
	
    public static void main( String[] args )
    {
    	ConnectionParameters connParams = new ConnectionParameters();
    	connParams.setHost("localhost");
    	connParams.setUsername("guest");
    	connParams.setPassword("guest");
    	connParams.setPort(5672);
    	connParams.setVirtualHost("/");
    	
    	Configuration config = Configuration.getDefault("Orion:AuditorService", connParams);
    	
    	new AuditorService(new AmqpEventManager(config));
    	
        System.out.println( "Hello World!" );
    }
}
