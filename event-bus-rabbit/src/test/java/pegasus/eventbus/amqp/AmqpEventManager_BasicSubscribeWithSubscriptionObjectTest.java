package pegasus.eventbus.amqp;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;

import pegasus.eventbus.client.EventHandler;
import pegasus.eventbus.client.Subscription;
import pegasus.eventbus.client.SubscriptionToken;

public class AmqpEventManager_BasicSubscribeWithSubscriptionObjectTest extends
	AmqpEventManager_BasicSubscribeTest {

	protected static final String NON_DURABLE_QUEUE_NAME = "nonDurableQueue";
	protected static final String DURABLE_QUEUE_NAME = "durableQueue";

	
	@Test
	public void unsubscribingANonDurableQueueShouldDeleteIt() throws InterruptedException{

		SubscriptionToken token = subscribeWithANonDurableQueue();
		
		manager.unsubscribe(token);
	
		verify(messageBus).deleteQueue(NON_DURABLE_QUEUE_NAME);
	}

	@Test
	public void unsubscribingANonDurableQueueWithDeleteDurableFalseShouldDeleteIt() throws InterruptedException{

		SubscriptionToken token = subscribeWithANonDurableQueue();
		
		manager.unsubscribe(token, false);
	
		verify(messageBus).deleteQueue(NON_DURABLE_QUEUE_NAME);
	}

	@Test
	public void unsubscribingANonDurableQueueWithDeleteDurableTrueShouldDeleteIt() throws InterruptedException{

		SubscriptionToken token = subscribeWithANonDurableQueue();
		
		manager.unsubscribe(token, true);
	
		verify(messageBus).deleteQueue(NON_DURABLE_QUEUE_NAME);
	}

	@Test
	public void unsubscribingADurableQueueShouldNotDeleteIt() throws InterruptedException{

		SubscriptionToken token = subscribeWithADurableQueue();
		
		manager.unsubscribe(token);
	
		verify(messageBus, never()).deleteQueue(DURABLE_QUEUE_NAME);
	}
	
	@Test
	public void unsubscribingADurableQueueWithDeleteDurableFalseShouldNotDeleteIt() throws InterruptedException{

		SubscriptionToken token = subscribeWithADurableQueue();
		
		manager.unsubscribe(token, false);
	
		verify(messageBus, never()).deleteQueue(DURABLE_QUEUE_NAME);
	}
	
	@Test
	public void unsubscribingADurableQueueWithDeleteDurableTrueShouldDeleteIt() throws InterruptedException{

		SubscriptionToken token = subscribeWithADurableQueue();
		
		manager.unsubscribe(token, true);
	
		verify(messageBus).deleteQueue(DURABLE_QUEUE_NAME);
	}
	
	@Test
	public void closingTheManagerShouldDeleteAllNonDurableQueues() throws InterruptedException{

		subscribeWithANonDurableQueue();
		
		manager.close();
	
		verify(messageBus).deleteQueue(NON_DURABLE_QUEUE_NAME);
	}

	@Test
	public void closingTheManagerTwiceShouldDeleteQueuesOnlyOnce() throws InterruptedException{

		subscribeWithANonDurableQueue();
		
		manager.close();
		manager.close();
		
		verify(messageBus, times(1)).deleteQueue(NON_DURABLE_QUEUE_NAME);
	}

	@Test
	public void closingTheManagerShouldNotDeleteAnyDurableQueues() throws InterruptedException{

		subscribeWithADurableQueue();
		
		manager.close();
	
		verify(messageBus, never()).deleteQueue(DURABLE_QUEUE_NAME);
	}

	@Override
	protected SubscriptionToken subscribe(){
		return subscribe(handler);
	}
	
	@Override
	protected SubscriptionToken subscribe(EventHandler<?> handler){
		Subscription subscription = new Subscription(handler);
		return subscribe(subscription);
	}
	
	@Override
	protected void subscribe(String queueName){
		Subscription subscription = new Subscription(handler, queueName);
		subscribe(subscription);
	}

	protected SubscriptionToken subscribeWithANonDurableQueue() {
		Subscription subscription = new Subscription(handler);
		subscription.setQueueName(NON_DURABLE_QUEUE_NAME);
		subscription.setIsDurable(false);
		
		return subscribe(subscription);
	}

	protected SubscriptionToken subscribeWithADurableQueue() {
		Subscription subscription = new Subscription(handler);
		subscription.setQueueName(DURABLE_QUEUE_NAME);
		subscription.setIsDurable(true);
		
		return subscribe(subscription);
	}

	protected SubscriptionToken subscribe(Subscription subscription){
		return manager.subscribe(subscription);
	}
}
