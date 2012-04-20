package gov.ment.eventbus.topology.service.osgi;

import java.util.Map;

import gov.ment.eventbus.client.EventBusFactory;
import gov.ment.eventbus.client.EventManager;
import gov.ment.eventbus.topology.TopologyRegistry;
import gov.ment.eventbus.topology.service.ClientRegistry;
import gov.ment.eventbus.topology.service.RegistrationHandler;
import gov.ment.eventbus.topology.service.TopologyService;
import gov.ment.eventbus.topology.service.UnknownEventTypeHandler;

public class EventBusFactoryServiceListener {

  private String clientName;
  private String username;
  private String password;
  private String host;
  private String vHost;
  private String port;
  private String connectionRetryTimeout;
  private TopologyRegistry topologyRegistry;
  private UnknownEventTypeHandler unknownEventTypeHandler;
  private ClientRegistry clientRegistry;
  private RegistrationHandler registrationHandler;
  private TopologyService topologyService;
  private EventManager eventManager;

  public String getClientName() {
    return clientName;
  }

  public void setClientName(String clientName) {
    this.clientName = clientName;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public String getVHost() {
    return vHost;
  }

  public void setVHost(String vHost) {
    this.vHost = vHost;
  }

  public String getPort() {
    return port;
  }

  public void setPort(String port) {
    this.port = port;
  }

  public String getConnectionRetryTimeout() {
    return connectionRetryTimeout;
  }

  public void setConnectionRetryTimeout(String connectionRetryTimeout) {
    this.connectionRetryTimeout = connectionRetryTimeout;
  }

  public TopologyRegistry getTopologyRegistry() {
    return topologyRegistry;
  }

  public void setTopologyRegistry(TopologyRegistry topologyRegistry) {
    this.topologyRegistry = topologyRegistry;
  }

  public UnknownEventTypeHandler getUnknownEventTypeHandler() {
    return unknownEventTypeHandler;
  }

  public void setUnknownEventTypeHandler(UnknownEventTypeHandler unknownEventTypeHandler) {
    this.unknownEventTypeHandler = unknownEventTypeHandler;
  }

  public ClientRegistry getClientRegistry() {
    return clientRegistry;
  }

  public void setClientRegistry(ClientRegistry clientRegistry) {
    this.clientRegistry = clientRegistry;
  }

  public RegistrationHandler getRegistrationHandler() {
    return registrationHandler;
  }

  public void setRegistrationHandler(RegistrationHandler registrationHandler) {
    this.registrationHandler = registrationHandler;
  }

  public TopologyService getTopologyService() {
    return topologyService;
  }

  public void setTopologyService(TopologyService topologyService) {
    this.topologyService = topologyService;
  }

  public void onEventBusFactoryServiceBound(EventBusFactory eventBusFactory, Map<?, ?> properties) {
    eventBusFactory.setValue(EventBusFactory.CLIENT_NAME_PROPERTY, clientName);
    eventBusFactory.setValue(EventBusFactory.USERNAME_PROPERTY, username);
    eventBusFactory.setValue(EventBusFactory.PASSWORD_PROPERTY, password);
    eventBusFactory.setValue(EventBusFactory.HOST_PROPERTY, host);
    eventBusFactory.setValue(EventBusFactory.VHOST_PROPERTY, vHost);
    eventBusFactory.setValue(EventBusFactory.PORT_PROPERTY, port);
    eventBusFactory.setValue(EventBusFactory.CONNECTION_RETRY_TIMEOUT_PROPERTY,
            connectionRetryTimeout);
    eventManager = eventBusFactory.getNewEventManager();
    eventManager.start();
    unknownEventTypeHandler.setEventManager(eventManager);
    registrationHandler.setEventManager(eventManager);
    topologyService.start();
  }

  public void onEventBusFactoryServiceUnbound(EventBusFactory eventBusFactory, Map<?, ?> properties)
          throws Exception {
    topologyService.stop();
    eventManager.close();
  }
}
