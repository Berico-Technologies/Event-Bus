package pegasus.eventbus.integration_tests;

import static com.jayway.awaitility.Awaitility.to;
import static com.jayway.awaitility.Awaitility.waitAtMost;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.commons.httpclient.HttpException;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import pegasus.eventbus.client.EventHandler;
import pegasus.eventbus.client.EventResult;
import pegasus.eventbus.client.Subscription;
import pegasus.eventbus.client.SubscriptionToken;
import pegasus.eventbus.testsupport.ParallizedEvent;

import com.berico.testsupport.IntegrationTest;

@Category(IntegrationTest.class)
public class MultiThreadedSubscriptionTest extends IntegrationTestBase {

    ArrayList<ParallizedEvent>            receivedEvents;
    SubscriptionToken                   subscription;
    private MultiThreadingTestHandler   handler;
    private volatile int threadsInUses;
    private volatile int maxThreadsUsed;

    public ArrayList<ParallizedEvent> getReceivedEvents() {
        return receivedEvents;
    }
    
    @Override
    @Before
    public void beforeEachTest() throws HttpException, IOException {
    	super.beforeEachTest();
        receivedEvents = new ArrayList<ParallizedEvent>();
        
        subscription = subscribe();
        ParallizedEvent e1 = new ParallizedEvent(1, 100);
        ParallizedEvent e2 = new ParallizedEvent(2, 5);
        ParallizedEvent e3 = new ParallizedEvent(3, 5);
        ParallizedEvent e4 = new ParallizedEvent(4, 5);
        
        manager.publish(e1);
        manager.publish(e2);
        manager.publish(e3);
        manager.publish(e4);
        
        try {
			waitAtMost(10, TimeUnit.SECONDS).untilCall(to(this.getReceivedEvents()).size(), equalTo(4));
		} catch (Exception e) {
			e.printStackTrace();
			fail("Interrupted.");
		}

        for(ParallizedEvent e : receivedEvents){
        	log.debug("Event {} started at {}",  e.getThreadId(), e.getStartTime().getTime());
        }
        
    }
    
    @Test
    public void eventsShouldBeProcessedOnExactlyTwoThreads() throws Exception {
    	assertEquals(2, maxThreadsUsed);
    }
   
    @Test
    public void eventsShouldBeProcessedInTheOrderSent() throws Exception {
    	for(int i = 0; i < 4; i++){
    		assertEquals(i+1, receivedEvents.get(i).getId());
    	}
    }
   
    @Test
    public void event2ShouldStartBeforeEvent1Finishes() throws Exception {
    	ParallizedEvent e1 = receivedEvents.get(0);
    	ParallizedEvent e2 = receivedEvents.get(1);
    	assertTrue(e2.getStartTime().getTime() < e1.getEndTime().getTime());
    }
    

    @Test
    public void shortRunningEventShouldStartAfterLongRunningEvent() throws Exception {
    	ParallizedEvent longRunningEvent = receivedEvents.get(0);
    	ParallizedEvent shortRunningEvent = receivedEvents.get(1);
    	assertTrue("Failed: " + longRunningEvent.getStartTime().getTime() + "<" + shortRunningEvent.getStartTime().getTime(),
    			longRunningEvent.getStartTime().getTime()<shortRunningEvent.getStartTime().getTime());
    }
    
    
    @Test
    public void shortRunningEventShouldCompleteBeforePriorLongRunningEvent() throws Exception {
    	ParallizedEvent longRunningEvent = receivedEvents.get(0);
    	ParallizedEvent shortRunningEvent = receivedEvents.get(1);
    	assertTrue("Failed: " + longRunningEvent.getEndTime().getTime() + ">" + shortRunningEvent.getEndTime().getTime(),
    			longRunningEvent.getEndTime().getTime()>shortRunningEvent.getEndTime().getTime());
    }

    protected SubscriptionToken subscribe() {
        handler = new MultiThreadingTestHandler();
        Subscription subscription = new Subscription(handler);
        subscription.setNumberOfThreads(2);
        return manager.subscribe(subscription);
    }

    public class MultiThreadingTestHandler implements EventHandler<ParallizedEvent> {

        @Override
        public EventResult handleEvent(ParallizedEvent event) {
        	
        	synchronized(this){
        		threadsInUses++;
        		if (threadsInUses > maxThreadsUsed) maxThreadsUsed = threadsInUses;
        	}
        	
        	try{
        		
        		Date startTime = Calendar.getInstance().getTime();
            	long threadId = Thread.currentThread().getId();
    			
            	log.debug("Starting to handled event {} on thread {} at {}.", 
            			event.getId(), threadId, startTime.getTime());
            	
            	receivedEvents.add(event);
    			event.setThreadId(threadId);
                event.setStartTime(startTime);
                try {
    				Thread.sleep(event.getRunTime());
    				Date endTime = Calendar.getInstance().getTime();
            		event.setEndTime(endTime);

    				log.debug("Finished handling event {} on thread {} at {}.", 
    	        			event.getId(), threadId, endTime.getTime());
    			
                } catch (InterruptedException e) {
    				e.printStackTrace();
    			}
            	
            	return EventResult.Handled;
            	
        	} finally {
        		 synchronized(this){
             		threadsInUses--;
             	}
        	}
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public Class<? extends ParallizedEvent>[] getHandledEventTypes() {
            Class[] classes = { ParallizedEvent.class };
            return classes;
        }
    }
}
