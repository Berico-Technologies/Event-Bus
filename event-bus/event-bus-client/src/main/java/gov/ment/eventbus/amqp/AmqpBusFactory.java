package gov.ment.eventbus.amqp;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gov.ment.eventbus.client.EventBusFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AmqpBusFactory implements EventBusFactory {

  private static final Logger LOG = LoggerFactory.getLogger(AmqpBusFactory.class);

  private static final Map<String, String> DEFAULT_VALUES = initializeDefaults();

  private final Map<String, String> parameters = new HashMap<String, String>();

  private AmqpMessageBus amqpMessageBus;
  private TopologyManager topologyManager;
  private Serializer serializer;

  /**
   * Default Constructor
   */
  public AmqpBusFactory() {

  }

  /**
   * Initialize with a HashMap (Good for IOC containers)
   * 
   * @param parameters
   *          Parameter Map
   */
  public AmqpBusFactory(Map<String, String> parameters) {
    for (Entry<String, String> entry : parameters.entrySet()) {
      this.parameters.put(entry.getKey(), entry.getValue());
    }
  }

  @Override
  public AmqpEventManager getNewEventManager() {
    LOG.debug("Generating new AmqpEventManager...");
    AmqpEventManager eventManager =
            new AmqpEventManager(getClientName(), getAmqpMessageBus(), getTopologyManager(),
                    getSerializer());
    LOG.debug("Successfully created new AmqpEventManager {}", eventManager);

    return eventManager;
  }

  @Override
  public AmqpEventManager getNewEventManager(String clientName) {
    setClientName(clientName);

    return getNewEventManager();
  }

  /**
   * Get the Username of the Account accessing the bus
   * 
   * @return Username
   */
  public String getUsername() {
    return getValue(USERNAME_PROPERTY, DEFAULT_VALUES.get(USERNAME_PROPERTY));
  }

  /**
   * Set the username of the Account accessing the bus.
   * 
   * @param username
   *          user account
   */
  public void setUsername(String username) {
    setValue(USERNAME_PROPERTY, username);
  }

  /**
   * Get the password of the Account accessing the bus.
   * 
   * @return Password
   */
  public String getPassword() {
    return getValue(PASSWORD_PROPERTY, DEFAULT_VALUES.get(PASSWORD_PROPERTY));
  }

  /**
   * Set the password of the Account accessing the bus.
   * 
   * @param password
   *          Password
   */
  public void setPassword(String password) {
    setValue(PASSWORD_PROPERTY, password);
  }

  /**
   * Get the hostname of the AMQP broker
   * 
   * @return Hostname
   */
  public String getHost() {
    return getValue(HOST_PROPERTY, DEFAULT_VALUES.get(HOST_PROPERTY));
  }

  /**
   * Set the hostname of the AMQP broker
   * 
   * @param host
   *          Hostname
   */
  public void setHost(String host) {
    setValue(HOST_PROPERTY, host);
  }

  /**
   * Get the virtual host (if not the default) on the AMQP broker (this setting
   * is typically specific to RabbitMQ)
   * 
   * @return Virtual Host
   */
  public String getVHost() {
    return getValue(VHOST_PROPERTY, DEFAULT_VALUES.get(VHOST_PROPERTY));
  }

  /**
   * Set the virtual host name to use on the AMQP broker (this setting is
   * typically specific to RabbitMQ)
   * 
   * @param vhost
   *          Virtual Host
   */
  public void setVHost(String vhost) {
    setValue(VHOST_PROPERTY, vhost);
  }

  /**
   * Get the port of the AMQP broker
   * 
   * @return Port number
   */
  public int getPort() {
    return Integer.parseInt(getValue(PORT_PROPERTY, DEFAULT_VALUES.get(PORT_PROPERTY)));
  }

  /**
   * Set the port of the AMQP broker
   * 
   * @param port
   *          Port Number
   */
  public void setPort(int port) {
    setValue(PORT_PROPERTY, Integer.toString(port));
  }

  /**
   * Get the connection retry timeout
   * 
   * @return connectionRetryTimeout
   */
  public long getConnectionRetryTimeout() {
    return Long.parseLong(getValue(CONNECTION_RETRY_TIMEOUT_PROPERTY,
            DEFAULT_VALUES.get(CONNECTION_RETRY_TIMEOUT_PROPERTY)));
  }

  /**
   * Set the connection retry timeout
   * 
   * @param connectionRetryTimeout
   */
  public void setConnectionRetryTimeout(long connectionRetryTimeout) {
    setValue(CONNECTION_RETRY_TIMEOUT_PROPERTY, Long.toString(connectionRetryTimeout));
  }

  /**
   * Get the client name string
   * 
   * @return clientName
   */
  public String getClientName() {
    return getValue(CLIENT_NAME_PROPERTY, DEFAULT_VALUES.get(CLIENT_NAME_PROPERTY));
  }

  /**
   * Set the client name string
   * 
   * @param clientName
   */
  public void setClientName(String clientName) {
    clientName = getFallBackClientNameIfNeeded(clientName);
    clientName = fixNameIfInvalidForAmqp(clientName);
    validateClientName(clientName);
    setValue(CLIENT_NAME_PROPERTY, clientName);
  }

  /**
   * Get the AMQP provider.
   * 
   * @return AMQP provider
   */
  public AmqpMessageBus getAmqpMessageBus() {
    return amqpMessageBus;
  }

  /**
   * Set the AMQP provider
   * 
   * @param amqpMessageBus
   *          AMQP provider
   */
  public void setAmqpMessageBus(AmqpMessageBus amqpMessageBus) {
    this.amqpMessageBus = amqpMessageBus;
  }

  /**
   * Get the Topology Manager
   * 
   * @return TopologyManager
   */
  public TopologyManager getTopologyManager() {
    return topologyManager;
  }

  /**
   * Set the Topology Manager
   * 
   * @param TopologyManager
   */
  public void setTopologyManager(TopologyManager topologyManager) {
    this.topologyManager = topologyManager;
  }

  /**
   * Get the Serializer.
   * 
   * @return The Serializer
   */
  public Serializer getSerializer() {
    return serializer;
  }

  /**
   * Set the Serializer.
   * 
   * @param serializer
   *          Serializer used to SerDe objects
   */
  public void setSerializer(Serializer serializer) {
    this.serializer = serializer;
  }

  /**
   * Get the value of a factory parameter
   * 
   * @param key
   *          Key of the parameter
   * @param defaultValue
   *          default value to return if no value is present in the factory
   * @return Value (either in the map or default)
   */
  @Override
  public String getValue(String key, String defaultValue) {
    return parameters.get(key) != null ? parameters.get(key) : defaultValue;
  }

  /**
   * Set a factory parameter
   * 
   * @param key
   *          Parameter
   * @param value
   *          Value of the Parameter
   */
  @Override
  public void setValue(String key, String value) {
    parameters.put(key, value);
  }

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
  private static final Pattern NAME_FROM_COMMAND =
          Pattern.compile("((?:^([^\\s./\\\\]+?(?:\\.[^\\s./\\\\]{0,7})*?))|((?:(?:^\\S*?[./\\\\])|^)([^\\s./\\\\]+?(?:\\.[^\\s./\\\\]{0,7})*?)))(?:\\s|$)");

  /**
   * If client name is null, attempt to pull the host name from the environment
   * or fall back to "UNKNOWN"
   * 
   * @param clientName
   *          Name of this instance of the AmqpEventManager
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
   * 
   * @param clientName
   *          Current Client Name
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
  private void validateClientName(String clientName) {
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
   * Initialize the Default Map of Parameters.
   * 
   * @return Default Parameter Map
   */
  private static Map<String, String> initializeDefaults() {
    Map<String, String> defaults = new HashMap<String, String>();

    defaults.put(USERNAME_PROPERTY, "guest");
    defaults.put(PASSWORD_PROPERTY, "guest");
    defaults.put(HOST_PROPERTY, "rabbit");
    defaults.put(PORT_PROPERTY, "5672");
    defaults.put(VHOST_PROPERTY, "/");
    defaults.put(CONNECTION_RETRY_TIMEOUT_PROPERTY, "30000");

    return defaults;
  }
}
