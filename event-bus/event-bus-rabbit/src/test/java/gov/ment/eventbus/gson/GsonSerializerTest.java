package gov.ment.eventbus.gson;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import gov.ment.eventbus.amqp.Serializer;
import gov.ment.eventbus.gson.GsonSerializer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class GsonSerializerTest {

  @Parameters
  public static Collection<Object[]> testObjectsToSerialize() {

    Object[][] data =
            new Object[][] { { "Simple string", "adjfoiaj95489" },
                { "Json string", "{key: \"value\"}" }, { "TestType1", new TestType1() },
                { "Immutable Types", new ComplexImmutableType() }, };
    return Arrays.asList(data);
  }

  private Object objectToSerialize;
  private String description;

  public GsonSerializerTest(String description, Object objectToSerialize) {
    this.objectToSerialize = objectToSerialize;
    this.description = description;
  }

  @Test
  public void test() throws Exception {
    Serializer s = new GsonSerializer();
    byte[] serialized;
    Object deserializedObject;
    try {
      serialized = s.serialize(objectToSerialize);
    } catch (Exception e) {
      throw new Exception(description + " failed to serialze.", e);
    }
    try {
      deserializedObject = s.deserialize(serialized, objectToSerialize.getClass());
    } catch (Exception e) {
      throw new Exception(description + " failed to deserialze.", e);
    }
    assertTrue(description, objectToSerialize.equals(deserializedObject));
  }

  public static class TestType1 {
    private Date date = new Date(8945408342342000L);
    private String string =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890-=+_)(*&^%$#@!`~{}|\\][:\"';/.,<>?";
    private byte[] byteArray = { 53, 45, 72, 98, 0, 34, 5, 79, 80 };
    private char[] charArray = string.toCharArray();
    private int integer = 890823948;
    private long longInteger = 8945408342342357L;
    private float floatingPointNumber = 3.40282346638528860e+37f;
    private double bigFloatingPointNumber = 1.79769313486231570e+308d;
    private UUID uuid = UUID.randomUUID();
    private Map<String, String> map = new HashMap<String, String>();
    private HashMap<ComplexImmutableType, String> mapWithComplexKeys =
            new HashMap<ComplexImmutableType, String>();

    public TestType1() {
      map.put("1", "one");
      map.put("2", "two");
      map.put("3", "three");
      map.put("4", "four");
      map.put("5", "five");

      mapWithComplexKeys.put(new ComplexImmutableType(), "a");
      mapWithComplexKeys.put(new ComplexImmutableType(), "b");
    }

    public Date getDate() {
      return date;
    }

    public void setDate(Date date) {
      this.date = date;
    }

    public String getString() {
      return string;
    }

    public void setString(String string) {
      this.string = string;
    }

    public byte[] getByteArray() {
      return byteArray;
    }

    public void setByteArray(byte[] byteArray) {
      this.byteArray = byteArray;
    }

    public char[] getCharArray() {
      return charArray;
    }

    public void setCharArray(char[] charArray) {
      this.charArray = charArray;
    }

    public int getInteger() {
      return integer;
    }

    public void setInteger(int integer) {
      this.integer = integer;
    }

    public long getLongInteger() {
      return longInteger;
    }

    public void setLongInteger(long longInteger) {
      this.longInteger = longInteger;
    }

    public float getFloatingPointNumber() {
      return floatingPointNumber;
    }

    public void setFloatingPointNumber(float floatingPointNumber) {
      this.floatingPointNumber = floatingPointNumber;
    }

    public double getBigFloatingPointNumber() {
      return bigFloatingPointNumber;
    }

    public void setBigFloatingPointNumber(double bigFloatingPointNumber) {
      this.bigFloatingPointNumber = bigFloatingPointNumber;
    }

    public UUID getUuid() {
      return uuid;
    }

    public void setUuid(UUID uuid) {
      this.uuid = uuid;
    }

    public Map<String, String> getMap() {
      return map;
    }

    public void setMap(Map<String, String> map) {
      this.map = map;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      long temp;
      temp = Double.doubleToLongBits(bigFloatingPointNumber);
      result = prime * result + (int) (temp ^ (temp >>> 32));
      result = prime * result + Arrays.hashCode(byteArray);
      result = prime * result + Arrays.hashCode(charArray);
      result = prime * result + ((date == null) ? 0 : date.hashCode());
      result = prime * result + Float.floatToIntBits(floatingPointNumber);
      result = prime * result + integer;
      result = prime * result + (int) (longInteger ^ (longInteger >>> 32));
      result = prime * result + ((map == null) ? 0 : map.hashCode());
      result = prime * result + ((mapWithComplexKeys == null) ? 0 : mapWithComplexKeys.hashCode());
      result = prime * result + ((string == null) ? 0 : string.hashCode());
      result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      TestType1 other = (TestType1) obj;
      if (Double.doubleToLongBits(bigFloatingPointNumber) != Double
              .doubleToLongBits(other.bigFloatingPointNumber))
        return false;
      if (!Arrays.equals(byteArray, other.byteArray))
        return false;
      if (!Arrays.equals(charArray, other.charArray))
        return false;
      if (date == null) {
        if (other.date != null)
          return false;
      } else if (!date.equals(other.date))
        return false;
      if (Float.floatToIntBits(floatingPointNumber) != Float
              .floatToIntBits(other.floatingPointNumber))
        return false;
      if (integer != other.integer)
        return false;
      if (longInteger != other.longInteger)
        return false;
      if (map == null) {
        if (other.map != null)
          return false;
      } else if (!map.equals(other.map))
        return false;
      if (mapWithComplexKeys == null) {
        if (other.mapWithComplexKeys != null)
          return false;
      } else if (!mapWithComplexKeys.equals(other.mapWithComplexKeys))
        return false;
      if (string == null) {
        if (other.string != null)
          return false;
      } else if (!string.equals(other.string))
        return false;
      if (uuid == null) {
        if (other.uuid != null)
          return false;
      } else if (!uuid.equals(other.uuid))
        return false;
      return true;
    }
  }

  private static class ComplexImmutableType {
    private final String string = UUID.randomUUID().toString();
    private final long time = UUID.randomUUID().getMostSignificantBits();

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((string == null) ? 0 : string.hashCode());
      result = prime * result + (int) (time ^ (time >>> 32));
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      ComplexImmutableType other = (ComplexImmutableType) obj;
      if (string == null) {
        if (other.string != null)
          return false;
      } else if (!string.equals(other.string))
        return false;
      if (time != other.time)
        return false;
      return true;
    }
  }
}
