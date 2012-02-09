package pegasus.eventbus.testsupport;

import java.util.HashMap;
import java.util.UUID;

import pegasus.eventbus.amqp.ConnectionParameters;

public class ConnectionParametersWithRandomVirtualHost extends
		ConnectionParameters {

	private String uniqueVHostSuffix = UUID.randomUUID().toString();
	
	public ConnectionParametersWithRandomVirtualHost() {
	}

	public ConnectionParametersWithRandomVirtualHost(
			HashMap<String, String> parametersMap) {
		super(parametersMap);
	}

	public ConnectionParametersWithRandomVirtualHost(String connectionParameters) {
		super(connectionParameters);
	}

	@Override 
	public String getVirtualHost(){
		return super.getVirtualHost() + "-" + uniqueVHostSuffix;
	}
	
}
