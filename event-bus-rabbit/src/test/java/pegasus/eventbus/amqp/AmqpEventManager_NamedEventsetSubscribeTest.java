//package pegasus.eventbus.amqp;
//
//import pegasus.eventbus.client.Subscription;
//import pegasus.eventbus.client.SubscriptionToken;
//
//public class AmqpEventManager_NamedEventsetSubscribeTest extends
//	AmqpEventManager_BasicSubscribeWithSubscriptionObjectTest {
//
//	@Override
//	protected SubscriptionToken subscribe(Subscription subscription) {
//		subscription.setEventsetName(NAMED_EVENT_SET_NAME);
//		return super.subscribe(subscription);
//	}
//
//	@Override
//	protected RoutingInfo[] getExpectedRoutes() {
//		return routesForNamedEventSet;
//	}
//}
