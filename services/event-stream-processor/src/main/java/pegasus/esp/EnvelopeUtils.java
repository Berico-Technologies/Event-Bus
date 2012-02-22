package pegasus.esp;

import java.util.Map;
import java.util.UUID;

import pegasus.eventbus.client.Envelope;

import com.google.common.collect.Maps;

public class EnvelopeUtils {

	public static Map<String, UUID> idMappings = Maps.newHashMap();

	/**
	 * Translate between human readable IDs and internal UUIDs.  This method finds the UUID
	 * that corresponds to the specified string and returns it.  If there isn't a UUID for
	 * that string already, then a new UUID is allocated and returned, caching the correspondence
	 * for future references of that string.
	 *
	 * @param idsymbol a string representing a unique ID, or null to create a new unique UUID
	 * @return the UUID that permanently corresponds to the string
	 */
	public static UUID symIdToRealId(String idsymbol) {
	    UUID id = null;
	
	    if (idsymbol != null) {
	        id = idMappings.get(idsymbol);
	    }
	
	    if (id == null) {
	        id = UUID.randomUUID();
	        idMappings.put(idsymbol, id);
	    }
	    return id;
	}

	public static Envelope makeEnvelope(String type, String idsymbol, String correlationIdsymbol,
	        String topic, String replyTo) {
	    Envelope e = new Envelope();
	    e.setEventType(type);
	    e.setId(symIdToRealId(idsymbol));
	    if (correlationIdsymbol != null) {
	        e.setCorrelationId(symIdToRealId(correlationIdsymbol));
	    }
	    e.setTopic(topic);
	    e.setReplyTo(replyTo);
	    return e;
	}

}
