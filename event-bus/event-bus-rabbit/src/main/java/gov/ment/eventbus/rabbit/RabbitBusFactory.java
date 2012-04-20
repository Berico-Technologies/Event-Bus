package gov.ment.eventbus.rabbit;

import gov.ment.eventbus.amqp.AmqpBusFactory;
import gov.ment.eventbus.amqp.AmqpEventManager;
import gov.ment.eventbus.amqp.Serializer;
import gov.ment.eventbus.amqp.TopologyManager;
import gov.ment.eventbus.gson.GsonSerializer;
import gov.ment.eventbus.topology.CompositeTopologyManager;
import gov.ment.eventbus.topology.FallbackTopologyManager;
import gov.ment.eventbus.topology.GlobalTopologyServiceManager;
import gov.ment.eventbus.topology.StaticTopologyManager;

public class RabbitBusFactory extends AmqpBusFactory {

  @Override
  public AmqpEventManager getNewEventManager() {
    String clientName = getClientName();
    RabbitConnection rabbitConnection = new RabbitConnection(this);
    setAmqpMessageBus(new RabbitMessageBus(rabbitConnection));
    CompositeTopologyManager compositeTopologyManager = new CompositeTopologyManager();
    TopologyManager fixedTopologyManager = new StaticTopologyManager();
    compositeTopologyManager.addManager(fixedTopologyManager);
    TopologyManager globalTopologyService = new GlobalTopologyServiceManager(clientName);
    compositeTopologyManager.addManager(globalTopologyService);
    TopologyManager fallbackToplogyService = new FallbackTopologyManager();
    compositeTopologyManager.addManager(fallbackToplogyService);
    setTopologyManager(compositeTopologyManager);
    Serializer serializer = new GsonSerializer();
    setSerializer(serializer);

    return super.getNewEventManager();
  }
}
