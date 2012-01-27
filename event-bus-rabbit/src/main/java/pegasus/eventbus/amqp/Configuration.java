package pegasus.eventbus.amqp;

import pegasus.eventbus.gson.GsonSerializer;
import pegasus.eventbus.rabbitmq.RabbitMessageBus;
import pegasus.eventbus.routing.BasicTopicToRoutingMapper;
import pegasus.eventbus.routing.BasicTypeToTopicMapper;

public class Configuration {

    private String clientName;
    private ConnectionParameters connectionParameters;
    private AmqpMessageBus amqpMessageBus;
    private EventTypeToTopicMapper eventTypeToTopicMapper;
    private TopicToRoutingMapper topicToRoutingMapper;
    private Serializer serializer;

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public ConnectionParameters getConnectionParameters() {
        return connectionParameters;
    }

    public void setConnectionParameters(ConnectionParameters connectionParameters) {
        this.connectionParameters = connectionParameters;
    }

    public AmqpMessageBus getAmqpMessageBus() {
        return amqpMessageBus;
    }

    public void setAmqpMessageBus(AmqpMessageBus amqpMessageBus) {
        this.amqpMessageBus = amqpMessageBus;
    }

    public EventTypeToTopicMapper getEventTypeToTopicMapper() {
        return eventTypeToTopicMapper;
    }

    public void setEventTypeToTopicMapper(EventTypeToTopicMapper eventTypeToTopicMapper) {
        this.eventTypeToTopicMapper = eventTypeToTopicMapper;
    }

    public TopicToRoutingMapper getTopicToRoutingMapper() {
        return topicToRoutingMapper;
    }

    public void setTopicToRoutingMapper(TopicToRoutingMapper topicToRoutingMapper) {
        this.topicToRoutingMapper = topicToRoutingMapper;
    }

    public Serializer getSerializer() {
        return serializer;
    }

    public void setSerializer(Serializer serializer) {
        this.serializer = serializer;
    }

    public static Configuration getDefault(String clientName) {
        return getDefault(clientName, new ConnectionParameters());
    }

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
