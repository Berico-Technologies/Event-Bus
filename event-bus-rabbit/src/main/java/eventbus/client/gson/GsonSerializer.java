package eventbus.client.gson;

import java.io.UnsupportedEncodingException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import eventbus.client.amqp.Serializer;
import eventbus.client.api.EventManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the Serializer interface used by the Event Bus client to transform Objects into JSON Strings (and then to byte arrays) and JSON Strings (originally as byte arrays) into Objects of
 * supplied type (derived from the EventHandler's "Class<? extends TEvent>[] getHandledEventTypes()" method.
 * 
 * @author Ken Baltrinic (Berico Technologies)
 * @lastModified 01/28/2012 by Richard Clayton (Berico Technologies)
 */
public class GsonSerializer implements Serializer {

    private static final Logger LOG = LoggerFactory.getLogger(GsonSerializer.class);

    private Gson                gson;

    public GsonSerializer() {

        LOG.info("Instantiating GsonSerializer.");

        gson = new GsonBuilder().enableComplexMapKeySerialization().create();

        LOG.debug("Gson object instantiated.  Is not null? = {}", gson != null);
    }

    @Override
    public void start(EventManager eventManager) {
        // do nothing - gson serializer doesn't care
    }

    @Override
    public void close() {
        // do nothing - gson serializer doesn't care
    }

    @Override
    public byte[] serialize(Object object) {

        LOG.debug("Serializing object: {}", object.getClass());

        String json = gson.toJson(object);

        LOG.trace("JSON Representation of [{}] is {}", object.getClass().getName(), json);

        byte[] serializedObject = null;

        try {

            LOG.trace("Converting JSON string into byte array.");

            serializedObject = json.getBytes("UTF-8");

        } catch (UnsupportedEncodingException e) {

            LOG.error("Failed to encoded JSON string as UTF-8 byte array.", e);

            throw new RuntimeException("Failed to encode json as UTF-8", e);
        }

        LOG.debug("Returning serialized object as byte array [{} bytes]", serializedObject.length);

        return serializedObject;
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<? extends T> type) {

        LOG.debug("Deserializing byte[] into type {}", type.getName());

        String json;

        try {

            LOG.trace("Attempting to convert byte[] to JSON string");

            json = new String(bytes, "UTF-8");

            LOG.trace("JSON string value is {}", json);
        } catch (UnsupportedEncodingException e) {

            LOG.error("Could not convert byte[] into UTF-8 string");

            throw new RuntimeException("Failed to dencode json as UTF-8", e);
        }

        LOG.trace("Deserializing JSON string into {}", type.getName());

        T deserializedObject = gson.fromJson(json, type);

        LOG.debug("Deserialized object is not null? = {}", deserializedObject != null);

        return deserializedObject;
    }

}
