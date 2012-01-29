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

/**
 * An implementation of the Event Manager based on the AMQP specification.
 * @author Ken Baltrinic (Berico Technologies)
 */
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

    // Loggers for this Class and internal Classes (which can't be declared "static" from
    // within their own definition.
    protected static final Logger LOG = LoggerFactory.getLogger(AmqpEventManager.class);

    private final String clientName;
    private final AmqpMessageBus messageBus;
    private final EventTypeToTopicMapper eventTopicMapper;
    private final TopicToRoutingMapper routingProvider;
    private final Serializer serializer;

    private Map<SubscriptionToken, ActiveSubscription> activeSubscriptions = new HashMap<SubscriptionToken, ActiveSubscription>();
    private Map<Object, Envelope> envelopesBeingHandled = new HashMap<Object, Envelope>();

    /**
     * Instantiate the EventManager from configuration.
     * @param configuration Configuration object for the Event Manager.
     */
    public AmqpEventManager(Configuration configuration) {

    	LOG.info("Starting the AMQP Event Manager.");
    	
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

    /**
     * If client name is null, attempt to pull the host name from the
     * environment or fall back to "UNKNOWN"
     * @param clientName Name of this instance of the AmqpEventManager
     * @return Best client name available
     */
    private String getFallBackClientNameIfNeeded(String clientName) {

    	LOG.trace("Attempting to grab the correct client name; one provided = [{}]", clientName);
    	
        if (clientName == null || clientName.trim().length() == 0) {
        	
        	LOG.trace("Invalid client name. Attempting to pull from Environment.");
        	
            // Try to get name from what command was run to start this process.
            clientName = System.getProperty("sun.java.command").trim();
            Matcher matcher = NAME_FROM_COMMAND.matcher(clientName);
            if (matcher.find()) {
                clientName = matcher.group(2) == null ? matcher.group(4) : matcher.group(2);
            }
        }

        if (clientName == null || clientName.trim().length() == 0) {
        	
        	LOG.trace("Could not find client name in environment, pulling hostname of computer instead.");
        	
            // Try to use computer name as client name.
            try {
            	
                clientName = InetAddress.getLocalHost().getHostName();
            
            } catch (UnknownHostException e) {
            
            	LOG.error("Could not find the hostname. Resorting to 'UNKNOWN'.", e);
            	
            	clientName = "UNKNOWN";
            }
        }

        LOG.trace("Final client name was [{}]", clientName);
        
        return clientName;
    }

    /**
     * If the client name is invalid for AMQP, attempt to fix it.
     * @param clientName Current Client Name
     * @return Valid Client Name
     */
    private String fixNameIfInvalidForAmqp(String clientName) {
        
    	LOG.trace("Normalizing Client Name.");
    	
    	clientName = clientName.trim();

        int length = clientName.length();
        if (length > 214) {
            clientName = clientName.substring(length - 215, length - 1);
        }
        if (FIRST_CHARS_INVALID_FOR_AMQP.matcher(clientName).find()) {
            clientName = "_" + clientName;
        }
        
        LOG.trace("Normalized Client Name is [{}]", clientName);
        return clientName;
    }

    /**
     * Ensure the client name is valid.
     */
    private void validateClientName() {
    	
    	LOG.trace("Validating Client Name.");
    	
        if (!VALID_AMQP_NAME.matcher(clientName).find()) {
        	
        	LOG.error("The client name must begin with a letter number or underscore and be no more than 215 characters long.");
        	
            throw new IllegalArgumentException(
                    "The clientName must begin with a letter number or underscore and be no more than 215 characters long.");
            
        } else if (clientName.startsWith("amq.")) {
        	
        	LOG.error("The client name may not begin with 'amq.' as this is a reserved namespace.");
        	
            throw new IllegalArgumentException(
                    "The clientName may not begin with 'amq.' as this is a reserved namespace.");
        }
    }

    /**
     * Publish an Event on the Bus.
     * @param event Event (message) to publish.
     */
    @Override
    public void publish(Object event) {
    	
    	LOG.debug("Publishing event of type [{}] on the bus.", event.getClass().getName());
    	
        publish(event, null, false);
    }

    /**
     * Actual implementation of publishing a message on the bus, taking into account the need
     * for a reply, or optionally, the publishing of this message as a reply to another event.
     * @param event Event to publish
     * @param replyToQueue ReplyTo Queue
     * @param sendToReplyToQueue Is this message being sent as a reply?
     */
    private void publish(Object event, String replyToQueue, boolean sendToReplyToQueue) {
    	
    	LOG.trace("Publishing event of type [{}].  Expect Response? {}; Is this a reply? = {}",
    			new Object[]{ event.getClass().getName(), replyToQueue != null, sendToReplyToQueue });
    	
    	LOG.trace("Finding the correct topic for event [{}]", event.getClass().getName());
    	
        String topic = eventTopicMapper.getTopicFor(event.getClass());
        
        LOG.trace("Finding the correct RoutingInfo for the topic [{}]", topic);
        
        RoutingInfo routing = routingProvider.getRoutingInfoFor(topic);
        
        if (sendToReplyToQueue) {
        
        	LOG.trace("Creating Routing Info for the ReplyTo queue.");
        	
        	routing = new RoutingInfo(routing.exchange, routing.routingKey
                    + AmqpEventManager.AMQP_ROUTE_SEGMENT_DELIMITER + replyToQueue);
        	
        } else {
        	
        	LOG.trace("Asserting route actually exists.");
        	
            ensureRouteExists(routing);
        }
        
        LOG.trace("Serializing the event to byte array.");
        
        byte[] body = serializer.serialize(event);
        
        LOG.trace("Creating envelope.");
        
        Envelope envelope = new Envelope();
        envelope.setId(UUID.randomUUID());
        envelope.setTopic(routing.getRoutingKey());
        envelope.setEventType(event.getClass().getCanonicalName());
        envelope.setReplyTo(replyToQueue);
        envelope.setBody(body);
        
        LOG.trace("Publishing to the message bus instance.");
        
        messageBus.publish(routing, envelope);
    }
    
    
    /**
     * Subscribe to all events the supplied handler is capable of handling.
     * @param handler Event Handler
     */
    @Override
    public SubscriptionToken subscribe(EventHandler<?> handler) {
    	
    	LOG.debug("Subscribing Handler [{}] to Event Types: {}", 
    			handler.getClass().getName(), joinEventTypesAsString(handler.getHandledEventTypes()));
    	
        return subscribe(getNewQueueName(), false, handler, getFailingFallbackHandler());
    }

    /**
     * Subscribe to all events the supplied handler is capable of handling, but
     * if any issue arises, use the FallbackHandler to process the envelope.
     * @param handler Event Handler
     * @param fallbackHandler Fallback Handler that will process the envelope on failures
     * @return Subscription Token used to unregister the handler
     */
    @Override
    public SubscriptionToken subscribe(EventHandler<?> handler, FallbackHandler fallbackHandler) {
    	
    	LOG.debug("Subscribing Handler [{}] and FallbackHandler [{}] to Event Types: {}", 
    			new Object[]{ 
    				handler.getClass().getName(), 
    				fallbackHandler.getClass().getName(), 
    				joinEventTypesAsString(handler.getHandledEventTypes()) });
    	
        return subscribe(getNewQueueName(), false, handler, fallbackHandler);
    }

    /**
     * Subscribe to all events on a known queue with the supplied Event Handler.
     * @param queueName Name of the Known Queue
     * @param handler Event Handler
     * @return Subscription Token used to unregister the handler
     */
    @Override
    public SubscriptionToken subscribe(String queueName, EventHandler<?> handler) {
    	
    	LOG.debug("Subscribing Handler [{}] to known queue [{}] for Event Types: {}", 
    			new Object[]{ 
    				handler.getClass().getName(), 
    				queueName,
    				joinEventTypesAsString(handler.getHandledEventTypes()) });
    	
        return subscribe(queueName, handler, getFailingFallbackHandler());
    }

    /**
     * Subscribe to all events on a known queue with the supplied Event Handler.
     * @param queueName Name of the Known Queue
     * @param handler Event Handler
     * @param fallbackHandler Fallback Handler that will process the envelope on failures
     * @return Subscription Token used to unregister the handler
     */
    @Override
    public SubscriptionToken subscribe(String queueName, EventHandler<?> handler, FallbackHandler fallbackHandler) {
    	
    	LOG.debug("Subscribing Handler [{}] and FallbackHandler [{}] to known queue [{}] for Event Types: {}", 
    			new Object[]{ 
    				handler.getClass().getName(), 
    				fallbackHandler.getClass().getName(), 
    				queueName,
    				joinEventTypesAsString(handler.getHandledEventTypes()) });
    	
        if (queueName == null || queueName.length() == 0){ 
         
        	LOG.error("QueueName may not be null nor zero length.");
        	
        	throw new IllegalArgumentException("QueueName may not be null nor zero length.");
        }
        
        return subscribe(queueName, true, handler, fallbackHandler);
    }

    /**
     * Subscribe to all events on a known queue with the supplied Event Handler.
     * @param queueName Name of the Known Queue
     * @param isDurable Is the Queue Durable
     * @param handler Event Handler
     * @param fallbackHandler Fallback Handler that will process the envelope on failures
     * @return Subscription Token used to unregister the handler
     */
    private SubscriptionToken subscribe(String queueName, boolean isDurable, EventHandler<?> handler,
            FallbackHandler fallbackHandler) {
    	
    	LOG.debug("Subscribing Handler [{}] and FallbackHandler [{}] to known queue [{}] (is durable? = {}) for Event Types: {}", 
    			new Object[]{ 
    				handler.getClass().getName(), 
    				fallbackHandler.getClass().getName(), 
    				queueName,
    				isDurable,
    				joinEventTypesAsString(handler.getHandledEventTypes()) });
    	
        Subscription subscription = new Subscription(queueName, handler);
        subscription.setFallbackHandler(fallbackHandler);
        subscription.setIsDurable(isDurable);
        return subscribe(subscription);
    }

    /**
     * IOC friendly way to register a subscription with the bus.
     * @param subscription Subscription to register.
     */
    @Override
    public SubscriptionToken subscribe(Subscription subscription) {
    	
    	LOG.debug("New subscription registered with the Event Bus client");
    	
        if (subscription == null){
        	
        	LOG.error("Subscription may not be null.");
        	
            throw new IllegalArgumentException("Subscription may not be null.");    
        }
        
        return subscribe(subscription, AMQP_ROUTE_SEGMENT_WILDCARD);
    }

    /**
     * Does the dirty work of actually subscribing to a particular queue, registering
     * the subscription with the EventManager's internal list, and producing the token
     * components will need to unbind their active subscriptions.
     * @param subscription Subscription to register.
     * @param routeSuffix Used to capture all messages in a particular namespace (wild card)
     * @return Subscription Token that can be used to unbind the handler from the bus.
     */
    private SubscriptionToken subscribe(Subscription subscription, String routeSuffix) {

    	LOG.trace("Locating route information for the provided subscription.");
    	
        RoutingInfo[] routes = subscription.getEventsetName() == null ? getRoutesBaseOnEventHandlerHandledTypes(
                subscription.getEventHandler(), routeSuffix) : routingProvider
                .getRoutingInfoForNamedEventSet(subscription.getEventsetName());
                
        LOG.trace("{} routes found for subscription.", (routes != null)? routes.length : 0);
                
        LOG.trace("Ensuring routes exist.");
                
        for (RoutingInfo route : routes) {
            ensureRouteExists(route);
        }
        
        String queueName = subscription.getQueueName() == null ? getNewQueueName() : subscription.getQueueName();

        LOG.trace("Creating new queue [{}] (or ensuring queue exists).", queueName);
        
        messageBus.createQueue(queueName, routes, subscription.getIsDurable());

        LOG.trace("Pulling EnvelopeHandler from subscription.");
        
        EnvelopeHandler handler = subscription.getEnvelopeHandler();
        
        if (handler == null) {
        
        	LOG.trace("EnvelopeHandler was null, creating default (EventEnvelopeHandler) instead.");
        	
            handler = new EventEnvelopeHandler(subscription.getEventHandler(), subscription.getFallbackHandler());
        }

        LOG.trace("Creating new queue listener for subscription.");
        
        QueueListener listener = new QueueListener(queueName, handler);
        
        LOG.trace("Starting the queue listener.");
        
        listener.beginListening();
        
        SubscriptionToken token = new SubscriptionToken();
        
        LOG.trace("Adding new active subscription with token to the 'active subscriptions' list.");
        
        activeSubscriptions.put(token, new ActiveSubscription(queueName, subscription.getIsDurable(), listener));
        
        LOG.trace("Returning subscription token.");
        
        return token;
    }

    /**
     * Determine the correct routing information based on the "handled types" provided by the EventHandler.
     * @param eventHandler EventHandler that provided the types that need to be mapped to routes
     * @param routeSuffix Suffix to append on route bindings
     * @return Array of Routes that apply to that Handler
     */
    private RoutingInfo[] getRoutesBaseOnEventHandlerHandledTypes(EventHandler<?> eventHandler, String routeSuffix) {

    	LOG.trace("Getting routes handled by event handler [{}]", eventHandler.getClass().getName());
    	
        ArrayList<RoutingInfo> routes = new ArrayList<RoutingInfo>();
        
        final Class<?>[] handledEventTypes = eventHandler.getHandledEventTypes();
        
        for (Class<?> eventType : handledEventTypes) {
        	
        	LOG.trace("Getting route for [{}]", eventType.getName());
        	
            RoutingInfo routingInfo = routingProvider.getRoutingInfoFor(eventTopicMapper.getTopicFor(eventType));
            
            // Assuming we want to ensure that we not only catch types that match the canonical class name
            // but also anything past it in the hierarchy. Ken?
            
            if (routeSuffix == AMQP_ROUTE_SEGMENT_WILDCARD) {
                routes.add(routingInfo);
            }
            
            routingInfo = new RoutingInfo(routingInfo.getExchange(), routingInfo.getRoutingKey()
                    + AMQP_ROUTE_SEGMENT_DELIMITER + routeSuffix);
            
            routes.add(routingInfo);
        }
        
        LOG.trace("Found [{}] routes for event handler [{}]", routes.size(), eventHandler.getClass().getName());
        
        return routes.toArray(new RoutingInfo[0]);
    }

    /**
     * Get random queue name for this client
     * @return Random Queue Name
     */
    private String getNewQueueName() {
        return clientName + ":" + UUID.randomUUID().toString();
    }

    /**
     * Assuming that this is a default FallbackHandler for EventHandlers
     * that can't handle the message, but haven't supplied a fallback handler.
     * @return CURRENTLY RETURNS NULL!
     */
    private FallbackHandler getFailingFallbackHandler() {
        // TODO Ask Ken about this method's purpose
        return null;
    }

    private HashSet<String> exchangesKnownToExist = new HashSet<String>();

    /**
     * Ensure the supplied route currently exists.
     * @param routingInfo Route Info
     */
    private void ensureRouteExists(RoutingInfo routingInfo) {
    	
    	LOG.trace("Ensuring route exists: {}", routingInfo);
    	
        final String exchangeName = routingInfo.getExchange().getName();
        
        if (exchangesKnownToExist.contains(exchangeName)){
         
        	LOG.trace("Route already exists. Done.");
        	
        	return;
        }

        LOG.debug("Route did not exist, attempting to create exchange [{}] to ensure it exists on the bus.",
        		routingInfo.getExchange().getName());
        
        messageBus.createExchange(routingInfo.getExchange());
        
        LOG.trace("Adding route to the known routes list.");
        
        exchangesKnownToExist.add(exchangeName);
    }

    /**
     * Handle the Responses to the supplied event with the provided Event Handler
     * @param event Event published
     * @param handler Event Handler taking care of responses.
     * @return Subscription Token that can be used to unbind the handler from the bus.
     */
    @Override
    public SubscriptionToken getResponseTo(Object event, EventHandler<?> handler) {
    	
    	LOG.debug("Publishing event of type [{}] and handling responses with [{}]", 
    			event.getClass().getName(), handler.getClass().getName());
    	
        String replyToQueueName = getNewQueueName();
        
        LOG.trace("Creating reply-to queue with name [{}].", replyToQueueName);
        
        Subscription subscription = new Subscription(handler);
        subscription.setQueueName(replyToQueueName);
        
        LOG.trace("Creating subscription to responses on the reply-to queue.");
        
        SubscriptionToken token = subscribe(subscription, replyToQueueName);
        
        LOG.trace("Blocking the active until consumer is registered.");
        
        while (!activeSubscriptions.get(token).listener.isCurrentlyListening()) {
            try {
            	
            	LOG.trace("Sleeping for 10ms");
            	
                Thread.sleep(10);
                
            } catch (InterruptedException e) {
            	
            	LOG.error("Thread was interrupted when waiting for responses to the reply-to queue.", e);
            	
                break;
            }
        }
        
        LOG.trace("Publishing the event and waiting for responses.");
        
        publish(event, replyToQueueName, false);
        
        LOG.trace("Returning Subscription Token");
        
        return token;
    }

    
    /**
     * Handle the Responses to the supplied event, but return the result immediately (blocking until result
     * has been received - RPC)
     * @param event Event published
     * @param timeoutMills Time to wait for result before quitting
     * @param responseTypes The expected response types
     * @return Return an event of the expected TResponse type
     */
    @Override
    public <TResponse> TResponse getResponseTo(Object event, int timeoutMills,
            Class<? extends TResponse>... responseTypes) throws InterruptedException, TimeoutException {

    	LOG.debug("Publishing event of type [{}] and expecting a response of types [{}]", 
    			event.getClass().getName(), joinEventTypesAsString(responseTypes));
    
    	LOG.trace("Registering a CallbackHandler for collecting the Responses");
    	
        CallbackHandler<TResponse> handler = new CallbackHandler<TResponse>(responseTypes);
        
        SubscriptionToken token = getResponseTo(event, handler);
        
        try {
        
        	LOG.trace("Waiting for response {}ms, then returning result.", timeoutMills);
        	
        	return waitForAndReturnResponse(handler, timeoutMills);
        
        } finally {
        
        	LOG.debug("Unregistering subscription for getResponseTo");
        	
        	unsubscribe(token);
        }
    }

    /**
     * Waits for a response to occur (if the timeout doesn't occur first), then returns the result.
     * @param handler CallbackHandler that will collect the result
     * @param timeoutMills Time in milliseconds to wait for response.
     * @return The response from another service on the bus.
     * @throws InterruptedException
     * @throws TimeoutException
     */
    private <TResponse> TResponse waitForAndReturnResponse(CallbackHandler<TResponse> handler, int timeoutMills)
            throws InterruptedException, TimeoutException {
    	
        int sleepInterval = Math.min(50, timeoutMills / 10);

        StopWatch watch = new StopWatch();
        watch.start();
        
        LOG.trace("Attempting to get response.");
        
        TResponse response = handler.getReceivedResponse();
        
        if(response == null){
        	
        	LOG.trace("Response was null, looping until response received or timeout reached.");
        }
        
        while (watch.getTime() < timeoutMills && response == null) {
            
        	LOG.trace("Still nothing, sleeping {}ms.", sleepInterval);
        	
        	Thread.sleep(sleepInterval);
            
        	LOG.trace("Attempting to get Response from bus again.");
        	
            response = handler.getReceivedResponse();
        }
        
        watch.stop();

        if (response == null) {
         
        	LOG.error("Response was not received within the time specified.");
        	
        	throw new TimeoutException("Response was not received within the time specified.");
        }

        LOG.trace("Response of type [{}] received.", response.getClass().getName());
        
        return response;
    }

    /**
     * Respond to an event with another event.
     * @param originalRequest Original Event
     * @param response The Event used to Respond to the first.
     */
    @Override
    public void respondTo(Object originalRequest, Object response) {
    	
    	LOG.debug("Responding to event [{}] with event [{}]", 
    			originalRequest.getClass().getName(),
    			response.getClass().getName());
    	
    	LOG.trace("Pulling original event's envelope.");
    	
        Envelope originalRequestEnvelope = envelopesBeingHandled.get(originalRequest);
        
        if (originalRequestEnvelope.getReplyTo() == null) {
        
        	LOG.warn("No reply-to address on the original event, publishing response as normal event.");
        	
        	publish(response);
        
        } else {
            
        	LOG.trace("Publishing response [{}] to reply-to queue [{}].", 
        			response.getClass().getName(), originalRequestEnvelope.getReplyTo());
        	
        	publish(response, originalRequestEnvelope.getReplyTo(), true);
        }
    }

    /**
     * Unsubscribe a handler from a particular event using the Subscription Token.
     * @param token Subscription Token
     */
    @Override
    public void unsubscribe(SubscriptionToken token) {
        unsubscribe(token, false);
    }

    /**
     * Unsubscribe a handler from a particular event using the Subscription Token.
     * @param token Subscription Token
     * @param deleteQueue Should the queue be removed too? 
     */
    @Override
    public void unsubscribe(SubscriptionToken token, boolean deleteQueue) {
    	
    	LOG.debug("Unsubscribing handlers corresponding to this token: {}", token);
    	
        ActiveSubscription subscription = activeSubscriptions.get(token);
        
        if (subscription == null) {
        
        	LOG.error("The provided token does not refer to an active subscription of this event manager.");
        	
            throw new IllegalStateException(
                    "The provided token does not refer to an active subscription of this event manager.");
        }
        
        
        ArrayList<ActiveSubscription> subscriptions = new ArrayList<ActiveSubscription>();
        
        subscriptions.add(subscription);
        
        LOG.trace("Deactivating the handlers corresponding to the subscription (delete queue? = {})", deleteQueue);
        
        deactivateSubscriptions(subscriptions, deleteQueue);
        
        LOG.trace("Removing token from the 'active subscriptions' list.");
        
        activeSubscriptions.remove(token);
    }

    /**
     * Close the Event Manager and stop all active subscriptions.
     */
    @Override
    public void close() {

    	LOG.info("Shutting down the Event Manager.");
    	
    	LOG.trace("Deactivating all subscriptions.");
    	
        deactivateSubscriptions(activeSubscriptions.values(), false);
        
        activeSubscriptions.clear();
        
        LOG.trace("Closing the connection to the broker.");
        
        messageBus.close();
    }

    /**
     * Deactivate activate subscriptions listed in the provided subscriptions list (and optionally, delete the queues).
     * @param subscriptions List of subscriptions to be deactivated
     * @param deleteDurableQueues Should these queues be deleted?
     */
    private void deactivateSubscriptions(Collection<ActiveSubscription> subscriptions, boolean deleteDurableQueues) {
        
    	LOG.debug("Deactivating subscriptions, stopping all listeners.");
    	
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
                    	
                    	LOG.trace("Some of the subscriptions are taking a while to shutdown.  Sleeping for 50ms.");
                    	
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        LOG.debug("Thread [" + Thread.currentThread().getName()
                                + "] interrupted in method AmqpEventManager.deactivateSubscriptions().");
                    }
                    break;
                }
            }
        }

        if(deleteDurableQueues){
        	
        	LOG.trace("Deleting all queues provided in the deactivated subscriptions list.");
        }
        
        for (ActiveSubscription subscription : subscriptions) {
            if (deleteDurableQueues || !subscription.getQueueIsDurable()) {
            	
            	LOG.trace("Deleting queue [{}]", subscription.getQueueName());
            	
                messageBus.deleteQueue(subscription.getQueueName());
            }
        }
    }
    
    /**
     * Joins a list of event types into a comma separated string
     * @param eventTypes List of event types
     * @return list of event types as a comma separated string
     */
    private static String joinEventTypesAsString(Class<?>[] eventTypes){
    	StringBuilder sb = new StringBuilder();
    	for(Class<?> eventType : eventTypes){
    		sb.append(", ").append(eventType.getName());
    	}
    	return sb.substring(1);
    }

    /**
     * 
     * @author Ken Baltrinic (Berico Technologies)
     * @param <TResponse> Response Type Handled by the Callback
     */
    private class CallbackHandler<TResponse> implements EventHandler<TResponse> {

        private final Class<? extends TResponse>[] handledTypes;
        private volatile TResponse receivedResponse;

        /**
         * Types handled by the Respond To Handler
         * @param handledTypes
         */
        public CallbackHandler(Class<? extends TResponse>... handledTypes) {

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

    /**
     * Represents a Subscription with an active queue listener,
     * pulling messages from the Bus.
     * @author Ken Baltrinic (Berico Technologies)
     */
    private class ActiveSubscription {

        private final String queueName;
        private final Boolean queueIsDurable;
        final QueueListener listener;
        private boolean isActive = true;

        /**
         * Instantiate the class supplying the queue information and listener
         * @param queueName Name of the Queue being watched
         * @param queueIsDurable Is the Queue durable?
         * @param listener The listener watching for events
         */
        public ActiveSubscription(String queueName, Boolean queueIsDurable, QueueListener listener) {

            this.queueName = queueName;
            this.queueIsDurable = queueIsDurable;
            this.listener = listener;
        }

        /**
         * Get the Queue Name
         * @return Queue Name
         */
        public String getQueueName() {
            return queueName;
        }

        /**
         * Is the Queue Durable?
         * @return true if it is durable
         */
        public Boolean getQueueIsDurable() {
            return queueIsDurable;
        }

        /**
         * Get the Listener watching the Queue
         * @return Listener
         */
        public QueueListener getListener() {
            return listener;
        }

        /**
         * Is the subscription currently Active?
         * @return true if active.
         */
        public boolean isActive() {
            return isActive;
        }

        /**
         * Toggle whether the queue is active
         * @param queueIsDeleted Has the queue been deleted?
         */
        public void setIsActive(boolean queueIsDeleted) {
            this.isActive = queueIsDeleted;
        }

    }
    
    /**
     * Watches a Queue for new messages on a background thread, calling
     * the EnvelopeHandler when new messages arrive.
     * @author Ken Baltrinic (Berico Technologies)
     */
    protected class QueueListener implements Runnable {
    	
    	protected final Logger QL_LOG;
    	
        private final String queueName;
        private final String threadName;
        private EnvelopeHandler envelopeHandler;

        private volatile boolean currentlyListening;
        private volatile boolean continueListening;

        private Thread backgroundThread;

        /**
         * Start up an new Queue Listener bound on the supplied queue name,
         * with the provided EnvelopeHander dealing with new messages.
         * @param queueName Name of the Queue to watch.
         * @param envelopeHandler EnvelopeHandler that deals with new messages.
         */
        public QueueListener(String queueName, EnvelopeHandler envelopeHandler) {

        	//Custom Logger for Each Queue Listener.
        	QL_LOG = LoggerFactory.getLogger(String.format("%s$>%s", this.getClass().getName(), queueName));
        	
            this.queueName = queueName;
            this.threadName = "Listener for queue: " + queueName;
            this.envelopeHandler = envelopeHandler;
        }

        /**
         * Begin listening for messages on the Queue.
         */
        public void beginListening() {
        	
        	QL_LOG.debug("QueueListener commanded to start on a new thread.");
        	
            if (backgroundThread != null)
                return;

            continueListening = true;

            backgroundThread = new Thread(this);
            backgroundThread.setName(threadName);
            backgroundThread.start();
        }

        /**
         * Executed on a separate thread.
         */
        @Override
        public void run() {
        	
            QL_LOG.info("Starting to listen on thread [{}].", Thread.currentThread().getName());
            
            currentlyListening = true;
            while (continueListening) {
            	
                try {
                    UnacceptedMessage message;
                    synchronized (this) {
                    	
                    	QL_LOG.trace("Getting next message for queue [{}]", queueName);
                    	
                        // see not in StopListening() as to why we are
                        // synchronizing here.
                        message = messageBus.getNextMessageFrom(queueName);
                    }
                    if (message == null) {
                    	
                    	QL_LOG.debug("No messages received.  Waiting 50ms.");
                    	
                        try {
                            Thread.sleep(50);
                            
                        } catch (InterruptedException e) {
                            
                        	QL_LOG.debug("Thread [{}] interrupted in method AmqpEventManager$QueueListener.run().", 
                            		threadName);
                            
                        	break;
                        } finally {
                        	//?
                        }
                        continue;
                    }

                    QL_LOG.debug("Message received.");
                    
                    EventResult result;
                    
                    Envelope envelope = message.getEnvelope();
                    
                    try {
                    
                    	QL_LOG.trace("Handling envelope.");
                    	
                    	result = envelopeHandler.handleEnvelope(envelope);
                    } catch (Exception e) {
                        
                    	result = EventResult.Failed;
                        
                        String id;
                        
                        try {
                        
                        	id = envelope.getId().toString();
                        
                        } catch (Exception ee) {
                            
                        	id = "<message id not available>";
                        }
                        
                        QL_LOG.error("Envelope handler of type " + envelopeHandler.getClass().getCanonicalName()
                                + " on queue " + queueName + " threw exception of type "
                                + e.getClass().getCanonicalName() + " handling message " + id, e);
                    }
                    
                    QL_LOG.trace("Determining how to handle EventResult [{}]", result);

                    switch (result) {
                    case Handled:
                    
                    	QL_LOG.trace("Accepting Message [{}]", message.getAcceptanceToken());
                    	
                    	messageBus.acceptMessage(message);
                    	
                        break;
                    case Failed:
                        
                    	QL_LOG.trace("Rejecting Message [{}]", message.getAcceptanceToken());
                    	
                    	messageBus.rejectMessage(message, false);
                        
                    	break;
                    case Retry:
                        
                    	QL_LOG.trace("Retrying Message [{}]", message.getAcceptanceToken());
                    	
                    	messageBus.rejectMessage(message, true);
                        
                    	break;
                    }
                } catch (Exception e) {
                	
                    QL_LOG.error("Envelope handler of type " + envelopeHandler.getClass().getCanonicalName()
                            + " on queue " + queueName + " threw exception of type " + e.getClass().getCanonicalName()
                            + " while retrieving next message.");
                }
            }
            currentlyListening = false;
            backgroundThread = null;
            
            QL_LOG.info("Stopped listening on thread [" + threadName + "].");
        }

        /**
         * Command the QueueListener to stop listening on the queue, thereby
         * stopping the background thread.
         */
        public void StopListening() {
            continueListening = false;

            QL_LOG.debug("Interrupting thread [" + threadName + "].");
            
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
            synchronized (this) {
                
            	if (backgroundThread == null) {
                	
                    QL_LOG.debug("backgroundThread was null for thread [" + threadName + "].");
                    
                } else {
                    
                	backgroundThread.interrupt();
                }
            }

        }

        /**
         * Is the QueueListener currently monitoring the Queue?
         * @return true is it is monitoring queue.
         */
        public boolean isCurrentlyListening() {
            return currentlyListening;
        }
    }

    /**
     * The default implementation of the EnvelopeHandler.  When an event occurs
     * on the bus, an EventEnvelopeHandler specific to that subscription is
     * responsible for attempting to execute the the EventHandler, and if that
     * fails, falling back to the FallbackHandler (if present).
     * @author Ken Baltrinic (Berico Technologies)
     */
    private class EventEnvelopeHandler implements EnvelopeHandler {
    	
    	private final Logger EEH_LOG;
    	
        private final EventHandler<?> eventHandler;
        private final FallbackHandler fallbackHandler;
        private final ArrayList<Class<?>> handledTypes;
        private Method handlerMethod;

        public EventEnvelopeHandler(EventHandler<?> eventHandler, FallbackHandler fallbackHandler) {

        	EEH_LOG =  LoggerFactory.getLogger(String.format("{}", EventEnvelopeHandler.class));
        	
        	EEH_LOG.trace("EventEnvelopeHandler instantiated for EventHandler of type {} and FallbackHandler of type {}", 
        			eventHandler.getClass().getName(), fallbackHandler.getClass().getName());
        	
            this.eventHandler = eventHandler;
            this.fallbackHandler = fallbackHandler;

            handledTypes = new ArrayList<Class<?>>();
            for (Class<?> eventType : eventHandler.getHandledEventTypes()) {
                handledTypes.add(eventType);
            }

            EEH_LOG.trace("Locating the 'Genericized' handleEvent method in the list of EventHandler's methods.");
            
            for (Method method : eventHandler.getClass().getMethods()) {
                if (method.getName() == "handleEvent") {
                	
                	EEH_LOG.trace("Found the 'handleEvent' method, saving a reference to it.");
                	
                    handlerMethod = method;
                    break;
                }
            }

            // This should never actually happen
            if (handlerMethod == null) {
            	
            	EEH_LOG.error("EventHandler [{}] does not have a method called 'handleEvent', violating the contract of the EventHandler interface.",
            			eventHandler.getClass().getName());
            	
                throw new RuntimeException("eventHandler method not found on EvenHandler of type "
                        + eventHandler.getClass());
            }
        }

        /**
         * Implementation of FallbackDetails, which describe
         * the reasons why an EventHandler may have not been
         * able to handle a particular event.
         * @author Ken Baltrinic (Berico Technologies)
         */
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

        /**
         * An event has occurred on the Event Bus, and now it is
         * time to handle the message Envelope.  We first begin
         * by determining whether we can actually handle the event
         * with the provided EventHandler.  If the event can be handled,
         * we attempt to deserialize the event and then provide it
         * to the EventHandler.  If the event could not be deserialized,
         * or errors in the EventHandler, we attempt to "fallback" on an
         * FallbackHandler (if set).  The result of either the EventHandler
         * or the FallbackHandler (success or fail) is returned.
         * @param envelope The envelope that represents the message.
         * @return Resulting state of how the Event was handled.
         */
        @Override
        public EventResult handleEnvelope(Envelope envelope) {

        	EEH_LOG.debug("Handling envelope of type [{}]", envelope.getEventType());
        	
            Details fallbackDetails = new Details();

            try {
                Object event = null;
                boolean eventIsOfWrongType = false;
                
                try {
                    String className = envelope.getEventType();
                    
                    EEH_LOG.trace("Determining if the event type is a class on this Java process's classpath.");
                    
                    Class<? extends Object> eventType = Class.forName(className);
                    
                    EEH_LOG.trace("Event Class was found on classpath.");
                    
                    EEH_LOG.trace("Determining if the EventHandler can handle the received Event Type.");
                    
                    if (handledTypes.contains(eventType)) {
                    	
                    	EEH_LOG.trace("The EventHandler can handle type, attempting to deserialize.");
                    	
                        event = serializer.deserialize(envelope.getBody(), eventType);
                        
                        EEH_LOG.trace("Event deserialized without error: {}", event);
                        
                    } else {
                    	
                    	EEH_LOG.trace("Event cannot be handled by this EventHandler [{}]", 
                    			eventHandler.getClass().getName());
                    	
                        eventIsOfWrongType = true;
                    }
                } catch (Exception e) {
                	
                	EEH_LOG.error("Could not handle event type with the supplied EventHandler (deserialization or forname exception).", e);
                	
                    fallbackDetails.setReason(FallbackReason.DeserializationError);
                    fallbackDetails.setException(e);
                    
                    EEH_LOG.trace("FallbackHandler called to handle Envelope.");
                    
                    return fallbackHandler.handleEnvelope(envelope, fallbackDetails);
                }

                if (eventIsOfWrongType) {
                	
                	EEH_LOG.trace("FallbackHandler called to handle Envelope.");
                	
                    fallbackDetails.setReason(FallbackReason.EventNotOfHandledType);
                    return fallbackHandler.handleEnvelope(envelope, fallbackDetails);
                }

                EEH_LOG.trace("Adding envelope to envelopesBeingHandled map.");
                
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
                	
                	EEH_LOG.debug("Presenting the strongly-typed event to the EventHandler.");
                	
                    EventResult result = (EventResult) handlerMethod.invoke(eventHandler, event);
                    
                    if (result == EventResult.Failed) {
                    
                    	EEH_LOG.debug("EventHandler [{}] declared that it failed to handle the Event [{}].",
                    			eventHandler.getClass().getName(), event.getClass().getName());
                    	
                    	fallbackDetails.setReason(FallbackReason.EventHandlerReturnedFailure);
                    	
                    	EEH_LOG.debug("FallbackHandler called to handle Envelope.");
                    	
                        return fallbackHandler.handleEnvelope(envelope, fallbackDetails);
                        
                    } else {
                        
                    	EEH_LOG.debug("EventHandler [{}] successfully handled the Event [{}].",
                    			eventHandler.getClass().getName(), event.getClass().getName());
                    	
                    	return result;
                    }
                } catch (Exception e) {
                	
                	EEH_LOG.error("EventHandler failed to handle event (exception thrown in handler).", e);
                	
                    fallbackDetails.setReason(FallbackReason.EventHandlerThrewException);
                    fallbackDetails.setException(e);
                    
                    EEH_LOG.debug("FallbackHandler called to handle Envelope.");
                    
                    return fallbackHandler.handleEnvelope(envelope, fallbackDetails);
                    
                } finally {
                	
                	EEH_LOG.trace("Removing envelope from envelopesBeingHandled map.");
                	
                    envelopesBeingHandled.remove(event);
                }
            } catch (Exception e) {
            	
                EEH_LOG.error("Unable to handle message: {}", envelope, e);
                
                return EventResult.Failed;
            }
        }
    }
}
