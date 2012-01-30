package pegasus.eventbus.amqp;

import java.lang.reflect.Method;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pegasus.eventbus.client.Envelope;
import pegasus.eventbus.client.EnvelopeHandler;
import pegasus.eventbus.client.EventHandler;
import pegasus.eventbus.client.EventResult;
import pegasus.eventbus.client.FallbackDetails;
import pegasus.eventbus.client.FallbackHandler;
import pegasus.eventbus.client.FallbackDetails.FallbackReason;

/**
 * The default implementation of the EnvelopeHandler.  When an event occurs
 * on the bus, an EventEnvelopeHandler specific to that subscription is
 * responsible for attempting to execute the the EventHandler, and if that
 * fails, falling back to the FallbackHandler (if present).
 * @author Ken Baltrinic (Berico Technologies)
 */
public class EventEnvelopeHandler implements EnvelopeHandler {
	
	private final Logger LOG;
	
	private final AmqpEventManager amqpEventManager;
    private final EventHandler<?> eventHandler;
    private final FallbackHandler fallbackHandler;
    private final ArrayList<Class<?>> handledTypes;
    private Method handlerMethod;

    public EventEnvelopeHandler(AmqpEventManager amqpEventManager, EventHandler<?> eventHandler, FallbackHandler fallbackHandler) {

    	this.amqpEventManager = amqpEventManager;
    	
		LOG =  LoggerFactory.getLogger(String.format("{}", EventEnvelopeHandler.class));
    	
    	LOG.trace("EventEnvelopeHandler instantiated for EventHandler of type {} and FallbackHandler of type {}", 
    			eventHandler.getClass().getName(), 
    			(fallbackHandler != null)? fallbackHandler.getClass().getName() : "null");
    	
        this.eventHandler = eventHandler;
        this.fallbackHandler = fallbackHandler;

        handledTypes = new ArrayList<Class<?>>();
        for (Class<?> eventType : eventHandler.getHandledEventTypes()) {
            handledTypes.add(eventType);
        }

        LOG.trace("Locating the 'Genericized' handleEvent method in the list of EventHandler's methods.");
        
        for (Method method : eventHandler.getClass().getMethods()) {
            if (method.getName() == "handleEvent") {
            	
            	LOG.trace("Found the 'handleEvent' method, saving a reference to it.");
            	
                handlerMethod = method;
                break;
            }
        }

        // This should never actually happen
        if (handlerMethod == null) {
        	
        	LOG.error("EventHandler [{}] does not have a method called 'handleEvent', violating the contract of the EventHandler interface.",
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

    	LOG.debug("Handling envelope of type [{}]", envelope.getEventType());
    	
        Details fallbackDetails = new Details();

        try {
            Object event = null;
            boolean eventIsOfWrongType = false;
            
            try {
                String className = envelope.getEventType();
                
                LOG.trace("Determining if the event type is a class on this Java process's classpath.");
                
                Class<? extends Object> eventType = Class.forName(className);
                
                LOG.trace("Event Class was found on classpath.");
                
                LOG.trace("Determining if the EventHandler can handle the received Event Type.");
                
                if (handledTypes.contains(eventType)) {
                	
                	LOG.trace("The EventHandler can handle type, attempting to deserialize.");
                	
                    event = this.amqpEventManager.serializer.deserialize(envelope.getBody(), eventType);
                    
                    LOG.trace("Event deserialized without error: {}", event);
                    
                } else {
                	
                	LOG.trace("Event cannot be handled by this EventHandler [{}]", 
                			eventHandler.getClass().getName());
                	
                    eventIsOfWrongType = true;
                }
            } catch (Exception e) {
            	
            	LOG.error("Could not handle event type with the supplied EventHandler (deserialization or forname exception).", e);
            	
                fallbackDetails.setReason(FallbackReason.DeserializationError);
                fallbackDetails.setException(e);
                
                LOG.trace("FallbackHandler called to handle Envelope.");
                
                return fallbackHandler.handleEnvelope(envelope, fallbackDetails);
            }

            if (eventIsOfWrongType) {
            	
            	LOG.trace("FallbackHandler called to handle Envelope.");
            	
                fallbackDetails.setReason(FallbackReason.EventNotOfHandledType);
                return fallbackHandler.handleEnvelope(envelope, fallbackDetails);
            }

            LOG.trace("Adding envelope to envelopesBeingHandled map.");
            
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
            this.amqpEventManager.envelopesBeingHandled.put(event, envelope);

            try {
            	
            	LOG.debug("Presenting the strongly-typed event to the EventHandler.");
            	
                EventResult result = (EventResult) handlerMethod.invoke(eventHandler, event);
                
                if (result == EventResult.Failed) {
                
                	LOG.debug("EventHandler [{}] declared that it failed to handle the Event [{}].",
                			eventHandler.getClass().getName(), event.getClass().getName());
                	
                	fallbackDetails.setReason(FallbackReason.EventHandlerReturnedFailure);
                	
                	LOG.debug("FallbackHandler called to handle Envelope.");
                	
                    return fallbackHandler.handleEnvelope(envelope, fallbackDetails);
                    
                } else {
                    
                	LOG.debug("EventHandler [{}] successfully handled the Event [{}].",
                			eventHandler.getClass().getName(), event.getClass().getName());
                	
                	return result;
                }
            } catch (Exception e) {
            	
            	LOG.error("EventHandler failed to handle event (exception thrown in handler).", e);
            	
                fallbackDetails.setReason(FallbackReason.EventHandlerThrewException);
                fallbackDetails.setException(e);
                
                LOG.debug("FallbackHandler called to handle Envelope.");
                
                return fallbackHandler.handleEnvelope(envelope, fallbackDetails);
                
            } finally {
            	
            	LOG.trace("Removing envelope from envelopesBeingHandled map.");
            	
                this.amqpEventManager.envelopesBeingHandled.remove(event);
            }
        } catch (Exception e) {
        	
            LOG.error("Unable to handle message: {}", envelope, e);
            
            return EventResult.Failed;
        }
    }
}