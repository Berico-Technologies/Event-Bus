package pegasus.eventbus.topology;

import java.util.concurrent.TimeoutException;

import org.junit.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.Mockito.*;

import pegasus.eventbus.client.EventManager;
import pegasus.eventbus.topology.events.HeartBeat;
import pegasus.eventbus.topology.events.TopologyUpdate;

//TODO: Need to add tests to cover non-heartbeat related functionality.
public class GlobalTopologyServiceManagerTest {

	@Mock
	private EventManager eventManager;

	@SuppressWarnings("unchecked")
	@Before
	public void beforeEachTest() throws InterruptedException, TimeoutException{
		MockitoAnnotations.initMocks(this);
	       
		when(eventManager.getResponseTo(anyObject(), anyInt(), eq(TopologyUpdate.class)))
	    	.thenReturn(new TopologyUpdate());
	}
	
	@Test
	public void startingTheManagerShouldStartSendingHeartbeats() throws InterruptedException {
		GlobalTopologyServiceManager manager = new GlobalTopologyServiceManager("testClient", 1);
		manager.start(eventManager);
		try{
			Thread.sleep(3000);
			verify(eventManager, atLeast(2)).publish(any(HeartBeat.class));
		} finally { 
			manager.close();
		}
	}

	@Test
	public void failuresToPublishHeatbeatShouldNotStopTheHeartbeat() throws InterruptedException {
		GlobalTopologyServiceManager manager = new GlobalTopologyServiceManager("testClient", 1);
		//doThrow(new RuntimeException("Failed to send heartbeat.")).when(eventManager).publish(any(HeartBeat.class));
		doAnswer(new Answer(){
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				if(invocation.getArguments()[0].getClass() == HeartBeat.class){
					throw new RuntimeException("Failed to send heartbeat.");
				}
				return null;
			}}).when(eventManager).publish(any(HeartBeat.class));
		manager.start(eventManager);
		try{
			Thread.sleep(3000);
			verify(eventManager, atLeast(2)).publish(any(HeartBeat.class));
		} finally { 
			manager.close();
		}
	}

	@Test
	public void closingTheManagerShouldStopSendingHeartbeats() throws InterruptedException {
		GlobalTopologyServiceManager manager = new GlobalTopologyServiceManager("testClient", 1);
		manager.start(eventManager);
		
		try{
			Thread.sleep(1000);
		} finally { 
			manager.close();
		}

		reset(eventManager);
		Thread.sleep(3000);
		verify(eventManager, never()).publish(any(HeartBeat.class));
	}
	
}
