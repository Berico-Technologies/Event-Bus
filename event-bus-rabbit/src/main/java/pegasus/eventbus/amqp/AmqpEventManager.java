package pegasus.eventbus.amqp;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.time.StopWatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pegasus.eventbus.amqp.AmqpMessageBus.UnacceptedMessage;
import pegasus.eventbus.client.Envelope;
import pegasus.eventbus.client.EnvelopeHandler;
import pegasus.eventbus.client.EventHandler;
import pegasus.eventbus.client.EventManager;
import pegasus.eventbus.client.EventResult;
import pegasus.eventbus.client.FallbackDetails;
import pegasus.eventbus.client.FallbackHandler;
import pegasus.eventbus.client.Subscription;
import pegasus.eventbus.client.SubscriptionToken;
import pegasus.eventbus.client.FallbackDetails.FallbackReason;

public class AmqpEventManager implements EventManager {

    // DO NOT change these values, they are based on the in the AMPQ spec.
    static final String AMQP_ROUTE_SEGMENT_DELIMITER = ".";
    static final String AMQP_ROUTE_SEGMENT_WILDCARD = "#";

    // Must start with Alpha, Digit or _ and be no more than 255 chars. Special
    // chars, spaces, etc. are allowed.
    // We are limiting name to 215 chars to allow us to append UUID.
    private static final Pattern VALID_AMQP_NAME = Pattern.compile("^\\w{1}.{0,214}+$");
    // AMQP name may not start with amq. as this is reserved
    private static final Pattern FIRST_CHARS_INVALID_FOR_AMQP = Pattern.compile("^(\\W|(amq\\.))");

    // Assumes command is anything prior to the first whitespace and then
    // extracts the final ., / or \ delimited segment thereof
    // however . appearing within the final 8 characters of command are included
    // in command as a presumed extension.
    private static final Pattern NAME_FROM_COMMAND = Pattern
            .compile("((?:^([^\\s./\\\\]+?(?:\\.[^\\s./\\\\]{0,7})*?))|((?:(?:^\\S*?[./\\\\])|^)([^\\s./\\\\]+?(?:\\.[^\\s./\\\\]{0,7})*?)))(?:\\s|$)");

    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

    private final String clientName;
    private final AmqpMessageBus messageBus;
    private final EventTypeToTopicMapper eventTopicMapper;
    private final TopicToRoutingMapper routingProvider;
    private final Serializer serializer;

    private Map<SubscriptionToken, ActiveSubscription> activeSubscriptions = new HashMap<SubscriptionToken, ActiveSubscription>();
    private Map<Object, Envelope> envelopesBeingHandled = new HashMap<Object, Envelope>();

    /**
     * 
     * @param clientName
     *            the Name of the client application, used when creating random
     *            queues in order to make the queue names identifiable according
     *            to the applications that are using them.
     * @param messageBus
     *            an implementation of AmqpMessageBus that provides access to
     *            the bus itself.
     * @param eventTopicMapper
     *            a mapper that maps event types to their topics
     * @param routingProvider
     *            a mapper that provides routing information for topic.
     * @param serializer
     *            provides event serialization and deserialization services.
     */
    public AmqpEventManager(Configuration configuration) {
        super();

        this.messageBus = configuration.getAmqpMessageBus();
        this.eventTopicMapper = configuration.getEventTypeToTopicMapper();
        this.routingProvider = configuration.getTopicToRoutingMapper();
        this.serializer = configuration.getSerializer();

        String tempName = getFallBackClientNameIfNeeded(configuration.getClientName());
        this.clientName = fixNameIfInvalidForAmqp(tempName);

        // Because fixNameIfInvalidForAmqp should fixing any invalid names, the
        // validation method
        // is not really needed any more but we are keeping it in place just in
        // case something
        // gets past it.
        validateClientName();
    }

    private String getFallBackClientNameIfNeeded(String clientName) {

        if (clientName == null || clientName.trim().length() == 0) {
            // Try to get name from what command was run to start this process.
            clientName = System.getProperty("sun.java.command").trim();
            Matcher matcher = NAME_FROM_COMMAND.matcher(clientName);
            if (matcher.find()) {
                clientName = matcher.group(2) == null ? matcher.group(4) : matcher.group(2);
            }
        }

        if (clientName == null || clientName.trim().length() == 0) {
            // Try to use computer name as client name.
            try {
                clientName = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                clientName = "UNKNOWN";
            }
        }

        return clientName;
    }

