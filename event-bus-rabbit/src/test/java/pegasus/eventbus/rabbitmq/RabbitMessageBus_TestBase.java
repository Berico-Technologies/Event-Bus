package pegasus.eventbus.rabbitmq;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.junit.*;
import org.mockito.*;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import pegasus.eventbus.amqp.ConnectionParameters;
import pegasus.eventbus.testsupport.RabbitManagementApiHelper;

import com.rabbitmq.client.ConnectionFactory;


public class RabbitMessageBus_TestBase {

	protected Logger log = Logger.getLogger(this.getClass());

	protected ConnectionParameters connectionParameters;
	protected RabbitMessageBus rabbitBus;
	protected RabbitManagementApiHelper rabbitManagementApi;
	protected ConnectionFactory connectionFactory;

	private FileSystemXmlApplicationContext context;
	
	@Before
	public void beforeEachTest() throws IOException{
		
		MockitoAnnotations.initMocks(this);
		
		context = new FileSystemXmlApplicationContext("src/test/resources/eventbus-context.xml");
		connectionParameters = context.getBean(ConnectionParameters.class);
		connectionFactory = new ConnectionFactory();
		connectionFactory.setUsername(connectionParameters.getUsername());
		connectionFactory.setPassword(connectionParameters.getPassword());
		connectionFactory.setHost(connectionParameters.getHost());
		connectionFactory.setVirtualHost(connectionParameters.getVirtualHost());
		connectionFactory.setPort(connectionParameters.getPort());
        
		rabbitManagementApi = new RabbitManagementApiHelper(connectionParameters);
		rabbitManagementApi.createVirtualHost();

		rabbitBus = context.getBean(RabbitMessageBus.class);
		rabbitBus.start(null);
	}
	
	@After
	public void afterEachTest(){
		rabbitBus.close();
		rabbitManagementApi.deleteVirtualHost();
		context.close();
	}
}
