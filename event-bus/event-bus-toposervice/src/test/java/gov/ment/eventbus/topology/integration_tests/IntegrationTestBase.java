package gov.ment.eventbus.topology.integration_tests;

import java.io.IOException;

import org.apache.commons.httpclient.*;
import org.junit.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import gov.ment.eventbus.client.EventManager;

public class IntegrationTestBase {

  protected String virtualHostName;
  protected EventManager manager;
  protected ApplicationContext context;
  protected TestEvent sendEvent;

  @Before
  public void beforeEachTest() throws HttpException, IOException {
    // TODO: These tests need to be on the same vhost as the topology service,
    // consider seting one up on a dedicated
    // vhost for topology integration tests.
    FileSystemXmlApplicationContext context =
            new FileSystemXmlApplicationContext("src/test/resources/event-bus-toposervice.xml");

    manager = context.getBean(EventManager.class);
    manager.start();

    sendEvent = new TestEvent();
  }

  @After
  public void afterEachTest() throws Exception {
    manager.close();
  }
}
