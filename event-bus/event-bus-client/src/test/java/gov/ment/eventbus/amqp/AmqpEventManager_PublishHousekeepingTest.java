package gov.ment.eventbus.amqp;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import gov.ment.eventbus.amqp.RoutingInfo;

import org.junit.Test;

public class AmqpEventManager_PublishHousekeepingTest extends
        AmqpEventManager_PublishHousekeepingTestBase {

  @Override
  protected void publish() {
    manager.publish(sendEvent);
  }

  @Test
  public void publishingAnEventShouldNotCreateAnyQueues() {

    verify(messageBus, never()).createQueue(anyString(), any(RoutingInfo[].class), anyBoolean());
  }
}
