package pegasus.eventbus.rabbitmq;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;
import org.junit.*;
import org.junit.experimental.categories.Category;
import org.mockito.*;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.GetResponse;

import pegasus.eventbus.amqp.AmqpConnectionParameters;
import pegasus.eventbus.amqp.RoutingInfo;
import pegasus.eventbus.testsupport.RabbitManagementApiHelper;
import pegasus.core.testsupport.IntegrationTest;

@Category(IntegrationTest.class)
public class RabbitMessageBus_TestBase {

    protected Logger                        log = Logger.getLogger(this.getClass());

    protected AmqpConnectionParameters          connectionParameters;
    protected RabbitMessageBus              rabbitBus;
    protected RabbitManagementApiHelper     rabbitManagementApi;
    protected RabbitConnection              connection;

    private FileSystemXmlApplicationContext context;

    @Before
    public void beforeEachTest() throws IOException {

        MockitoAnnotations.initMocks(this);

        context = new FileSystemXmlApplicationContext("src/test/resources/eventbus-context.xml");
        connectionParameters = context.getBean(AmqpConnectionParameters.class);
        connection = new RabbitConnection(connectionParameters);

        rabbitManagementApi = new RabbitManagementApiHelper(connectionParameters);
        rabbitManagementApi.createVirtualHost();

        rabbitBus = context.getBean(RabbitMessageBus.class);
        rabbitBus.start();
    }

    @After
    public void afterEachTest() {
        rabbitBus.close();
        rabbitManagementApi.deleteVirtualHost();
        context.close();
    }

    protected String getExchangeName() {
        return "pegasus-test";
    }

    protected RoutingInfo.ExchangeType getExchangeType() {
        return RoutingInfo.ExchangeType.Direct;
    }

    protected GetResponse getMessageFromDestination(Channel channel, String queueName) throws IOException {
        GetResponse receivedMessage;
        StopWatch timer = new StopWatch();
        timer.start();
        do {
            receivedMessage = channel.basicGet(queueName, true);
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                fail("Thread was interupted");
            }
        } while (receivedMessage == null && timer.getTime() < 2000);

        assertNotNull("Message not received in time allowed.", receivedMessage);

        return receivedMessage;
    }
    
}
