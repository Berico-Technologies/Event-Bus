package gov.ment.eventbus.client;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * General data structure used to represent the raw event data received form the
 * AMQP transport.
 */
public class Envelope {

  private UUID id;
  private UUID correlationId;
  private String topic;
  private String eventType;
  private String replyTo;
  private Date timestamp;
  private byte[] body = {};
  private Map<String, String> headers = new HashMap<String, String>();

  public byte[] getBody() {
    return body;
  }

  public UUID getCorrelationId() {
    return correlationId;
  }

  public String getEventType() {
    return eventType;
  }

  public Map<String, String> getHeaders() {
    return headers;
  }

  public UUID getId() {
    return id;
  }

  public String getReplyTo() {
    return replyTo;
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public String getTopic() {
    return topic;
  }

  public void setBody(byte[] body) {
    this.body = body == null ? new byte[0] : body;
  }

  public void setCorrelationId(UUID correlationId) {
    this.correlationId = correlationId;
  }

  public void setEventType(String eventType) {
    this.eventType = eventType;
  }

  protected void setHeaders(Map<String, String> headers) {
    this.headers = headers;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public void setReplyTo(String replyTo) {
    this.replyTo = replyTo;
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }

  public void setTopic(String topic) {
    this.topic = topic;
  }

  @Override
  public String toString() {
    String[][] fields =
            { { "REPLYTO", getReplyTo() }, { "EVENT_TYPE", getEventType() },
                { "TOPIC", getTopic() }, { "ID", "" + getId() },
                { "CORRELATION_ID", "" + getCorrelationId() }, };
    StringBuffer sb = new StringBuffer("Envelope[");
    String sep = "";
    for (String[] key : fields) {
      String name = key[0];
      String val = key[1];
      if (val != null) {
        sb.append(sep + name + "=" + val);
        sep = ",";
      }
    }
    sb.append("]");
    return sb.toString();
  }
}
