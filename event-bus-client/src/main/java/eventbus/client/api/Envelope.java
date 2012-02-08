package eventbus.client.api;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * General data structure used to represent the raw event data received form the AMQP transport.
 */
public class Envelope {

	private UUID id;
	private UUID correlationId;
	private String topic;
	private String eventType;
	private String replyTo;
	private byte[] body = {};
	private Map<String, String> headers = new HashMap<String, String>();

	public Map<String, String> getHeaders() {
		return headers;
	}
	
	protected void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}
	
	public byte[] getBody() {
		return body;
	}

	public void setBody(byte[] body) {
		this.body = body == null ? new byte[0] : body;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public UUID getCorrelationId() {
		return correlationId;
	}

	public void setCorrelationId(UUID correlationId) {
		this.correlationId = correlationId;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}
	
	public String getReplyTo() {
		return replyTo;
	}
	
	public void setReplyTo(String replyTo) {
		this.replyTo = replyTo;
	}

	public String toString() {
		String[][] fields = { 
				{ "REPLYTO", this.getReplyTo() },
				{ "EVENT_TYPE", this.getEventType() },
				{ "TOPIC", this.getTopic() },
				{ "ID", "" + this.getId() },
				{ "CORRELATION_ID", "" + this.getCorrelationId() },
		};
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