    private String fixNameIfInvalidForAmqp(String clientName) {
        clientName = clientName.trim();

        int length = clientName.length();
        if (length > 214) {
            clientName = clientName.substring(length - 215, length - 1);
        }
        if (FIRST_CHARS_INVALID_FOR_AMQP.matcher(clientName).find()) {
            clientName = "_" + clientName;
        }
        return clientName;
    }

    private void validateClientName() {
        if (!VALID_AMQP_NAME.matcher(clientName).find()) {
            throw new IllegalArgumentException(
                    "The clientName must begin with a letter number or underscore and be no more than 215 characters long.");
        } else if (clientName.startsWith("amq.")) {
            throw new IllegalArgumentException(
                    "The clientName may not begin with 'amq.' as this is a reserved namespace.");
        }
    }

    @Override
    public void publish(Object event) {
        publish(event, null, false);
    }

    private void publish(Object event, String replyToQueue, boolean sendToReplyToQueue) {
        String topic = eventTopicMapper.getTopicFor(event.getClass());
        RoutingInfo routing = routingProvider.getRoutingInfoFor(topic);
        if (sendToReplyToQueue) {
            routing = new RoutingInfo(routing.exchange, routing.routingKey
                    + AmqpEventManager.AMQP_ROUTE_SEGMENT_DELIMITER + replyToQueue);
        } else {
            ensureRouteExists(routing);
        }
        byte[] body = serializer.serialize(event);
        Envelope envelope = new Envelope();
        envelope.setId(UUID.randomUUID());
        envelope.setTopic(routing.getRoutingKey());
        envelope.setEventType(event.getClass().getCanonicalName());
        envelope.setReplyTo(replyToQueue);
        envelope.setBody(body);
        messageBus.publish(routing, envelope);
    }

    @Override
    public SubscriptionToken subscribe(EventHandler<?> handler) {
        return subscribe(getNewQueueName(), false, handler, getFailingFallbackHandler());
    }

    @Override
    public SubscriptionToken subscribe(EventHandler<?> handler, FallbackHandler fallbackHandler) {
        return subscribe(getNewQueueName(), false, handler, fallbackHandler);
    }

    @Override
    public SubscriptionToken subscribe(String queueName, EventHandler<?> handler) {
        return subscribe(queueName, handler, getFailingFallbackHandler());
    }

    @Override
    public SubscriptionToken subscribe(String queueName, EventHandler<?> handler, FallbackHandler fallbackHandler) {
        if (queueName == null || queueName.length() == 0)
            throw new IllegalArgumentException("QueueName may not be null nor zero length.");
        return subscribe(queueName, true, handler, fallbackHandler);
    }

    private SubscriptionToken subscribe(String queueName, boolean isDurable, EventHandler<?> handler,
            FallbackHandler fallbackHandler) {
        Subscription subscription = new Subscription(queueName, handler);
        subscription.setFallbackHandler(fallbackHandler);
        subscription.setIsDurable(isDurable);
        return subscribe(subscription);
    }

    @Override
    public SubscriptionToken subscribe(Subscription subscription) {
        if (subscription == null)
            throw new IllegalArgumentException("Subscription may not be null.");
        return subscribe(subscription, AMQP_ROUTE_SEGMENT_WILDCARD);
    }

    private SubscriptionToken subscribe(Subscription subscription, String routeSuffix) {

        RoutingInfo[] routes = subscription.getEventsetName() == null ? getRoutesBaseOnEventHandlerHandledTypes(
                subscription.getEventHandler(), routeSuffix) : routingProvider
                .getRoutingInfoForNamedEventSet(subscription.getEventsetName());

        for (RoutingInfo route : routes) {
            ensureRouteExists(route);
        }

        String queueName = subscription.getQueueName() == null ? getNewQueueName() : subscription.getQueueName();

        messageBus.createQueue(queueName, routes, subscription.getIsDurable());

        EnvelopeHandler handler = subscription.getEnvelopeHandler();
        if (handler == null) {
            handler = new EventEnvelopeHandler(subscription.getEventHandler(), subscription.getFallbackHandler());
        }

        QueueListener listener = new QueueListener(queueName, handler);
        listener.beginListening();

        SubscriptionToken token = new SubscriptionToken();
        activeSubscriptions.put(token, new ActiveSubscription(queueName, subscription.getIsDurable(), listener));
        return token;
    }

