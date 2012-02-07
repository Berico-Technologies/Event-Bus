//package pegasus.eventbus.rabbitmq;
//
//import static org.junit.Assert.assertArrayEquals;
//
//import org.junit.Test;
//
//import pegasus.eventbus.amqp.RoutingInfo;
//
//public class RabbitMessageBus_HousekeepingsTest extends RabbitMessageBus_TestBase {
//
//	@Test
//	public void createExchangeShouldCreateTheExchange() {
//		rabbitBus.createExchange(new RoutingInfo.Exchange("TestExchange", RoutingInfo.ExchangeType.Topic, false));
//		rabbitManagementApi.assertExchangeExists("TestExchange");
//	}
//
//	@Test
//	public void createQueueShouldCreateAQueueWithBindings() {
//		RoutingInfo[] bindings = {
//				new RoutingInfo("TestExchange", RoutingInfo.ExchangeType.Topic, false, "Topic1"),
//				new RoutingInfo("TestExchange", RoutingInfo.ExchangeType.Topic, false, "Topic2")};
//		
//		rabbitBus.createExchange(bindings[0].getExchange());
//		rabbitBus.createQueue("TestQueue", bindings  ,false);
//		
//		rabbitManagementApi.assertQueueExists("TestQueue");
//		
//		String[] expectedBindings = {bindings[0].getRoutingKey(), bindings[1].getRoutingKey()};
//		String[] actualBindings = rabbitManagementApi.getBindingsForQueue("TestQueue", true).toArray(new String[0]);
//		assertArrayEquals(expectedBindings, actualBindings);
//	}
//	
//	@Test
//	public void deleteQueueShouldDeleteTheQueue(){
//		RoutingInfo[] bindings = {
//				new RoutingInfo("TestExchange", RoutingInfo.ExchangeType.Topic, false, "Topic1"),
//				new RoutingInfo("TestExchange", RoutingInfo.ExchangeType.Topic, false, "Topic2")};
//		
//		rabbitBus.createExchange(bindings[0].getExchange());
//		rabbitBus.createQueue("TestQueue", bindings  ,false);
//		rabbitBus.deleteQueue("TestQueue");
//	
//		rabbitManagementApi.assertQueueDoesNotExists("TestQueue");
//	}
//	
//	@Test
//	public void closeCanBeSafelyCalledMultipleTimes(){
//		rabbitBus.close();
//		rabbitBus.close();
//	}
//
//}
