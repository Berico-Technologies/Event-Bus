package pegasus.eventbus.amqp;

import static com.jayway.awaitility.Awaitility.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.time.StopWatch;
import org.junit.*;
import org.mockito.ArgumentCaptor;

import pegasus.eventbus.client.Envelope;
import pegasus.eventbus.client.EnvelopeHandler;
import pegasus.eventbus.client.EventHandler;
import pegasus.eventbus.client.EventResult;
import pegasus.eventbus.client.SubscriptionToken;
import pegasus.eventbus.testsupport.TestSendEvent;

public class AmqpEventManager_BasicSubscribeTest extends AmqpEventManager_BasicSubscribeTestBase {

    @Test
    public void subscribingWithAQueueNameShouldCreateThatQueue() {
        subscribe("UseThisQueue");
        ArgumentCaptor<String> queueNameCaptor = ArgumentCaptor.forClass(String.class);
        verify(messageBus, times(1)).createQueue(queueNameCaptor.capture(), any(RoutingInfo[].class), anyBoolean());
        assertEquals("UseThisQueue", queueNameCaptor.getValue());
    }

    @Test
    public void aSubscriptionThatSpecifiesAQueueNameShouldBeDurable() {

        subscribe("thisQueueShouldBeDurable");
        ArgumentCaptor<Boolean> durrabilityCaptor = ArgumentCaptor.forClass(boolean.class);
        verify(messageBus, times(1)).createQueue(anyString(), any(RoutingInfo[].class), durrabilityCaptor.capture());
        assertTrue(durrabilityCaptor.getValue());
    }

    @Test
    public void subscribingWithAQueueNameShouldCauseTheRelatedQueueToBePolled() throws InterruptedException {

        subscribe("PollThisQueue");

        verify(messageBus, times(1)).beginConsumingMessages(eq("PollThisQueue"), any(EnvelopeHandler.class));
    }

    @Test
    @Ignore("Needs to be moved to RabbitMq package as this is now an integration test.")
    public void unsubscribingShouldSendtInteruptRequestsToAllHandlerThreadsOfThatSubscription() throws Exception {

        BackgroundUnsubscriber actor = new BackgroundUnsubscriber();
        LongRunningHandler handler = performActionWhileLongRunningEventHandlerIsHandlingAnEvent(actor);
        assertTrue(handler.threadInterupRequestWasReceived());
    }

    @Test
    @Ignore("Needs to be moved to RabbitMq package as this is now an integration test.")
    public void unsubscribingShouldWaitForAnyHandlerThreadsForThatSubscriptionWhichAreCurrentyProcessingAnEventToCompleteProcesing() throws Exception {

        BackgroundUnsubscriber actor = new BackgroundUnsubscriber();
        LongRunningHandler handler = performActionWhileLongRunningEventHandlerIsHandlingAnEvent(actor);
        assertTrue(handler.actionDidNotReturnedPriorToEventHandlerCompleting());
    }

    @Test
    @Ignore("Needs to be moved to RabbitMq package as this is now an integration test.")
    public void closingTheManagerShouldSendtInteruptRequestsToAllHandlerThreadsOfThatSubscription() throws Exception {

        BackgroundCloser actor = new BackgroundCloser();
        LongRunningHandler handler = performActionWhileLongRunningEventHandlerIsHandlingAnEvent(actor);
        assertTrue(handler.threadInterupRequestWasReceived());
    }

    @Test
    @Ignore("Needs to be moved to RabbitMq package as this is now an integration test.")
    public void closingTheManagerShouldWaitForAnyHandlerThreadsWhichAreCurrentyProcessingAnEventToCompleteProcesing() throws Exception {

        BackgroundCloser actor = new BackgroundCloser();
        LongRunningHandler handler = performActionWhileLongRunningEventHandlerIsHandlingAnEvent(actor);
        assertTrue(handler.actionDidNotReturnedPriorToEventHandlerCompleting());
    }

    @Test
    public void closingTheManagerShouldCloseTheMessageBus() {

        manager.close();

        verify(messageBus, times(1)).close();
    }

    protected SubscriptionToken subscribe(EventHandler<?> handler) {
        return manager.subscribe(handler);
    }

    protected void subscribe(String queueName) {
        manager.subscribe(handler, queueName);
    }

