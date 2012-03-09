package pegasus.eventbus.policy;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pegasus.eventbus.client.Envelope;

/**
 * Iterates over a collection of deserializers attempting to
 * marshall an object.  The first deserializer to not return 
 * a null object or throw an exception is the one returned to
 * the client.
 * @author Richard Clayton (Berico Technologies)
 */
public class CompositeJsonDeserializer implements Deserializer {

	private final Logger LOG = LoggerFactory.getLogger(CompositeJsonDeserializer.class);
	
	protected final Collection<Deserializer> deserializers;
	
	public CompositeJsonDeserializer(Collection<Deserializer> deserializers){
		
		this.deserializers = deserializers;
	}
	
	public Object deserialize(Envelope envelope) throws Exception {
		
		for(Deserializer deserializer : this.deserializers){
			
			try {
				
				Object value = deserializer.deserialize(envelope);
				
				if(value == null){
					
					continue;
				}
				
				return value;
				
			} catch (Exception e){
				
				LOG.error("Could not marshall object with deserializer.", e);
			}
		}
		
		return null;
	}

}
