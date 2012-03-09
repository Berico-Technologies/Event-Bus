package pegasus.eventbus.policy;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import pegasus.eventbus.client.Envelope;

/**
 * Deserializes JSON strings into a Navigatable Tree (literally
 * a Jackson "JsonNode" object).
 * @author Richard Clayton (Berico Technologies).
 */
public class JsonToJsonNodeDeserializer implements Deserializer {

	protected final ObjectMapper mapper;
	
	public JsonToJsonNodeDeserializer(){
		
		this.mapper = new ObjectMapper();
	}
	
	public Object deserialize(Envelope envelope) throws Exception {
		
		JsonNode rootNode = mapper.readTree(envelope.getBody());
		
		return rootNode;
	}

}
