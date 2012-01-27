package pegasus.eventbus.amqp;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.ArgumentCaptor;

import pegasus.eventbus.testsupport.TestSendEvent;

@RunWith(value = Parameterized.class)
public class AmqpEventManager_NullClientNameHandlingTest extends AmqpEventManager_TestBase{

	@Parameters
	public static Collection<Object[]> testObjectsToSerialize(){
		
		Object[][] data = new Object[][] { 
				{ null }, 
				{ "" },
				{ "  " }
				};
		return Arrays.asList(data);
	}

	private final String clientNameOnConstructor;
	
	public AmqpEventManager_NullClientNameHandlingTest(
			String clientNameOnConstructor) {
		super();
		this.clientNameOnConstructor = clientNameOnConstructor;
	}

	@Test
	public void clientNameFirstUsesNameOfCurrentlyExecutingCommand(){

		System.setProperty("sun.java.command", "myCommand");
		testCreationOfManagerWithClientNameOf("myCommand");  
	}

	@Test
	public void clientNameUsesNameOfComputerWhenCurrentlyExecutingCommandNotAvailable() throws UnknownHostException{
		
		System.setProperty("sun.java.command", "");

		testCreationOfManagerWithClientNameOf(InetAddress.getLocalHost().getHostName());  
	}

	private void testCreationOfManagerWithClientNameOf(String expectedFinalClientName) {
		manager = new AmqpEventManager(clientNameOnConstructor, messageBus, eventTopicMapper, routingProvider, serializer);
		
		manager.subscribe(new TestEventHandler(TestSendEvent.class));
		
		ArgumentCaptor<String> queueNameCaptor = ArgumentCaptor.forClass(String.class);
		
		verify(messageBus, times(1)).createQueue(queueNameCaptor.capture(), any(RoutingInfo[].class), anyBoolean());
			
		assertTrue(queueNameCaptor.getValue().startsWith(expectedFinalClientName));
	}
}
