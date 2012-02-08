package eventbus.client.amqp;

import eventbus.client.api.EventHandler;
import eventbus.client.api.EventResult;

/**
 * 
 * @author Ken Baltrinic (Berico Technologies)
 * @param <TResponse> Response Type Handled by the Callback
 */
class CallbackHandler<TResponse> implements EventHandler<TResponse> {

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