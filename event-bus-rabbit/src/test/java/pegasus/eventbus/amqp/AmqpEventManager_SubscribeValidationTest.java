package pegasus.eventbus.amqp;

import org.junit.*;

import pegasus.eventbus.client.EventHandler;
import pegasus.eventbus.client.Subscription;
import pegasus.eventbus.testsupport.TestSendEvent;

public class AmqpEventManager_SubscribeValidationTest extends AmqpEventManager_TestBase{

	protected TestSendEvent sendEvent;
	
	protected byte[] bytesFromSerializer = {39,84,72,30,87,50,98,75,0};
	
	
	private TestEventHandler eventHandler;

	@Before
	@Override
	public void beforeEachTest() {
		
		super.beforeEachTest();

		eventHandler = new TestEventHandler(TestSendEvent.class);	
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void subscribingWithNullQueueNameShouldThrow() {
		manager.subscribe(null, eventHandler);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void subscribingWithZeroLengthQueueNameShouldThrow() {
		manager.subscribe("", eventHandler);
	}

	@Test(expected=IllegalArgumentException.class)
	public void subscribingWithNullEventHanlderShouldThrow() {
		manager.subscribe((EventHandler<?>)null);
	}

	@Test(expected=IllegalArgumentException.class)
	public void subscribingWithAQueueNameAndNullEventHanlderShouldThrow() {
		manager.subscribe("queueName", (EventHandler<?>)null);
	}

	@Test(expected=IllegalArgumentException.class)
	public void subscribingWithNullSubcriptionShouldThrow() {
		manager.subscribe((Subscription)null);
	}
}
