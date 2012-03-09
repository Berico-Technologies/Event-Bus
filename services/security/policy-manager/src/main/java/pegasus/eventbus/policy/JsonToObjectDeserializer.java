package pegasus.eventbus.policy;

import pegasus.eventbus.client.Envelope;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Attempts to deserialize a JSON string to the type specified
 * in the header of the Message envelope.  If the class does
 * not exist within the runtime, an exception will be thrown.
 * @author Richard Clayton (Berico Technologies).
 */
public class JsonToObjectDeserializer implements Deserializer {

	private Gson gson;
	
	public JsonToObjectDeserializer(){
		
		gson = new GsonBuilder().enableComplexMapKeySerialization().create();
	}

	public Object deserialize(Envelope envelope) throws Exception {
		
		@SuppressWarnings("rawtypes")
		Class clazz = Class.forName(envelope.getEventType());
		
		String jsonBody = new String(envelope.getBody(), "UTF-8");
		
		@SuppressWarnings("unchecked")
		Object deserializedObject = gson.fromJson(jsonBody, clazz);
		
		return deserializedObject;
	}
	
}
