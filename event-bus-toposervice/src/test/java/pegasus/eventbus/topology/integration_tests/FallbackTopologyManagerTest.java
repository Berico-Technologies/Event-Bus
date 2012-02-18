package pegasus.eventbus.topology.integration_tests;

import static com.jayway.awaitility.Awaitility.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.httpclient.HttpException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import pegasus.eventbus.client.EventHandler;
import pegasus.eventbus.client.EventResult;
import pegasus.eventbus.topology.event.EventTypeRoutingInfo;
import pegasus.eventbus.topology.event.GetEventTypeRoute;
import pegasus.eventbus.topology.event.TopologyUpdate;

@Ignore("Un-ignore after a topology service is actually deployed to the test environment.")
public class FallbackTopologyManagerTest extends IntegrationTestBase {

	private final String nameOfNewType = UUID.randomUUID().toString();
	UpdateHandler handler;
	EventTypeRoutingInfo routeInfoResponseForNewType;
	
    @SuppressWarnings("unchecked")
	@Before
    @Override
    public void beforeEachTest() throws HttpException, IOException {
    	super.beforeEachTest();
    	
    	handler = new UpdateHandler();
    	manager.subscribe(handler);
    	
    	try {
    		routeInfoResponseForNewType = manager.getResponseTo(
    				new GetEventTypeRoute(nameOfNewType), 1000, EventTypeRoutingInfo.class);
		} catch (InterruptedException e) {
			fail("Thread interrupted.");
		} catch (TimeoutException e) {
			fail("Timeout occurred while getting routing info from topology service.");
		}

    	try {
			waitAtMost(5, TimeUnit.SECONDS).untilCall(to(handler).getReceivedEvent(), notNullValue());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Timeout occurred while waiting for TopologyUpdate event.");
		}
   }

    @Test
	public void gettingTheRouteForAnUnknownEventTypeShouldReturnARoute(){
    	assertEquals(nameOfNewType, routeInfoResponseForNewType.getEventTypeCannonicalName());
    	assertNotNull(routeInfoResponseForNewType.getRouteInfo());
    }
    
    @Test
	public void gettingTheRouteForAnUnknownEventTypeShouldCauseATopologyUpdateContainingTheNewRouteToBeBroadcase(){
    	assertNotNull(handler.getReceivedEvent());
    	assertEquals(routeInfoResponseForNewType.getRouteInfo(), 
    			handler.getReceivedEvent().getTopologyRegistry().getEventRoute(nameOfNewType));
    }

    public class UpdateHandler implements EventHandler<TopologyUpdate> {

    	private TopologyUpdate receivedEvent;
    	
		@SuppressWarnings("unchecked")
		@Override
		public Class<? extends TopologyUpdate>[] getHandledEventTypes() {
			Class<?>[] handledEvents = { TopologyUpdate.class };
			return (Class<? extends TopologyUpdate>[]) handledEvents;
		}

		@Override
		public EventResult handleEvent(TopologyUpdate event) {
			receivedEvent = event;
			return EventResult.Handled;
		}
    	
		public TopologyUpdate getReceivedEvent(){
			return receivedEvent;
		}
    }
}