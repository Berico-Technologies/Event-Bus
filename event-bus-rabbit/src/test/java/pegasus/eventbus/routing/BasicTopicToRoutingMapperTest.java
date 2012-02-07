//package pegasus.eventbus.routing;
//
//import static org.junit.Assert.*;
//
//import org.junit.BeforeClass;
//import org.junit.Test;
//
//import pegasus.eventbus.amqp.RoutingInfo;
//
//public class BasicTopicToRoutingMapperTest {
//
//	private static final String TOPIC = "TestTopic";
//	
//	private static RoutingInfo route;
//	
//	@BeforeClass
//	public static void onceBeforeAllTests(){
//		route = new BasicTopicToRoutingMapper().getRoutingInfoFor(TOPIC);
//	}
//	
//	@Test
//	public void routeShouldSpecifyPegasusExchange() {
//		assertEquals("pegasus", route.getExchange().getName());
//	}
//	
//	@Test
//	public void routeShouldSpecifyATopicExchange() {
//		assertEquals(RoutingInfo.ExchangeType.Topic, route.getExchange().getType());
//	}
//	
//	@Test
//	public void routeShouldSpecifyDurrableExchange() {
//		assertTrue(route.getExchange().isDurable());
//	}
//	
//	@Test
//	public void routeShouldSpecifyTopicAsRoutingKey() {
//		assertEquals(TOPIC, route.getRoutingKey());
//	}
//	
//	
//
//}
