package pegasus.eventbus.integration_tests;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang.time.StopWatch;
import org.junit.*;
import org.junit.experimental.categories.Category;

import pegasus.eventbus.client.EventHandler;
import pegasus.eventbus.client.EventResult;
import pegasus.eventbus.testsupport.TestResponseEvent;
import pegasus.eventbus.testsupport.TestResponseEvent2;
import pegasus.eventbus.testsupport.TestSendEvent;
import pegasus.cip.core.testsupport.IntegrationTest;

import static com.jayway.awaitility.Awaitility.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class RpcTest extends IntegrationTestBase{
	
	@Test 
	public void getResponseToShouldReceiveResponseToSentEvent() throws Exception{
		
		RespondingEventHandler handler = new RespondingEventHandler();
		
		manager.subscribe(handler);
		
		@SuppressWarnings("unchecked")
		TestResponseEvent response = manager.getResponseTo(sendEvent, 2000, TestResponseEvent.class);
		
		assertNotNull(response);
		assertEquals(sendEvent.getId(), response.getId());
	}
	
	@Test 
	public void getResponseToShouldReceiveResponsesToResponsesToSentEvent() throws Exception{
		
		manager.subscribe(new RespondingEventHandler());
		manager.subscribe(new RespondingEventHandler2());
		
		ResponseEventHandler handler = new ResponseEventHandler();
		manager.getResponseTo(sendEvent, handler);
		
		waitAtMost(2, TimeUnit.SECONDS).untilCall(to(handler.getResponses()).size(), equalTo(2));
		
		assertThat(handler.getResponses().get(0), instanceOf(TestResponseEvent.class));
		assertThat(handler.getResponses().get(1), instanceOf(TestResponseEvent2.class));
	}
	
	@Test  
	public void getResponseToShouldNotWaitSignificatlyLongerThanTimeoutPeriodBeforeTimingOut() throws Exception{
		
		RpcListener listener = new RpcListener(sendEvent);
		Thread backgroundThread = new Thread(listener);
		backgroundThread.setName("Listener for test fixture " + this.getClass().getSimpleName());
		backgroundThread.start();

		//This will throw if we wait more than 200 mills for request to time out.
		waitAtMost(1, TimeUnit.SECONDS).untilCall(to(listener).getRequestTimedOut(), equalTo(true));
		
		assertThat(listener.getResponseTime(), lessThan(200l));
	}
	
	public void respondingEventHandler(TestSendEvent event){
		manager.respondTo(event, new TestResponseEvent(event.getId()));
	}
	
	public class RespondingEventHandler implements EventHandler<TestSendEvent>{
		
		@Override
		public EventResult handleEvent(TestSendEvent event) {
			log.debug("Responding to message: " + event.getId());
			manager.respondTo(event, new TestResponseEvent(event.getId()));
			return EventResult.Handled;
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public Class<? extends TestSendEvent>[] getHandledEventTypes() {
			Class[] classes =  { TestSendEvent.class };
			return classes;
		}
	}

	public class RespondingEventHandler2 implements EventHandler<TestResponseEvent>{
		
		@Override
		public EventResult handleEvent(TestResponseEvent event) {
			log.debug("Responding to response to message: " + event.getId());
			manager.respondTo(event, new TestResponseEvent2(event.getId()));
			return EventResult.Handled;
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public Class<? extends TestResponseEvent>[] getHandledEventTypes() {
			Class[] classes =  { TestResponseEvent.class };
			return classes;
		}
	}

	public class ResponseEventHandler implements EventHandler<TestResponseEvent>{
		
		private final ArrayList<TestResponseEvent> responses = new ArrayList<TestResponseEvent>();

		@Override
		public EventResult handleEvent(TestResponseEvent event) {
			log.debug("Response of type " + event.getClass().getSimpleName() + " received for message: " + event.getId());
			responses.add(event);
			return EventResult.Handled;
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public Class<? extends TestResponseEvent>[] getHandledEventTypes() {
			Class[] classes =  { TestResponseEvent.class, TestResponseEvent2.class };
			return classes;
		}

		public ArrayList<TestResponseEvent> getResponses() {
			return responses;
		}
	}

	public class RpcListener implements Runnable {

		TestSendEvent sendEvent;
		
		volatile boolean requestTimedOut = false;
		volatile long responseTime; 
		
		public RpcListener(TestSendEvent sendEvent) {
	    	this.sendEvent = sendEvent;
		}

		@SuppressWarnings("unchecked")
		public void run() {
		   StopWatch watch = new StopWatch();
			try {
				watch.start();
				manager.getResponseTo(sendEvent, 100, Object.class);
				watch.stop();
			} catch (TimeoutException e) {
				requestTimedOut = true;
				watch.stop();
			} catch (InterruptedException e) {
				e.printStackTrace();
				watch.stop();
			} finally {
				watch.getTime();
			}
		}
		
		public boolean getRequestTimedOut(){
			return requestTimedOut;
		}
		
		public long getResponseTime(){
			return responseTime;
		}
	}
}
