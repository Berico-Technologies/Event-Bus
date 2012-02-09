package pegasus.eventbus.amqp;

public class AmqpEventManager_NamedEventsetSubscribeTest extends
	AmqpEventManager_BasicSubscribeWithSubscriptionObjectTest {

	@Override
	protected RoutingInfo[] getExpectedRoutes() {
		return routesForNamedEventSet;
	}
}
