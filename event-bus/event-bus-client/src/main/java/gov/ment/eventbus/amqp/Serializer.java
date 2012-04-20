package gov.ment.eventbus.amqp;

/**
 * Defines the requirements of a Serializer/Deserializer used within the Event
 * Bus client.
 * 
 * @author Ken Baltrinic (Berico Technologies)
 */
public interface Serializer {

  /**
   * Given an object, produce a byte array representation of it.
   * 
   * @param object
   *          Object to Serialize
   * @return byte array representation of the object
   */
  byte[] serialize(Object object);

  /**
   * Given a byte array and a class, return an instance of that class from the
   * state of the byte array.
   * 
   * @param bytes
   *          bytes to deserialize
   * @param type
   *          desired type for deserialization
   * @return instance of the class from the byte array state
   */
  <T> T deserialize(byte[] bytes, Class<? extends T> type);
}
