package gov.ment.eventbus.amqp;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import gov.ment.eventbus.amqp.RoutingInfo;

import org.junit.*;
import org.mockito.InOrder;

import gov.ment.eventbus.client.Envelope;
import gov.ment.eventbus.client.EnvelopeHandler;
import gov.ment.eventbus.client.SubscriptionToken;

public class AmqpEventManager_NonBlockingRPCBasicSubscribeTest extends
        AmqpEventManager_BasicSubscribeTestBase {

  @Override
  protected SubscriptionToken subscribe() {
    return manager.getResponseTo(sendEvent, handler);
  }

  @Override
  protected String getRouteSuffix() {
    // Routing keys for RPC subscriptions should include the reply-to queueName
    // as the final segment.
    return getCreatedQueueName();
  }

  @Test
  public void theEnvelopeShouldNotBePublishedBeforeTheResponseHandlerIsPolling() {

    InOrder inOrder = inOrder(messageBus);

    subscribe();

    inOrder.verify(messageBus).beginConsumingMessages(anyString(), any(EnvelopeHandler.class));
    inOrder.verify(messageBus).publish(any(RoutingInfo.class), any(Envelope.class));
  }
}
