package pegasus.eventbus.routing;

import static org.junit.Assert.*;

import org.junit.Test;

import pegasus.eventbus.testsupport.TestSendEvent;

public class BasicTypeToTopicMapperTest {

	@Test
	public void getTopicForShouldReturnCannonicalNameOfClass() {
		BasicTypeToTopicMapper mapper = new BasicTypeToTopicMapper();
		assertEquals(TestSendEvent.class.getCanonicalName(), mapper.getTopicFor(TestSendEvent.class));
	}
	
	@Test
	public void getEventTypeForShouldReturnATypeGivenItsCannonicalName() {
		BasicTypeToTopicMapper mapper = new BasicTypeToTopicMapper();
		assertEquals(TestSendEvent.class, mapper.getEventTypeFor(TestSendEvent.class.getCanonicalName()));
	}
}
