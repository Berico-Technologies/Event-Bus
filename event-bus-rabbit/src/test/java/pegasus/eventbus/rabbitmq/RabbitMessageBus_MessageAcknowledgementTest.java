//package pegasus.eventbus.rabbitmq;
//
//import static org.junit.Assert.*;
//
//import java.io.IOException;
//import java.util.UUID;
//
//import org.apache.commons.lang.time.StopWatch;
//import org.junit.*;
//
//import pegasus.eventbus.amqp.AmqpMessageBus.UnacceptedMessage;
//
//import com.rabbitmq.client.Channel;
//import com.rabbitmq.client.Connection;
//import com.rabbitmq.client.GetResponse;
//
//public class RabbitMessageBus_MessageAcknowledgementTest extends RabbitMessageBus_TestBase {
//
//	private enum ResponseType {Accept, RejectNoRetry, RejectWithRetry, DoNotResponsd }
//	
//	private String queueName = UUID.randomUUID().toString();
//	
//	private void sendMessagesAndRespondToItWith(ResponseType responseType) throws IOException{
//		
//		Connection con = null;
//		Channel channel = null;
//		try{
//			con = connectionFactory.newConnection();
//			channel = con.createChannel();
//			channel.queueDeclare(queueName, false, false, false, null).getQueue();
//			
//			channel.basicPublish("", queueName, null, new byte[0]);
//			
//			StopWatch timer = new StopWatch();
//			timer.start();
//			
//			while(timer.getTime() < 1000){ 
//				
//				UnacceptedMessage receivedMessage = rabbitBus.getNextMessageFrom(queueName);
//				if(receivedMessage != null){
//					switch(responseType){
//						case Accept:
//							rabbitBus.acceptMessage(receivedMessage);
//							break;
//						case RejectNoRetry: 
//							rabbitBus.rejectMessage(receivedMessage, false);
//							break;
//						case RejectWithRetry: 
//							rabbitBus.rejectMessage(receivedMessage, true);
//							break;
//						case DoNotResponsd:
//							break;
//					}
//					break;
//				} else {
//					try {
//						Thread.sleep(50);
//					} catch (InterruptedException e) {
//						fail("Thread was interupted");
//					}
//				}
//			};
//		} finally {
//			if(channel != null && channel.isOpen())
//				channel.close();
//			if(con != null && con.isOpen())
//				con.close();
//		}
//	}
//	
//
//	private boolean queueIsEmpty() throws IOException {
//		Connection con = null;
//		Channel channel = null;
//		try{
//			con = connectionFactory.newConnection();
//			channel = con.createChannel();
//			GetResponse message = channel.basicGet(queueName, false);
//			return message == null;
//		} finally {
//			if(channel != null && channel.isOpen())
//				channel.close();
//			if(con != null && con.isOpen())
//				con.close();
//		}
//	}
//	
//		
//	@Test 
//	public void anAcceptedMessageShouldNotBeReturnedToTheQueue() throws IOException	{
//		sendMessagesAndRespondToItWith(ResponseType.Accept);
//		assertTrue(queueIsEmpty());
//	}
//	
//	@Test 
//	public void aRejectedMessageWithoutResendNotBeReturnedToTheQueue() throws IOException	{
//		sendMessagesAndRespondToItWith(ResponseType.RejectNoRetry);
//		assertTrue(queueIsEmpty());
//	}
//	
//	@Test 
//	public void aRejectedMessageWithResendShouldBeReturnedToTheQueue() throws IOException	{
//		sendMessagesAndRespondToItWith(ResponseType.RejectWithRetry);
//		assertFalse(queueIsEmpty());
//	}
//	
//	@Test 
//	public void aMessageThatIsNeitherAcceptedNorRejectedIsReturnedToTheQueueUponChannelClose() throws IOException	{
//		//In other word, this test asserts that we are not auto-acknowledging messages.
//		sendMessagesAndRespondToItWith(ResponseType.DoNotResponsd);
//		rabbitBus.close();
//		assertFalse(queueIsEmpty());
//	}
//}