    private LongRunningHandler performActionWhileLongRunningEventHandlerIsHandlingAnEvent(BackgroundActor actor) throws InterruptedException, Exception {

        LongRunningHandler handler = new LongRunningHandler(actor);
        Thread actorThread = new Thread(actor);

        setupIncomingMessage();
        SubscriptionToken token = subscribe(handler);

        Thread.sleep(50);

        actor.setToken(token);
        actorThread.run();

        waitAtMost(1, TimeUnit.SECONDS).untilCall(to(actor).hasActionReturned(), equalTo(true));

        // @fixme - this needs some explaining
        // This first makes sure our test is set up properly and each thread makes its call in the correct order.
         assertTrue(handler.handlerBeganHandlingTheEventPriorToActionBeingInvoked());
        return handler;
    }

    private void setupIncomingMessage() {
        byte[] bytesForMessageOfTypeTestSendEvent = { 1, 76, 76, 46 };

        Envelope envelopeOfTypeTestSendEvent = new Envelope();
        envelopeOfTypeTestSendEvent.setEventType(TestSendEvent.class.getCanonicalName());
        envelopeOfTypeTestSendEvent.setBody(bytesForMessageOfTypeTestSendEvent);

        when(serializer.deserialize(bytesForMessageOfTypeTestSendEvent, TestSendEvent.class)).thenReturn(new TestSendEvent());

        // UnacceptedMessage unacceptedMessage = new UnacceptedMessage(envelopeOfTypeTestSendEvent,1);
        //
        // when(messageBus.getNextMessageFrom(anyString()))
        // .thenReturn(unacceptedMessage)
        // .thenReturn(null);

    }

    private abstract class BackgroundActor implements Runnable {

        protected SubscriptionToken token;
        private volatile boolean    actionHasReturned = false;

        public void setToken(SubscriptionToken token) {
            this.token = token;
        }

        @Override
        public void run() {
            doAction();
            actionHasReturned = true;
        }

        public boolean hasActionReturned() {
            return actionHasReturned;
        }

        protected abstract void doAction();
    }

    private class BackgroundUnsubscriber extends BackgroundActor {

        @Override
        protected void doAction() {
            manager.unsubscribe(token);
        }
    }

    private class BackgroundCloser extends BackgroundActor {

        @Override
        protected void doAction() {
            manager.close();
        }
    }

    private class LongRunningHandler implements EventHandler<TestSendEvent> {

        private final BackgroundActor actor;
        private volatile boolean      eventWasHandled;
        private volatile boolean      actionReturnedPriorToEventHandlerCompleting;
        private volatile boolean      interuptWasReceived;

        public LongRunningHandler(BackgroundActor actor) {
            super();
            this.actor = actor;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Class<? extends TestSendEvent>[] getHandledEventTypes() {
            Class<?>[] handledTypes = { TestSendEvent.class };
            return (Class<? extends TestSendEvent>[]) handledTypes;
        }

        @Override
        public EventResult handleEvent(TestSendEvent event) {
            StopWatch watch = new StopWatch();
            watch.start();
            while (watch.getTime() < 1000) {
                try {
                    Thread.sleep(500 - watch.getTime());
                } catch (InterruptedException e) {
                    interuptWasReceived = true;
                    continue;
                }
            }
            actionReturnedPriorToEventHandlerCompleting = actor.hasActionReturned();
            eventWasHandled = true;
            return EventResult.Handled;
        }

        public boolean handlerBeganHandlingTheEventPriorToActionBeingInvoked() {
            return eventWasHandled;
        }

        public boolean actionDidNotReturnedPriorToEventHandlerCompleting() {
            return !actionReturnedPriorToEventHandlerCompleting;
        }

        public boolean threadInterupRequestWasReceived() {
            return interuptWasReceived;
        }
    }

    @Override
    protected RoutingInfo[] getExpectedRoutes() {
        // TODO: PEGA-729 Make expected route list based on handler.getHandledTypes();

        // In order to support wiretaps on queue topic X where topic X may also be used for RPC
        // we must subscribe to topic X and topic X.# as the latter does not include the former.
        RoutingInfo[] expectedRoutes = { routingInfo, routingInfo, routingInfo2, routingInfo2, returnRoutingInfo, returnRoutingInfo };
        for (int i = 1; i < expectedRoutes.length; i += 2) {

            expectedRoutes[i] = new RoutingInfo(expectedRoutes[i].getExchange(), expectedRoutes[i].routingKey + AmqpEventManager.AMQP_ROUTE_SEGMENT_DELIMITER
                    + AmqpEventManager.AMQP_ROUTE_SEGMENT_WILDCARD);
        }
        return expectedRoutes;
    }

    @Override
    protected String getRouteSuffix() {
        throw new NotImplementedException("Not needed because getExpectedRoutes is overriden.");
    }
}
