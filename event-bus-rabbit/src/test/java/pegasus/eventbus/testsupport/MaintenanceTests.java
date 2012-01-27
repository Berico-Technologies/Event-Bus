package pegasus.eventbus.testsupport;

import java.io.IOException;

import org.junit.*;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.rabbitmq.client.ConnectionFactory;

/**
 * This class contains "test" that are not intended to be run as test in an automated fashion but 
 * give a convenient way of running certain pieces of maintenance code by manually executing a test.  
 */
public class MaintenanceTests {

	private RabbitManagementApiHelper rabbitManagementApi;

	@Before
	public void beforeEachTest() throws IOException{
		FileSystemXmlApplicationContext context = new FileSystemXmlApplicationContext("src/test/resources/eventbus-context.xml");
		ConnectionFactory connectionFactory = context.getBean(ConnectionFactory.class);

		rabbitManagementApi = new RabbitManagementApiHelper(connectionFactory);
	}

	@Test @Ignore("This test to be run mannually for cleanup as need be.")
	public void deleteAllVhosts(){
		rabbitManagementApi.deleteAllTestVhosts();
	}
	
}