    private RoutingInfo[] getRoutesBaseOnEventHandlerHandledTypes(EventHandler<?> eventHandler, String routeSuffix) {

        ArrayList<RoutingInfo> routes = new ArrayList<RoutingInfo>();
        final Class<?>[] handledEventTypes = eventHandler.getHandledEventTypes();
        for (Class<?> eventType : handledEventTypes) {
            RoutingInfo routingInfo = routingProvider.getRoutingInfoFor(eventTopicMapper.getTopicFor(eventType));
            if (routeSuffix == AMQP_ROUTE_SEGMENT_WILDCARD) {
                routes.add(routingInfo);
            }
            routingInfo = new RoutingInfo(routingInfo.getExchange(), routingInfo.getRoutingKey()
                    + AMQP_ROUTE_SEGMENT_DELIMITER + routeSuffix);
            routes.add(routingInfo);
        }
        return routes.toArray(new RoutingInfo[0]);
    }

    private String getNewQueueName() {
        return clientName + ":" + UUID.randomUUID().toString();
    }

    private FallbackHandler getFailingFallbackHandler() {
        // TODO Auto-generated method stub
        return null;
    }

    private HashSet<String> exchangesKnownToExist = new HashSet<String>();

    private void ensureRouteExists(RoutingInfo routingInfo) {
        final String exchangeName = routingInfo.getExchange().getName();
        if (exchangesKnownToExist.contains(exchangeName))
            return;

        messageBus.createExchange(routingInfo.getExchange());
        exchangesKnownToExist.add(exchangeName);
    }

