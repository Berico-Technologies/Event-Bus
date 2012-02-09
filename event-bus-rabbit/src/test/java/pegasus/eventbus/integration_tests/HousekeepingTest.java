package pegasus.eventbus.integration_tests;

import java.io.IOException;

import org.apache.commons.httpclient.HttpException;
import org.junit.*;

public class HousekeepingTest extends IntegrationTestBase {
	@Test 
	public void publishingAnEventShouldCreateExchangeIfMissing() throws HttpException, IOException{
		
		manager.publish(sendEvent);

		assertExchangeExists();
	}

	@Test 
	public void publishingAnEventShouldNotCreateAnyQueues() throws HttpException, IOException{
		
		manager.publish(sendEvent);

		assertQueueDoesNotExists();
	}

}
