package pegasus.eventbus.amqp;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.*;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class AmqpEventManager_ClientNameValidationTest {

	@Parameters
	public static Collection<Object> clientNameVariations(){

		ArrayList<Object> variations = new ArrayList<Object>();
		
		for(int i = 0; i < 128; i++)
		{
			
				String name = Character.toString(Character.toChars(i)[0])  + "X";
				
				
				Object[] inputs = {name};
				variations.add(inputs);
			
		}
		
		Object[] nameThatIsJustShortEnought = { StringUtils.rightPad("_", 215, "*") };
		Object[] nameThatIsTooLongByOne = { StringUtils.rightPad("_", 216, "*") };
		
		variations.add(nameThatIsJustShortEnought);
		variations.add(nameThatIsTooLongByOne);
		
		Object[] nameUsingReservedNamespace = { "amq.is.reserved" };
		variations.add(nameUsingReservedNamespace);
		
		return variations;
	}
	
	private final String name;

	public AmqpEventManager_ClientNameValidationTest(String name) {
		super();
		this.name = name;
	}
	
	@Test
	public void theEventManagerShouldAcceptAndWorkWithEvenInvalidNames(){
		//This test is something of a leftover.  Originally it was testing that the manager 
		//through an exception when an invalid name was passed in. Now the manager actually fixes
		//such names, so now we are really testing that no exception is thrown under any 
		//circumstance.
		boolean nameWasAccepted = true;
		try{
			new AmqpEventManager(name, null, null, null, null);
		}catch(IllegalArgumentException e){
			nameWasAccepted = false;
		}
		
		assertTrue(name + name.length(), nameWasAccepted);
	}
}
