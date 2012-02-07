//package pegasus.eventbus.amqp;
//
//import static org.junit.Assert.assertNull;
//
//import org.junit.Test;
//
//public class AmqpEventManager_PublishEnvelopeTest extends
//		AmqpEventManager_PublishEnvelopeTestBase {
//
//	@Override
//	protected void publish() {
//		manager.publish(sendEvent);
//	}
//
//	@Test 
//	public void thePublishedEnvelopeShouldNotHaveAReplyToValueAssigned(){
//		assertNull(publishedEnvelope.getReplyTo());
//	}
//}
