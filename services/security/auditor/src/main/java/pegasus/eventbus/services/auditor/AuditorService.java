package pegasus.eventbus.services.auditor;

import java.util.HashMap;
import java.util.Map;

import pegasus.eventbus.amqp.AmqpEventManager;
import pegasus.eventbus.amqp.AmqpConfiguration;
import pegasus.eventbus.amqp.ConnectionParameters;
import pegasus.eventbus.client.EventManager;
import pegasus.eventbus.client.SubscriptionToken;

/**
 * Service that logs (Audits) Event Sets using the SLF4J logging facade.
 * @author Richard Clayton (Berico Technologies)
 */
public class AuditorService 
{
	EventManager eventManager = null;
	Map<String, SubscriptionToken> tokens = new HashMap<String, SubscriptionToken>();
	
	/**
	 * Initialize the Auditor Service
	 * @param eventManager EventManager instance
	 * @param eventSets Array of Event Sets to log.
	 */
	public AuditorService(EventManager eventManager, String[] eventSets){
		
		this.eventManager = eventManager;
		
		for(String eventSet : eventSets){
			
			this.eventManager.subscribe(new Auditor(eventSet));
		}
	}
	
	/**
	 * Audit the supplied Event Set.
	 * @param eventSet Name of the Event Set.
	 */
	public void auditEventSet(String eventSet){
		
		tokens.put(
			eventSet,
			this.eventManager.subscribe(
				new Auditor(eventSet)));
	}
	
	/**
	 * Audit the supplied Event Set.
	 * @param eventSet Name of the Event Set.
	 * @param logname Identifier for the Log.
	 */
	public void auditEventSet(String eventSet, String logname){
		
		tokens.put(
			eventSet,
			this.eventManager.subscribe(
				new Auditor(eventSet, logname)));
	}
	
	/**
	 * Stop an Active Audit (if the event set is currently being watched).
	 * @param eventSet Event Set whose audit should be stopped.
	 */
	public void removeAudit(String eventSet){
		
		if(tokens.containsKey(eventSet)){
			
			this.eventManager.unsubscribe(tokens.get(eventSet));
		}
	}
	
	/**
	 * Shutdown (unregistering all auditors).
	 */
	public void shutdown(){
		
		for(SubscriptionToken token : tokens.values()){
			
			this.eventManager.unsubscribe(token);
		}
	}
	
	/**
	 * Start up the auditor service.
	 * @param args Named Event Sets to Capture to the Log.
	 */
    public static void main( String[] args )
    {	
    	
    	AmqpConfiguration config = AmqpConfiguration.getDefault(
    			"AuditorService", 
    			new ConnectionParameters("amqp://guest:guest@localhost:5672/"));
    	
    	EventManager em = new AmqpEventManager(config);
    	
    	em.start();
    	
    	new AuditorService(em, args);
    }
}
