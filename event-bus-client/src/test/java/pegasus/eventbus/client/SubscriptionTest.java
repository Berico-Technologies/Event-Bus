package pegasus.eventbus.client;

import static org.junit.Assert.*;

import org.junit.Test;

public class SubscriptionTest {

	static final TestEventHandler nonNullEventHandler = new TestEventHandler(); 
	static final TestEnvelopeHandler nonNullEnvelopeHandler = new TestEnvelopeHandler(); 
	
	@Test(expected=IllegalArgumentException.class)
	public void creatingSubscriptionWithANullEventHandlerShouldThrow(){
		new Subscription(null);
	}

	@Test(expected=IllegalArgumentException.class)
	public void creatingSubscriptionWithANullEventHandlerWithAQueueNameShouldThrow(){
		new Subscription("name", (EventHandler<?>)null);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void creatingSubscriptionWithZeroLengthQueueNameShouldThrow(){
		new Subscription("", nonNullEventHandler);
	}

	@Test(expected=IllegalArgumentException.class)
	public void creatingSubscriptionWithZeroLengthEventSetShouldThrow(){
		new Subscription("", nonNullEnvelopeHandler);
	}

	@Test(expected=IllegalArgumentException.class)
	public void creatingSubscriptionWithNullEventSetShouldThrow(){
		new Subscription(null, nonNullEnvelopeHandler);
	}

	@Test(expected=IllegalArgumentException.class)
	public void creatingSubscriptionWithNullEnvelopeHandlerShouldThrow(){
		new Subscription("name", (EnvelopeHandler)null);
	}

	@Test(expected=IllegalArgumentException.class)
	public void settingQueueNameToZeroLengthStringShouldThrow(){
		Subscription sub = new Subscription(nonNullEventHandler);
		sub.setQueueName("");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void settingEventsetNameToZeroLengthStringForEventBasedSubscriptionShouldThrow(){
		Subscription sub = new Subscription(nonNullEventHandler);
		sub.setEventsetName("");
	}

	@Test
	public void settingEventsetNameToNullForEventBasedSubscriptionShouldNotThrow(){
		Subscription sub = new Subscription(nonNullEventHandler);
		sub.setEventsetName(null);
	}

	@Test(expected=IllegalArgumentException.class)
	public void settingEventsetNameToZeroLengthStringForEnvelopeBasedSubscriptionShouldThrow(){
		Subscription sub = new Subscription("name", nonNullEnvelopeHandler);
		sub.setEventsetName("");
	}

	@Test(expected=IllegalArgumentException.class)
	public void settingEventsetNameToNullForEnvelopeBasedSubscriptionShouldThrow(){
		Subscription sub = new Subscription("name", nonNullEnvelopeHandler);
		sub.setEventsetName(null);
	}

	@Test
	public void creatingSubscriptionWithoutAQueueNameShouldInitializeIsDurableToFalse(){
		Subscription sub = new Subscription(nonNullEventHandler);
		assertFalse(sub.getIsDurable());
	}

	@Test
	public void creatingSubscriptionWithAQueueNameShouldInitializeIsDurableToFalse(){
		Subscription sub = new Subscription("AName", nonNullEventHandler);
		assertTrue(sub.getIsDurable());
	}

	@Test(expected=IllegalArgumentException.class)
	public void settingIsDurableToTrueWithoutAQueueNameShouldThrow() {
		Subscription sub = new Subscription(nonNullEventHandler);
		assertNull(sub.getQueueName());
		sub.setIsDurable(true);
	}

	@Test
	public void settingIsDurableToTrueOrFalseWithAQueueNameShouldSucceed() {
		Subscription sub = new Subscription(nonNullEventHandler);
		sub.setQueueName("AName");
		
		assertFalse(sub.getIsDurable());
		
		sub.setIsDurable(true);
		
		assertTrue(sub.getIsDurable());
		
		sub.setIsDurable(false);
		
		assertFalse(sub.getIsDurable());
	}

	@Test
	public void settingQueueNameToNullShouldForceIsDurableToFalse() {
		Subscription sub = new Subscription("AName", nonNullEventHandler);
		
		assertTrue(sub.getIsDurable());
		
		sub.setQueueName(null);
		
		assertFalse(sub.getIsDurable());
	}
	
	private static class TestEventHandler implements EventHandler<Object> {

		@Override
		public Class<? extends Object>[] getHandledEventTypes() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public EventResult handleEvent(Object event) {
			// TODO Auto-generated method stub
			return null;
		}	
	}
	
	private static class TestEnvelopeHandler implements EnvelopeHandler{

		@Override
		public EventResult handleEnvelope(Envelope envelope) {
			// TODO Auto-generated method stub
			return null;
		}
	}
}
