package pegasus.eventbus.testsupport;

import java.util.Dictionary;
import java.util.UUID;

import pegasus.eventbus.amqp.AmqpConnectionParameters;

public class ConnectionParametersWithRandomVirtualHost extends
		AmqpConnectionParameters {

	private String uniqueVHostSuffix = UUID.randomUUID().toString();
	
	public ConnectionParametersWithRandomVirtualHost() {
	}

	public ConnectionParametersWithRandomVirtualHost(
			Dictionary<String, String> parametersMap) {
		super(parametersMap);
	}

	public ConnectionParametersWithRandomVirtualHost(String connectionParameters) {
		super(connectionParameters);
	}

	@Override 
	public String getVHost(){
		return super.getVHost() + "-" + uniqueVHostSuffix;
	}
	
}
