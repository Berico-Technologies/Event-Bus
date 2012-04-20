package gov.ment.eventbus.amqp;

import static com.jayway.awaitility.Awaitility.*;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.TimeUnit;

import gov.ment.eventbus.testsupport.TestSendEvent2;

import org.junit.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import gov.ment.eventbus.client.Envelope;
import gov.ment.eventbus.client.EnvelopeHandler;
import gov.ment.eventbus.client.EventHandler;
import gov.ment.eventbus.client.EventResult;
import gov.ment.eventbus.client.SubscriptionToken;

public class AmqpEventManager_RespondToNonRpcEnvelopeTest extends
        AmqpEventManager_PublishEnvelopeTestBase {

  private byte[] bytesForMessageOfTypeTestSendEvent = { 18, 47, 0, 54, 70, 9, 5, 87, 0, 49, 87 };

  private void setupIncomingNonRpcMessage() {

    final Envelope envelopeOfTypeTestSendEvent = new Envelope();
    envelopeOfTypeTestSendEvent.setEventType(TestSendEvent2.class.getCanonicalName());
    envelopeOfTypeTestSendEvent.setBody(bytesForMessageOfTypeTestSendEvent);

    when(serializer.deserialize(bytesForMessageOfTypeTestSendEvent, TestSendEvent2.class))
            .thenReturn(new TestSendEvent2());

    when(messageBus.beginConsumingMessages(anyString(), any(EnvelopeHandler.class))).then(
            new Answer<String>() {

              @Override
              public String answer(InvocationOnMock invocation) throws Throwable {
                EnvelopeHandler handler = (EnvelopeHandler) invocation.getArguments()[1];
                handler.handleEnvelope(envelopeOfTypeTestSendEvent);
                return null;
              }
            });
  }

  @Override
  protected void publish() {
    // Here we set up a scenario where an event handler respondTo a received
    // event
    // but the event was sent with publish rather than getReponseTo. This
    // scenario
    // should simply publish the response according to the routing for that
    // type.
    setupIncomingNonRpcMessage();

    RpcEventHandler handler = new RpcEventHandler();

    SubscriptionToken token = manager.subscribe(handler);
    try {
      waitAtMost(200, TimeUnit.MILLISECONDS)
              .untilCall(to(handler).getEventHandled(), equalTo(true));
    } catch (Exception e) {
      fail(e.getMessage());
      e.printStackTrace();
    } finally {
      manager.unsubscribe(token);
    }
  }

  private class RpcEventHandler implements EventHandler<TestSendEvent2> {

    private volatile boolean eventHandled;

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends TestSendEvent2>[] getHandledEventTypes() {
      Class<?>[] handledTypes = { TestSendEvent2.class };
      return (Class<? extends TestSendEvent2>[]) handledTypes;
    }

    @Override
    public EventResult handleEvent(TestSendEvent2 event) {
      manager.respondTo(event, sendEvent);
      eventHandled = true;
      return EventResult.Handled;
    }

    public boolean getEventHandled() {
      return eventHandled;
    }
  }

  @Override
  @Test
  public void theEnvelopeShouldBePlacedOnTheBusUsingTheRoutingInformationFromTheRoutingProvider() {
    assertEquals(routingInfo, publishedRoute);
  }

  @Override
  @Test
  public void theTopicOfThePublishedEnvelopeShouldBeTheRoutingKeyFromTheRoutingInfo() {
    assertEquals(routingInfo.getRoutingKey(), publishedEnvelope.getTopic());
  }

  @Test
  public void thePublishedEnvelopetShouldHaveNoReplyToValueAssigned() {
    assertNull(publishedEnvelope.getReplyTo());
  }

  @Test
  public void thePublishedRouteShouldBeTheGenericRouteForTheResponseType() {
    assertEquals(routingInfo, publishedRoute);
  }
}
