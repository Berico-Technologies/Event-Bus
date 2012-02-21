package pegasus.eventbus.amqp;

import static org.junit.Assert.*;

import org.junit.Test;


public class ConnectionParameters_ParserTests {

	
	private void assertConnectionParameters(
			String expectedUsername, String expectedPassword, 
			String expectedHost, int expectedPort, 
			String expectedVhost, ConnectionParameters actual){
		
		assertEquals(expectedUsername, actual.getUsername());
		assertEquals(expectedPassword, actual.getPassword());
		assertEquals(expectedHost, actual.getHost());
		assertEquals(expectedPort, actual.getPort());
		assertEquals(expectedVhost, actual.getVHost());
	}
	
	@Test
	public void uri_string_with_username_and_password_is_correctly_parsed() {
		
		String uri = "amqp://test:password123@rabbit-master.pegasus.mil:1234/";
		
		ConnectionParameters actualParameters = new ConnectionParameters(uri);
		
		assertConnectionParameters(
			"test", "password123", "rabbit-master.pegasus.mil", 1234, "/", actualParameters);
	}

	@Test
	public void delimited_string_is_correctly_parsed() {
		
		String delimited = "username=test;password=password123;host=rabbit-master.pegasus.mil;port=1234;vhost=/";
		
		ConnectionParameters actualParameters = new ConnectionParameters(delimited);
		
		assertConnectionParameters(
				"test", "password123", "rabbit-master.pegasus.mil", 1234, "/", actualParameters);
	}
	
}
