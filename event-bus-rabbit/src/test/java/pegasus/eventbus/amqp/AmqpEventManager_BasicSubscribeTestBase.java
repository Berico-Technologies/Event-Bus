package pegasus.eventbus.amqp;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.*;
import org.mockito.*;

import pegasus.eventbus.client.SubscriptionToken;
import pegasus.eventbus.testsupport.TestResponseEvent;
import pegasus.eventbus.testsupport.TestSendEvent;
import pegasus.eventbus.testsupport.TestSendEvent2;

public abstract class AmqpEventManager_BasicSubscribeTestBase extends AmqpEventManager_TestBase{

	protected TestEventHandler handler;

	@Before
	@Override
	public void beforeEachTest() {
		
		super.beforeEachTest();

		handler = new TestEventHandler(TestSendEvent.class, TestSendEvent2.class, TestResponseEvent.class);
	}
	
	protected SubscriptionToken subscribe() {
		return manager.subscribe(handler);
	}

	protected void unsubscribe(SubscriptionToken token) {
		manager.unsubscribe(token);
	}
	
	@Test 
	public void subscribingToMultipleEventsShouldCreateAllNeededExchanges() {
		
		subscribe();

		for(RoutingInfo routingInfo : getExpectedRoutes()){
			verify(messageBus, times(1)).createExchange(routingInfo.getExchange());
		}
	}
	
	@Test 
	public void subscribingToMultipleEventsShouldCreateASignleQueue(){
		subscribe();
		verify(messageBus, times(1)).createQueue(anyString(), any(RoutingInfo[].class), anyBoolean());
	}
	
	@Test 
	public void subscribingWithoutSpecifyingAQueueNameShouldUseARandomQueueName(){
		//Really we are just asserting that multiple subscribes generate different queueNames.
		
		subscribe();
		subscribe();
		
		ArgumentCaptor<String> queueNameCaptor = ArgumentCaptor.forClass(String.class);
		
		verify(messageBus, times(2)).createQueue(queueNameCaptor.capture(), any(RoutingInfo[].class), anyBoolean());
		
		List<String> queueNames = queueNameCaptor.getAllValues();
		assertFalse(queueNames.get(0) == queueNames.get(1));  
	}

	@Test 
	public void subscribingWithoutSpecifyingAQueueNameShouldUseAQueuePrefixedWithTheClientName(){
		
		subscribe();
		
		String createdQueueName = getCreatedQueueName();
		assertTrue(
				String.format("Created queue name %s did not start with the expected prefix %s.", createdQueueName, clientName),
				createdQueueName.startsWith(clientName));  
	}

	@Test 
	public void subscribingToMultipleEventsShouldCreateMultipleBindingsOnThatQueue(){

		subscribe();

		ArgumentCaptor<RoutingInfo[]> routingCaptor = ArgumentCaptor.forClass(RoutingInfo[].class);
		
		verify(messageBus).createQueue(
				anyString(), 
				routingCaptor.capture(), 
				anyBoolean());
		
		assertArrayEquals(getExpectedRoutes(), routingCaptor.getValue());
	}
	
	protected RoutingInfo[] getExpectedRoutes() {
		// TODO: Make expected route list based on handler.getHandledTypes();
		RoutingInfo[] expectedRoutes = {routingInfo, routingInfo2, returnRoutingInfo};
		for(int i = 0; i < expectedRoutes.length; i++){
			
			expectedRoutes[i] = new RoutingInfo(
					expectedRoutes[i].getExchange(),
					expectedRoutes[i].routingKey + AmqpEventManager.AMQP_ROUTE_SEGMENT_DELIMITER + getRouteSuffix());
		}
		return expectedRoutes;
	}

	protected abstract String getRouteSuffix();

	@Test 
	public void aSubscriptionThatDoesNotSpecifyAQueueNameShouldNotBeDurable(){

		subscribe();
		ArgumentCaptor<Boolean> durrabilityCaptor = ArgumentCaptor.forClass(boolean.class);
		verify(messageBus, times(1)).createQueue(anyString(), any(RoutingInfo[].class), durrabilityCaptor.capture());
		assertFalse(durrabilityCaptor.getValue());
	}
	
	@Test
	public void subscribingWithoutAQueueNameShouldCauseTheRelatedQueueToBePolled() throws InterruptedException{
		subscribe();

		String queueName = getCreatedQueueName();
		
		Thread.sleep(100);
		
		verify(messageBus, atLeastOnce()).getNextMessageFrom(queueName);
	}
	
	@Test
	public void unsubscribingShouldStopPollingOnThatQueue() throws InterruptedException{

		SubscriptionToken token = subscribe();

		String queueName = getCreatedQueueName();
		
		unsubscribe(token);
		
		reset(messageBus);
		
		Thread.sleep(100);
		
		verify(messageBus, never()).getNextMessageFrom(queueName);
	}
	
	@Test
	public void unsubscribingShouldNotStopPollingOnOtherQueues() throws InterruptedException{

		subscribe();

		String queueName1 = getCreatedQueueName();
		
		SubscriptionToken token2 = subscribe();

		unsubscribe(token2);
		
		reset(messageBus);
		
		Thread.sleep(100);
		
		verify(messageBus, atLeastOnce()).getNextMessageFrom(queueName1);
	}
	
	
	@Test
	public void unsubscribingASubscriptionSubscribedWithoutAQueueNameShouldDeleteTheQueue() throws InterruptedException{

		SubscriptionToken token = subscribe();

		String queueName = getCreatedQueueName();
		
		unsubscribe(token);
	
		verify(messageBus).deleteQueue(queueName);
	}
	@Test
	public void closingTheManagerShouldStopAllPollingOnQueues() throws InterruptedException{

		subscribe();

		manager.close();
		
		reset(messageBus);
		
		Thread.sleep(100);
		
		verify(messageBus, never()).getNextMessageFrom(anyString());
	}
}
