package pegasus.eventbus.topology.integration_tests;


import java.io.IOException;

import org.apache.commons.httpclient.*;
import org.apache.log4j.Logger;
import org.junit.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import pegasus.eventbus.client.EventManager;

public class IntegrationTestBase {

    protected String             virtualHostName;
    protected Logger             log = Logger.getLogger(this.getClass());
    protected EventManager       manager;
    protected ApplicationContext context;
    protected TestEvent      sendEvent;
    
    @Before
    public void beforeEachTest() throws HttpException, IOException {
    	//TODO: PEGA-717 These tests need to be on the same vhost as the topology service, consider seting one up on a dedicated vhost for topology integration tests.
        FileSystemXmlApplicationContext context = new FileSystemXmlApplicationContext("src/test/resources/eventbus-context.xml");

        manager = context.getBean(EventManager.class);
        manager.start();

        sendEvent = new TestEvent();
    }

    @After
    public void afterEachTest() throws Exception {
        manager.close();
    }
}
