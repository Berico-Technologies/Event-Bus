package pegasus.eventbus.gson;

import java.io.UnsupportedEncodingException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import pegasus.eventbus.amqp.Serializer;

public class GsonSerializer implements Serializer {

	Gson gson;
	
	public GsonSerializer() {
		gson = new GsonBuilder()
			.enableComplexMapKeySerialization()
			.create();
	}
	
	@Override
	public byte[] serialize(Object object) {
		String json = gson.toJson(object);
	
		try {
			return json.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Failed to encode json as UTF-8", e);
		}
	}

	@Override
	public <T> T deserialize(byte[] bytes, Class<? extends T> type) {
		String json;
		try {
			json = new String(bytes, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Failed to dencode json as UTF-8", e);
		}
		return gson.fromJson(json, type);
	}

}
