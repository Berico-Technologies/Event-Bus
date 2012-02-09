package pegasus.eventbus.amqp;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.ArgumentCaptor;

import pegasus.eventbus.testsupport.TestSendEvent;

@RunWith(value = Parameterized.class)
public class AmqpEventManager_NullClientNameCommandLineVariationTest extends AmqpEventManager_TestBase{

	@Parameters
	public static Collection<Object[]> testObjectsToSerialize(){
		
		ArrayList<Object[]> variations = new ArrayList<Object[]>();

		String[] commandPrefixes = new String[] { 
				 "" , 
				 " " , 
				 "/path/to/" ,
				 "c:\\windows\\path\\to\\", 
				 "url://to/",
				 "/path/t.o/something.with.dots." ,
				 "c:\\windows\\path\\t.o\\something.with.dots.", 
				 "url://t.o/something.with.dots.",
				};

		String[][] commandsWithExpectedOutputs = new String[][] { 
				{ "basicCommand", "basicCommand" }, 
				{ "basicCommandWithExentsion.1234567", "basicCommandWithExentsion.1234567" }, 
				{ "basicCommandWithToLongExentsion.12345678", "12345678" }, 
				{ "$command-with-illegal-first-char", "_$command-with-illegal-first-char" },
				{ "amp.command-with-reserved-prefix", "command-with-reserved-prefix" },
				{ "underscore_command", "underscore_command" },
				{ "hyphen-command", "hyphen-command" },
				{ "CrazyCommand~`!@#$%^&*()_+-=[]}{';\":,<>?|", "CrazyCommand~`!@#$%^&*()_+-=[]}{';\":,<>?|" },
				{ StringUtils.rightPad("command-just-short-enough", 214, "x"), StringUtils.rightPad("command-just-short-enough", 214, "x")},
				{ StringUtils.rightPad("command-one-char-too-long", 215, "x"), StringUtils.rightPad("command-one-char-too-long", 214, "x")},
				{ StringUtils.rightPad("@illegally-prefixed-command-just-short-enough", 214, "x"), StringUtils.rightPad("_@illegally-prefixed-command-just-short-enough", 215, "x")},
				{ StringUtils.rightPad("@illegally-prefixed-command-one-char-too-long", 215, "x"), StringUtils.rightPad("_@illegally-prefixed-command-one-char-too-long", 215, "x")},
				};
		
		String[] commandSuffixes = new String[] { 
				 "" , 
				 " " , 
				 " -switch" ,
				 " -s1 arg0",
				 " arg -s2",
				 " /switch" ,
				 " /s1 arg0",
				 " arg /s2",
				 " arg0 arg1", 
				 " arg.with.dots /s.with.dots",
				};
		for( String prefix : commandPrefixes){
			for( String[] command : commandsWithExpectedOutputs){
				for( String suffix : commandSuffixes){
					Object[] variation = { prefix + command[0] + suffix, command[1] };
					variations.add(variation);
				}
			}
		}
		return variations;
	}

	private final String originalCommandLine;
	private final String expectedFinalClientName;
	
	public AmqpEventManager_NullClientNameCommandLineVariationTest(
			String originalCommandLine, String expectedFinalClientName) {
		super();
		this.originalCommandLine = originalCommandLine;
		this.expectedFinalClientName = expectedFinalClientName;
	}

	@Test
	public void clientNameShouldBeCorrectlyParsedFromCommandLineIfInitiallyNull(){

		System.setProperty("sun.java.command", originalCommandLine);

        configuration.setClientName(null);
        configuration.setAmqpMessageBus(messageBus);
        configuration.setTopologyManager(topologyManager);
        configuration.setSerializer(serializer);
        
        manager = new AmqpEventManager(configuration);
		
		manager.subscribe(new TestEventHandler(TestSendEvent.class));
		
		ArgumentCaptor<String> queueNameCaptor = ArgumentCaptor.forClass(String.class);
		
		verify(messageBus, times(1)).createQueue(queueNameCaptor.capture(), any(RoutingInfo[].class), anyBoolean());
			
		String queueName = queueNameCaptor.getValue();
		assertTrue(String.format("Input: %s Expected: %s(%d) but was %s(%d)", originalCommandLine, expectedFinalClientName ,expectedFinalClientName.length(), queueName.substring(0, queueName.length()-37), queueName.length()-37), queueName.startsWith(expectedFinalClientName));
	}

}
