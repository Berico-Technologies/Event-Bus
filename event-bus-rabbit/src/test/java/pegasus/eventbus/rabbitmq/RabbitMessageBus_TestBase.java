package pegasus.eventbus.rabbitmq;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.junit.*;
import org.mockito.*;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import pegasus.eventbus.testsupport.RabbitManagementApiHelper;

import com.rabbitmq.client.ConnectionFactory;


public class RabbitMessageBus_TestBase {

	protected Logger log = Logger.getLogger(this.getClass());

	protected ConnectionFactory connectionFactory;
	protected RabbitMessageBus rabbitBus;
	protected RabbitManagementApiHelper rabbitManagementApi;

	private FileSystemXmlApplicationContext context;
	
	@Before
	public void beforeEachTest() throws IOException{
		
		MockitoAnnotations.initMocks(this);
		
		context = new FileSystemXmlApplicationContext("src/test/resources/eventbus-context.xml");
		connectionFactory = context.getBean(ConnectionFactory.class);

		rabbitManagementApi = new RabbitManagementApiHelper(connectionFactory);
		
		rabbitBus = new RabbitMessageBus(connectionFactory);
	}
	
	@After
	public void afterEachTest(){
		rabbitBus.close();
		rabbitManagementApi.deleteVirtualHost();
		context.close();
	}
}
