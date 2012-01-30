package pegasus.eventbus.amqp;

import pegasus.eventbus.gson.GsonSerializer;
import pegasus.eventbus.rabbitmq.RabbitMessageBus;
import pegasus.eventbus.routing.BasicTopicToRoutingMapper;
import pegasus.eventbus.routing.BasicTypeToTopicMapper;

/**
 * Container for all the nasty settings and providers necessary
 * to make the AmqpEventManager work.  We recommend using the default
 * configuration, accessed by the static functions "getDefault", which
 * simplifies  
 * @author Asa Martin (Berico Technologies)
 */
public class Configuration {

    private String clientName;
    private ConnectionParameters connectionParameters;
    private AmqpMessageBus amqpMessageBus;
    private EventTypeToTopicMapper eventTypeToTopicMapper;
    private TopicToRoutingMapper topicToRoutingMapper;
    private Serializer serializer;
   
    /**
     * Get the Name of the Client.
     * @return Client Name
     */
    public String getClientName() {
        return clientName;
    }

    /**
     * Set the Name of the Client.
     * @param clientName Client Name
     */
    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    /**
     * Get the AMQP Connection Parameters.
     * @return Connection Parameters
     */
    public ConnectionParameters getConnectionParameters() {
        return connectionParameters;
    }

    /**
     * Set the AMQP Connection Parameters
     * @param connectionParameters 
     */
    public void setConnectionParameters(ConnectionParameters connectionParameters) {
        this.connectionParameters = connectionParameters;
    }

    /**
     * Get the AMQP provider.
     * @return AMQP provider
     */
    public AmqpMessageBus getAmqpMessageBus() {
        return amqpMessageBus;
    }

    /**
     * Set the AMQP provider
     * @param amqpMessageBus AMQP provider
     */
    public void setAmqpMessageBus(AmqpMessageBus amqpMessageBus) {
        this.amqpMessageBus = amqpMessageBus;
    }

    /**
     * Get the Mapper between Event Types and Topics
     * @return Mapper
     */
    public EventTypeToTopicMapper getEventTypeToTopicMapper() {
        return eventTypeToTopicMapper;
    }

    /**
     * Set the Mapper between Event Types and Topics
     * @param eventTypeToTopicMapper Mapper
     */
    public void setEventTypeToTopicMapper(EventTypeToTopicMapper eventTypeToTopicMapper) {
        this.eventTypeToTopicMapper = eventTypeToTopicMapper;
    }

    /**
     * Get the Mapper between Topics and Routing Information
     * @return Mapper
     */
    public TopicToRoutingMapper getTopicToRoutingMapper() {
        return topicToRoutingMapper;
    }

    /**
     * Set the Mapper between Topics and Routing Information
     * @param topicToRoutingMapper Mapper
     */
    public void setTopicToRoutingMapper(TopicToRoutingMapper topicToRoutingMapper) {
        this.topicToRoutingMapper = topicToRoutingMapper;
    }

    /**
     * Get the Serializer.
     * @return The Serializer
     */
    public Serializer getSerializer() {
        return serializer;
    }

    /**
     * Set the Serializer.
     * @param serializer Serializer used to SerDe objects
     */
    public void setSerializer(Serializer serializer) {
        this.serializer = serializer;
    }

    /**
     * Get the default configuration for the EventManager
     * @param clientName Unique name for this client instance
     * @return Default Configuration
     */
    public static Configuration getDefault(String clientName) {
        return getDefault(clientName, new ConnectionParameters());
    }

    /**
     * Get the default configuration for the EventManager
     * @param clientName Unique name for this client instance
     * @param connectionParameters Connection Parameters.
     * @return Default Configuration
     */
    public static Configuration getDefault(String clientName, ConnectionParameters connectionParameters) {
        AmqpMessageBus amqpMessageBus = new RabbitMessageBus(connectionParameters);
        EventTypeToTopicMapper eventTypeToTopicMapper = new BasicTypeToTopicMapper();
        TopicToRoutingMapper topicToRoutingMapper = new BasicTopicToRoutingMapper();
        Serializer serializer = new GsonSerializer();

        Configuration defaultConfiguration = new Configuration();
        defaultConfiguration.setClientName(clientName);
        defaultConfiguration.setConnectionParameters(connectionParameters);
        defaultConfiguration.setAmqpMessageBus(amqpMessageBus);
        defaultConfiguration.setEventTypeToTopicMapper(eventTypeToTopicMapper);
        defaultConfiguration.setTopicToRoutingMapper(topicToRoutingMapper);
        defaultConfiguration.setSerializer(serializer);

        return defaultConfiguration;
    }

   
    
}
