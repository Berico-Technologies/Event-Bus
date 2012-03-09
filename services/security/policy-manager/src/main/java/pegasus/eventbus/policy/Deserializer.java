package pegasus.eventbus.policy;

import pegasus.eventbus.client.Envelope;

public interface Deserializer {

	Object deserialize(Envelope envelope) throws Exception;
	
}
