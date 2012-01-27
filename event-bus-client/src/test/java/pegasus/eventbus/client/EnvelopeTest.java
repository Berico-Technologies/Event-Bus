package pegasus.eventbus.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

public class EnvelopeTest {

	private Envelope env;
    
	@Before
	public void beforeEachTest(){
		env = new Envelope();
	}

	@Test
	public void getHeadersShouldNotInitializeToNull(){
		assertNotNull(new Envelope().getHeaders());
	}
	
	
	@Test
	public void getBodyShouldNotInitializeToNull(){
		assertNotNull(new Envelope().getBody());
	}

	//This is the behavior that we get from Rabbit.  A null body sent is received as a zero length byte array.
	@Test
	public void getBodyShouldNeverReturnNull(){
		env.setBody(null);
		assertNotNull(new Envelope().getBody());
	}
	
	@Test
	public void settingTheIdPropertyShouldReflectInGettingTheId(){
		UUID id = UUID.randomUUID();
		
		env.setId(id);
		
		assertEquals(id, env.getId());
	}
	
	@Test
	public void settingTheIdPropertyShouldNotUpdateTheIdHeader(){
		UUID id = UUID.randomUUID();
		
		env.setId(id);
		
		assertTrue(env.getHeaders().isEmpty());
	}
	
	@Test
	public void settingTheCorrelationIdPropertyShouldReflectInGettingTheCorrelationId(){
		UUID id = UUID.randomUUID();
		
		env.setCorrelationId(id);
		
		assertEquals(id, env.getCorrelationId());
	}
	
	@Test
	public void settingTheCorrelationIdPropertyShouldNotUpdateTheIdHeader(){
		UUID id = UUID.randomUUID();
		
		env.setCorrelationId(id);
		
		assertTrue(env.getHeaders().isEmpty());
	}
	
	@Test
	public void settingTheTopicPropertyShouldReflectInGettingTheTopic(){
		String value = UUID.randomUUID().toString();
		
		env.setTopic(value);
		
		assertEquals(value, env.getTopic());
	}
	
	@Test
	public void settingTheTopicPropertyShouldNotUpdateTheTopicHeader(){
		String value = UUID.randomUUID().toString();
		
		env.setTopic(value);
		
		assertTrue(env.getHeaders().isEmpty());
	}
	
	@Test
	public void settingTheEventTypePropertyShouldReflectInGettingTheEventType(){
		String value = UUID.randomUUID().toString();
		
		env.setEventType(value);
		
		assertEquals(value, env.getEventType());
	}
	
	@Test
	public void settingTheEventTypePropertyShouldNotUpdateTheTopicHeader(){
		String value = UUID.randomUUID().toString();
		
		env.setEventType(value);
		
		assertTrue(env.getHeaders().isEmpty());
	}
	
	@Test
	public void settingTheReplyToPropertyShouldReflectInGettingTheReplyToProperty(){
		String value = UUID.randomUUID().toString();
		
		env.setReplyTo(value);
		
		assertEquals(value, env.getReplyTo());
	}
	
	@Test
	public void settingTheReplyToPropertyShouldNotUpdateTheTopicHeader(){
		String value = UUID.randomUUID().toString();
		
		env.setReplyTo(value);
		
		assertTrue(env.getHeaders().isEmpty());
	}
}
