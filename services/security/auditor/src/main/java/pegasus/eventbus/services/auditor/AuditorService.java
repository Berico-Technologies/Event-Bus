package pegasus.eventbus.services.auditor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pegasus.eventbus.amqp.AmqpEventManager;
import pegasus.eventbus.amqp.Configuration;
import pegasus.eventbus.amqp.ConnectionParameters;
import pegasus.eventbus.client.EventManager;

/**
 * 
 * @author Richard Clayton (Berico Technologies)
 */
public class AuditorService 
{
	private static final Logger LOG = LoggerFactory.getLogger(AuditorService.class);
	
	EventManager eventManager = null;
	
	public AuditorService(EventManager eventManager){
		
		this.eventManager = eventManager;
		
		this.eventManager.subscribe(new AuditEventHandler());
		
		this.eventManager.publish("Hi Mom!");
	}
	
	
    public static void main( String[] args )
    {	
    	Configuration config = Configuration.getDefault(
    			"Orion:AuditorService", 
    			new ConnectionParameters("amqp://guest:guest@localhost:5672/"));
    	
    	new AuditorService(new AmqpEventManager(config));
    	
        System.out.println( "Hello World!" );
    }
}