    @Override
    public SubscriptionToken getResponseTo(Object event, EventHandler<?> handler) {
        String replyToQueueName = getNewQueueName();
        Subscription subscription = new Subscription(handler);
        subscription.setQueueName(replyToQueueName);
        SubscriptionToken token = subscribe(subscription, replyToQueueName);
        while (!activeSubscriptions.get(token).listener.isCurrentlyListening()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                break;
            }
        }
        publish(event, replyToQueueName, false);
        return token;
    }

    @Override
    public <TResponse> TResponse getResponseTo(Object event, int timeoutMills,
            Class<? extends TResponse>... responseTypes) throws InterruptedException, TimeoutException {

        callbackHandler<TResponse> handler = new callbackHandler<TResponse>(responseTypes);

        SubscriptionToken token = getResponseTo(event, handler);
        try {
            return waitForAndReturnResponse(handler, timeoutMills);
        } finally {
            unsubscribe(token);
        }
    }

    private <TResponse> TResponse waitForAndReturnResponse(callbackHandler<TResponse> handler, int timeoutMills)
            throws InterruptedException, TimeoutException {

        int sleepInterval = Math.min(50, timeoutMills / 10);

        StopWatch watch = new StopWatch();
        watch.start();

        TResponse response = handler.getReceivedResponse();
        while (watch.getTime() < timeoutMills && response == null) {
            Thread.sleep(sleepInterval);
            response = handler.getReceivedResponse();
        }

        watch.stop();

        if (response == null) {
            throw new TimeoutException("Response was not received within the time specified.");
        }

        return response;
    }

    @Override
    public void respondTo(Object orginalRequest, Object response) {
        Envelope originalRequestEnvelope = envelopesBeingHandled.get(orginalRequest);
        if (originalRequestEnvelope.getReplyTo() == null) {
            publish(response);
        } else {
            publish(response, originalRequestEnvelope.getReplyTo(), true);
        }
    }

    @Override
    public void unsubscribe(SubscriptionToken token) {
        unsubscribe(token, false);
    }

    @Override
    public void unsubscribe(SubscriptionToken token, boolean deleteQueue) {
        ActiveSubscription subscription = activeSubscriptions.get(token);
        if (subscription == null) {
            throw new IllegalStateException(
                    "The provided token does not refer to an active subscription of this event manager.");
        }
        ArrayList<ActiveSubscription> subscriptions = new ArrayList<ActiveSubscription>();
        subscriptions.add(subscription);
        deactivateSubscriptions(subscriptions, deleteQueue);
        activeSubscriptions.remove(token);
    }

    @Override
    public void close() {

        deactivateSubscriptions(activeSubscriptions.values(), false);
        activeSubscriptions.clear();
        messageBus.close();
    }

    private void deactivateSubscriptions(Collection<ActiveSubscription> subscriptions, boolean deleteDurableQueues) {
        for (ActiveSubscription subscription : subscriptions) {
            subscription.getListener().StopListening();
        }

        boolean someListenersAreStillListening = !subscriptions.isEmpty();

        while (someListenersAreStillListening) {
            someListenersAreStillListening = false;
            for (ActiveSubscription subscription : subscriptions) {
                if (subscription.getListener().isCurrentlyListening()) {
                    someListenersAreStillListening = true;
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        LOG.debug("Thread [" + Thread.currentThread().getName()
                                + "] interrupted in method AmqpEventManager.deactivateSubscriptions().");
                    }
                    break;
                }
            }
        }

        for (ActiveSubscription subscription : subscriptions) {
            if (deleteDurableQueues || !subscription.getQueueIsDurable()) {
                messageBus.deleteQueue(subscription.getQueueName());
            }
        }
    }

    private class callbackHandler<TResponse> implements EventHandler<TResponse> {

        private final Class<? extends TResponse>[] handledTypes;
        private volatile TResponse receivedResponse;

        public callbackHandler(Class<? extends TResponse>... handledTypes) {
            super();
            this.handledTypes = handledTypes;
        }

        @Override
        public Class<? extends TResponse>[] getHandledEventTypes() {
            return handledTypes;
        }

        @Override
        public EventResult handleEvent(TResponse event) {
            receivedResponse = event;
            return EventResult.Handled;
        }

        TResponse getReceivedResponse() {
            return receivedResponse;
        }
    }

    protected class ActiveSubscription {

        private final String queueName;
        private final Boolean queueIsDurable;
        private final QueueListener listener;
        private boolean isActive = true;

        public ActiveSubscription(String queueName, Boolean queueIsDurable, QueueListener listener) {
            super();
            this.queueName = queueName;
            this.queueIsDurable = queueIsDurable;
            this.listener = listener;
        }

        public String getQueueName() {
            return queueName;
        }

        public Boolean getQueueIsDurable() {
            return queueIsDurable;
        }

        public QueueListener getListener() {
            return listener;
        }

        public boolean isActive() {
            return isActive;
        }

        public void setIsActive(boolean queueIsDeleted) {
            this.isActive = queueIsDeleted;
        }

    }

    protected class QueueListener implements Runnable {

        private final Logger log = LoggerFactory.getLogger(this.getClass());

        private final String queueName;
        private final String threadName;
        private EnvelopeHandler envelopeHandler;

        private volatile boolean currentlyListening;
        private volatile boolean continueListening;

        private Thread backgroundThread;

        public QueueListener(String queueName, EnvelopeHandler eventHandler) {

            this.queueName = queueName;
            this.threadName = "Listener for queue: " + queueName;
            this.envelopeHandler = eventHandler;
        }

        public void beginListening() {
            if (backgroundThread != null)
                return;

            continueListening = true;

            backgroundThread = new Thread(this);
            backgroundThread.setName(threadName);
            backgroundThread.start();
        }

        @Override
        public void run() {
            log.info("Starting to listen on thread [" + Thread.currentThread().getName() + "].");
            currentlyListening = true;
            while (continueListening) {
                try {
                    UnacceptedMessage message;
                    synchronized (this) {
                        // see not in StopListening() as to why we are
                        // synchronizing here.
                        message = messageBus.getNextMessageFrom(queueName);
                    }
                    if (message == null) {
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            log.debug("Thread [" + threadName
                                    + "] interrupted in method AmqpEventManager$QueueListener.run().");
                            break;
                        } finally {
                        }
                        continue;
                    }

                    EventResult result;
                    Envelope envelope = message.getEnvelope();
                    try {
                        result = envelopeHandler.handleEnvelope(envelope);
                    } catch (Exception e) {
                        result = EventResult.Failed;
                        String id;
                        try {
                            id = envelope.getId().toString();
                        } catch (Exception ee) {
                            id = "<message id not available>";
                        }
                        log.error("Envelope handler of type " + envelopeHandler.getClass().getCanonicalName()
                                + " on queue " + queueName + " threw exception of type "
                                + e.getClass().getCanonicalName() + " handling message " + id, e);
                    }

                    switch (result) {
                    case Handled:
                        messageBus.acceptMessage(message);
                        break;
                    case Failed:
                        messageBus.rejectMessage(message, false);
                        break;
                    case Retry:
                        messageBus.rejectMessage(message, true);
                        break;
                    }
                } catch (Exception e) {
                    log.error("Envelope handler of type " + envelopeHandler.getClass().getCanonicalName()
                            + " on queue " + queueName + " threw exception of type " + e.getClass().getCanonicalName()
                            + " while retrieving next message.");
                }
            }
            currentlyListening = false;
            backgroundThread = null;
            log.info("Stopped listening on thread [" + threadName + "].");
        }

        public void StopListening() {
            continueListening = false;

            // This is a bit screwy but the
            // RpcTest.getResponseToShouldReceiveResponsesToResposnesToSentEvent
            // test will
            // usually hang if interrupt() is called because the timing of the
            // test is such that the interrupt gets called
            // while the AMQP-client.channel.basicGet() is blocking because
            // getBasic apparently fails to handle the interrupt correctly.
            // Therefore we synchronize on the listener here and when calling
            // getNextMessageFrom so that we are sure never
            // to call interrupt while in the middle of a basicGet();
            log.debug("Interrupting thread [" + threadName + "].");
            synchronized (this) {
                if (backgroundThread == null) {
                    log.debug("backgroundThread was null for thread [" + threadName + "].");
                } else {
                    backgroundThread.interrupt();
                }
            }

        }

        public boolean isCurrentlyListening() {
            return currentlyListening;
        }
    }

    private class EventEnvelopeHandler implements EnvelopeHandler {

        private final EventHandler<?> eventHandler;
        private final FallbackHandler fallbackHandler;
        private final ArrayList<Class<?>> handledTypes;
        private Method handlerMethod;

        public EventEnvelopeHandler(EventHandler<?> eventHandler, FallbackHandler fallbackHandler) {

            this.eventHandler = eventHandler;
            this.fallbackHandler = fallbackHandler;

            handledTypes = new ArrayList<Class<?>>();
            for (Class<?> eventType : eventHandler.getHandledEventTypes()) {
                handledTypes.add(eventType);
            }

            for (Method method : eventHandler.getClass().getMethods()) {
                if (method.getName() == "handleEvent") {
                    handlerMethod = method;
                    break;
                }
            }

            // This should never actually happen
            if (handlerMethod == null)
                throw new RuntimeException("eventHandler method not found on EvenHandler of type "
                        + eventHandler.getClass());
        }

        private class Details implements FallbackDetails {

            private FallbackReason reason;
            private Exception exception;

            @Override
            public FallbackReason getReason() {
                return reason;
            }

            public void setReason(FallbackReason reason) {
                this.reason = reason;
            }

            @Override
            public Exception getException() {
                return exception;
            }

            public void setException(Exception exception) {
                this.exception = exception;
            }
        };

        @Override
        public EventResult handleEnvelope(Envelope envelope) {

            Details fallbackDetails = new Details();

            try {
                Object event = null;
                boolean eventIsOfWrongType = false;
                try {
                    String className = envelope.getEventType();
                    Class<? extends Object> eventType = Class.forName(className);
                    if (handledTypes.contains(eventType)) {
                        event = serializer.deserialize(envelope.getBody(), eventType);
                    } else {
                        eventIsOfWrongType = true;
                    }
                } catch (Exception e) {
                    fallbackDetails.setReason(FallbackReason.DeserializationError);
                    fallbackDetails.setException(e);
                    return fallbackHandler.handleEnvelope(envelope, fallbackDetails);
                }

                if (eventIsOfWrongType) {
                    fallbackDetails.setReason(FallbackReason.EventNotOfHandledType);
                    return fallbackHandler.handleEnvelope(envelope, fallbackDetails);
                }

                // NOTE: For performance sake, we are not synchronizing our
                // access to envelopesBeingHandled
                // Due to the nature of our keys the kinds of concerns
                // synchronizing defends against should
                // never occur. Rarely should any two threads ever be looking at
                // the same event. (This would
                // require that the handler spawn another thread and that thread
                // call respondTo. It is this
                // scenario that causes us not to just use ThreadLocal<Envelope>
                // here.) And in all cases,
                // never should there ever be the potential for an insert or
                // remove of the same event on
                // separate threads.
                envelopesBeingHandled.put(event, envelope);

                try {
                    EventResult result = (EventResult) handlerMethod.invoke(eventHandler, event);
                    if (result == EventResult.Failed) {
                        fallbackDetails.setReason(FallbackReason.EventHandlerReturnedFailure);
                        return fallbackHandler.handleEnvelope(envelope, fallbackDetails);
                    } else {
                        return result;
                    }
                } catch (Exception e) {
                    fallbackDetails.setReason(FallbackReason.EventHandlerThrewException);
                    fallbackDetails.setException(e);
                    return fallbackHandler.handleEnvelope(envelope, fallbackDetails);
                } finally {
                    envelopesBeingHandled.remove(event);
                }
            } catch (Exception e) {
                LOG.error("Unable to handle message: '" + envelope + "'", e);
                return EventResult.Failed;
            }
        }
    }
}
