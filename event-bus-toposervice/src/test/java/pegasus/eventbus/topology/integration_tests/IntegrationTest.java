package pegasus.eventbus.topology.integration_tests;

import static com.jayway.awaitility.Awaitility.to;
import static com.jayway.awaitility.Awaitility.waitAtMost;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.httpclient.*;
import org.apache.log4j.Logger;
import org.junit.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import pegasus.eventbus.client.EventHandler;
import pegasus.eventbus.client.EventManager;
import pegasus.eventbus.client.EventResult;

public class IntegrationTest {

    protected String             virtualHostName;
    protected Logger             log = Logger.getLogger(this.getClass());
    protected EventManager       manager;
    protected ApplicationContext context;
    protected TestEvent      sendEvent;
    
    @Before
    public void beforeEachTest() throws HttpException, IOException {

        FileSystemXmlApplicationContext context = new FileSystemXmlApplicationContext("src/test/resources/eventbus-context.xml");

        manager = context.getBean(EventManager.class);
        manager.start();

        sendEvent = new TestEvent();
    }

    @After
    public void afterEachTest() throws Exception {
        manager.close();
    }

    @Test
    public void canSendAndReceiveEvents() throws Exception{
    	
    	TestHandler handler = new TestHandler();
    	manager.subscribe(handler);
    	
    	manager.publish(new TestEvent());
    	
        waitAtMost(5, TimeUnit.SECONDS).untilCall(to(handler).eventWasReceived(), equalTo(true));
    }
 
    public class TestHandler implements EventHandler<TestEvent>{

    	private volatile boolean eventReceived;
    	
		@SuppressWarnings("unchecked")
		@Override
		public Class<? extends TestEvent>[] getHandledEventTypes() {
			Class<?>[] handledEvents = { TestEvent.class };
			return (Class<? extends TestEvent>[]) handledEvents;
		}

		@Override
		public EventResult handleEvent(TestEvent event) {
			eventReceived = true;
			return EventResult.Handled;
		}
    	
		public boolean eventWasReceived(){
			return eventReceived;
		}
    }
}
