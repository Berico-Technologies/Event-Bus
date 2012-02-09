package pegasus.eventbus.testsupport;

import java.io.IOException;
import java.util.UUID;

import org.springframework.context.support.FileSystemXmlApplicationContext;

import pegasus.eventbus.amqp.ConnectionParameters;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * This class extends the rabbitMq connection factory to override the vhost name so as to ensure
 * each instance of the factory (and hence each test) uses a unique vhost to ensure test isolation.  
 * It also ensures the creation of that vhost prior to the creation of any connection to it.  
 */
public class ConnectionFactoryWithRandomVHost extends ConnectionFactory {

	private String uniqueVHostSuffix = UUID.randomUUID().toString();
	private boolean vhostCreated;

    private ConnectionParameters connectionParameters;
    private FileSystemXmlApplicationContext context;
    
    public ConnectionFactoryWithRandomVHost() {
        super();
        
        context = new FileSystemXmlApplicationContext("src/test/resources/eventbus-context.xml");
        connectionParameters = context.getBean(ConnectionParameters.class);
    }
    
	@Override 
	public void setVirtualHost(String virtualHost){
		super.setVirtualHost(virtualHost + "-" + uniqueVHostSuffix);
	}
	
    @Override 
    public Connection newConnection() throws IOException{
    	
    	if(!vhostCreated){
    		RabbitManagementApiHelper helper = new RabbitManagementApiHelper(connectionParameters);
    		helper.createVirtualHost();
    		vhostCreated = true;
    	}
    	
    	return super.newConnection();
    }
}
