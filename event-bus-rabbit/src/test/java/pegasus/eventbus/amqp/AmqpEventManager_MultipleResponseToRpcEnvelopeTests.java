package pegasus.eventbus.amqp;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import static com.jayway.awaitility.Awaitility.*;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.junit.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import pegasus.eventbus.client.Envelope;
import pegasus.eventbus.client.EnvelopeHandler;
import pegasus.eventbus.client.EventHandler;
import pegasus.eventbus.client.EventResult;
import pegasus.eventbus.testsupport.TestResponseEvent;
import pegasus.eventbus.testsupport.TestResponseEvent2;
import pegasus.eventbus.testsupport.ValueSematicEvent;

/**
 * These tests assert the fix for PEGA-1903.  Specifically that events that implement a value based equality test and hashcode
 * can be processes **and replied to** by two separate handlers subscribed from the same message bus w/o interfering with each other even if 
 * the processing occurs concurrently.
 */
public class AmqpEventManager_MultipleResponseToRpcEnvelopeTests extends AmqpEventManager_TestBase {

    protected byte[]      bytesFromSerializer = { 42, 43, 52, 34, 54, 2, 23, 5, 43 };

    protected RoutingInfo publishedRoute;

    protected Envelope    publishedEnvelope;

    private List<EnvelopeHandler> handlers = new ArrayList<EnvelopeHandler>();
    
    private volatile boolean handler2HasStarted;
    private volatile boolean handler1HasCompleted;
    private volatile boolean handler2HasCompleted;
    
    private boolean response1Received ;
    private boolean response2Received ;
    
    
    @SuppressWarnings("unchecked")
	@Before
    @Override
    public void beforeEachTest() {

        super.beforeEachTest();

        when(topologyManager.getRoutingInfoForEvent(ValueSematicEvent.class)).thenReturn(routingInfo);
        when(topologyManager.getRoutingInfoForEvent(TestResponseEvent2.class)).thenReturn(routingInfo);

        when(serializer.serialize(any(TestResponseEvent.class))).thenReturn(bytesFromSerializer);
        when(serializer.serialize(any(TestResponseEvent2.class))).thenReturn(bytesFromSerializer);
        
        when(serializer.serialize(any(ValueSematicEvent.class))).then(new Answer<byte[]>(){
			@Override
			public byte[] answer(InvocationOnMock invocation) throws Throwable {
				Object event = invocation.getArguments()[0];
				//This appears to be a bug in Mockito.  Despite the when(serializer.serialize(any(TestResponseEvent2.class)))
				//calls above, all calls to serializer.serialize are being handed to this handler so we have to do a double duty here. 
				if( event instanceof ValueSematicEvent){
					return ((ValueSematicEvent)event).id.toString().getBytes(Charset.forName("UTF-8"));
				} else {
					return bytesFromSerializer;
				}
			}});

        when(serializer.deserialize(any(byte[].class), any(Class.class))).then(new Answer<ValueSematicEvent>(){
			@Override
			public ValueSematicEvent answer(InvocationOnMock invocation) throws Throwable {
				return new ValueSematicEvent( UUID.fromString(
						new String((byte[])invocation.getArguments()[0], Charset.forName("UTF-8"))));
			}});

		when(messageBus.beginConsumingMessages(anyString(), any(EnvelopeHandler.class)))
		.then(new Answer<String>(){
			
			@Override
			public String answer(InvocationOnMock invocation)
					throws Throwable {
				handlers.add((EnvelopeHandler) invocation.getArguments()[1]);
				return null;
			}
		});
		
		doAnswer(new Answer(){
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				
				Envelope envelope = ((Envelope) invocation.getArguments()[1]);
				
				if( envelope.getEventType().equals(ValueSematicEvent.class.getCanonicalName()))	{
					for(EnvelopeHandler h : handlers){
						new Thread(new HandlerThread(h, envelope)).start();
					}
				} else if( envelope.getEventType().equals(TestResponseEvent.class.getCanonicalName())) {
					response1Received = true;
				} else if( envelope.getEventType().equals(TestResponseEvent2.class.getCanonicalName()))	{
					response2Received = true;
				}
				return null;
				
			}}).when(messageBus).publish(any(RoutingInfo.class), any(Envelope.class));

		
		manager.subscribe(new EventHandler<ValueSematicEvent>(){

			@Override
			public Class<? extends ValueSematicEvent>[] getHandledEventTypes() {
				return new Class[] { ValueSematicEvent.class };
			}

			@Override
			public EventResult handleEvent(ValueSematicEvent event) {
				try {
					await().until(theSecondHandlerIsHandlingItsEvent());
					manager.respondTo(event, new TestResponseEvent());
				} catch (NullPointerException e) {
					handler1HasCompleted = true;
					System.out.println("It appears that the EventManager has prematurely discarded its reference to the envelope for the current event.");
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
				handler1HasCompleted = true;
				return EventResult.Handled;
			}});
		
		manager.subscribe(new EventHandler<ValueSematicEvent>(){

			@Override
			public Class<? extends ValueSematicEvent>[] getHandledEventTypes() {
				return new Class[] { ValueSematicEvent.class };
			}

			@Override
			public EventResult handleEvent(ValueSematicEvent event) {
				handler2HasStarted = true;
				try {
					manager.respondTo(event, new TestResponseEvent2());
				} catch (NullPointerException e) {
					handler1HasCompleted = true;
					System.out.println("It appears that the EventManager has prematurely discarded its reference to the envelope for the current event.");
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
				handler2HasCompleted = true;
				return EventResult.Handled;
			}});

		manager.publish(new ValueSematicEvent(UUID.randomUUID()));
		
		try {
			await().until(allHandlersAreFinished());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       
    }
    
    private Callable<Boolean> theSecondHandlerIsHandlingItsEvent() {
        return new Callable<Boolean>() {
                public Boolean call() throws Exception {
                	return handler2HasStarted; 
                }
        };
    }
        
    private Callable<Boolean> allHandlersAreFinished() {
        return new Callable<Boolean>() {
                public Boolean call() throws Exception {
                	return handler1HasCompleted && handler2HasCompleted; 
                }
        };
    }
   
    @Test
    public void theFirstResponseToTheValueEventShouldBeReceived() {
        assertTrue(response1Received);
    }

    @Test
    public void theSecondResponseToTheValueEventShouldBeReceived() {
    	assertTrue(response2Received);
    }
    
    private static class HandlerThread implements Runnable {

    	private final EnvelopeHandler handler;
    	private final Envelope envelope;
    	
    	public HandlerThread(EnvelopeHandler handler, Envelope envelope){
    		this.handler = handler;
    		this.envelope = envelope;
    	}
    	
		@Override
		public void run() {
			handler.handleEnvelope(envelope);			
		}
    	
    }
}
