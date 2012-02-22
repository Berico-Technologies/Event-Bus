package pegasus.eventbus.amqp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pegasus.eventbus.client.EventBusConnectionParameters;
import pegasus.eventbus.client.EventBusFactory;
import pegasus.eventbus.client.EventManager;

public class AmqpFactory implements EventBusFactory {

    private static final Logger LOG = LoggerFactory.getLogger(AmqpFactory.class);

    public EventManager getEventManager(String clientName, EventBusConnectionParameters connectionParameters) {

        LOG.trace("Getting new instance of EventManager for {}.", clientName);

        // @todo - very ugly cast. should handle this more elegantly
        return new AmqpEventManager(AmqpConfiguration.getDefault(clientName, (AmqpConnectionParameters) connectionParameters));
    }

}
