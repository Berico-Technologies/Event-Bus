package pegasus.eventbus.amqp;


import org.apache.commons.lang.NotImplementedException;
import org.junit.Test;

import pegasus.eventbus.client.EnvelopeHandler;
import pegasus.eventbus.client.EventHandler;
import pegasus.eventbus.client.Subscription;
import pegasus.eventbus.client.SubscriptionToken;

public class AmqpEventManager_EnvelopeSubscribeTest 
	extends AmqpEventManager_NamedEventsetSubscribeTest {

	EnvelopeHandler envelopeHandler = new TestEnvelopeHandler();
	
	@Override 
	@Test
	public void aSubscriptionThatSpecifiesAQueueNameShouldBeDurable() {
		//This test does not apply to this envelope subscriptions.
	}

	@Override 
	@Test
	public void unsubscribingShouldSendtInteruptRequestsToAllHandlerThreadsOfThatSubscription() 
			throws Exception {
		//TODO: Refactor all test to be based on envelope handler and then test EventEnvelopeHandlerSeparately.
	}

	@Override 
	@Test
	public void unsubscribingShouldWaitForAnyHandlerThreadsForThatSubscriptionWhichAreCurrentyProcessingAnEventToCompleteProcesing() 
			throws Exception {
		//TODO: Refactor all test to be based on envelope handler and then test EventEnvelopeHandlerSeparately.
	}

	@Override 
	@Test
	public void closingTheManagerShouldSendtInteruptRequestsToAllHandlerThreadsOfThatSubscription() 
			throws Exception {
		//TODO: Refactor all test to be based on envelope handler and then test EventEnvelopeHandlerSeparately.
	}

	@Override 
	@Test
	public void closingTheManagerShouldWaitForAnyHandlerThreadsWhichAreCurrentyProcessingAnEventToCompleteProcesing() 
			throws Exception {
		//TODO: Refactor all test to be based on envelope handler and then test EventEnvelopeHandlerSeparately.
	}

	
	@Override
	protected SubscriptionToken subscribe(Subscription subscription) {
		return manager.subscribe(subscription);
	}

	@Override
	protected SubscriptionToken subscribe() {
		Subscription subscription = new Subscription(NAMED_EVENT_SET_NAME, envelopeHandler);
		return super.subscribe(subscription);
	}

	@Override
	protected SubscriptionToken subscribe(EventHandler<?> handler) {
		throw new NotImplementedException();
		//return super.subscribe(handler);
	}

	@Override
	protected void subscribe(String queueName) {
		Subscription subscription = new Subscription(NAMED_EVENT_SET_NAME, envelopeHandler);
		subscription.setQueueName(queueName);
		super.subscribe(subscription);
	}

	@Override
	protected SubscriptionToken subscribeWithANonDurableQueue() {
		Subscription subscription = new Subscription(NAMED_EVENT_SET_NAME, envelopeHandler);
		subscription.setQueueName(NON_DURABLE_QUEUE_NAME);
		subscription.setIsDurable(false);
		return super.subscribe(subscription);
	}

	@Override
	protected SubscriptionToken subscribeWithADurableQueue() {
		Subscription subscription = new Subscription(NAMED_EVENT_SET_NAME, envelopeHandler);
		subscription.setQueueName(DURABLE_QUEUE_NAME);
		subscription.setIsDurable(true);
		return super.subscribe(subscription);
	}	
}