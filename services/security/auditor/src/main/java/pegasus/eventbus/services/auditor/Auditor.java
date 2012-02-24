package pegasus.eventbus.services.auditor;

import java.io.UnsupportedEncodingException;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pegasus.eventbus.client.Envelope;
import pegasus.eventbus.client.EnvelopeHandler;
import pegasus.eventbus.client.EventResult;

/**
 * Audits an event stream (outputs events to the logger)
 * @author Richard Clayton (Berico Technologies)
 */
public class Auditor implements EnvelopeHandler {

	private static final String DQ = "\"";
	
	private final String eventSetName;
	
	public final Logger LOG;
	
	/**
	 * Initialize.
	 * @param eventSetName Name of the Event Set to Log.
	 */
	public Auditor(String eventSetName){
		this(eventSetName, eventSetName);
	}
	
	/**
	 * Initialize.
	 * @param eventSetName Name of the Event Set to Log.
	 * @param logName Name to specify in the Log.
	 */
	public Auditor(String eventSetName, String logName){
		this.eventSetName = eventSetName;
		LOG = LoggerFactory.getLogger(logName);
	}
	
	/**
	 * Get the EventSetName.
	 */
	@Override
	public String getEventSetName() {
		return this.eventSetName;
	}

	/**
	 * Handle an incoming Envelope
	 * @param envelope Inbound message.
	 * @return Envelope is always handled!
	 */
	@Override
	public EventResult handleEnvelope(Envelope envelope) {
		
		LOG.info("{ 'Headers': {}, 'Body': {} }", 
			new Object[]{
				getHeadersAsString(envelope),
				getBodyAsString(envelope)
		});

		return EventResult.Handled;
	}
	
	/**
	 * Convert Headers to a JSON String.
	 * @param env Envelope
	 * @return JSON String
	 */
	public String getHeadersAsString(Envelope env){
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("{ ");
		
		for(Entry<String, String> entry : env.getHeaders().entrySet()){
			sb.append(DQ)
			  .append(entry.getKey())
			  .append(DQ)
			  .append(": ")
			  .append(DQ)
			  .append(entry.getValue())
			  .append(DQ)
			  .append(",");
		}
		
		sb.deleteCharAt(sb.length() -1);
		
		sb.append(" }");
		
		return sb.toString();
	}
	
	/**
	 * Convert the byte array body to a String (Should be JSON).
	 * @param env Envelope
	 * @return Body as String or a message that says it could not be converted.
	 */
	public String getBodyAsString(Envelope env){
		String strBody = "'COULD NOT CONVERT TO STRING'";
		
		try {
			
			strBody = new String(env.getBody(), "UTF-8");
			
		} catch (UnsupportedEncodingException e) {
			
			LOG.error("Could not convert body to string for message with id: [{}].", env.getId());
		}
		return strBody;
	}
	
}