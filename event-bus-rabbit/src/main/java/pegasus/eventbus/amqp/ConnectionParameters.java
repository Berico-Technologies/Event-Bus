package pegasus.eventbus.amqp;

import java.util.HashMap;

/**
 * Configuration for AMQP specific connection parameters.
 * @author Asa Martin (Berico Technologies)
 */
public class ConnectionParameters {

    private final HashMap<String, String> parametersMap = new HashMap<String, String>();

    /**
     * Default Constructor
     */
    public ConnectionParameters() {}

    /**
     * Initialize with a HashMap (Good for IOC containers)
     * @param parametersMap
     */
    public ConnectionParameters(HashMap<String, String> parametersMap) {
        this.parametersMap.putAll(parametersMap);
    }

    /**
     * Initialize Parameters using a Semicolon delimited property hash:
     * "host=eventbus.orion.mil;port=5672;username=service1;password=guest;vhost=default"
     * @param connectionParameters Semicolon delimited property hash
     */
    public ConnectionParameters(String connectionParameters) {
        if (connectionParameters == null) {
            throw new IllegalArgumentException("ConnectionParameters has no value.");
        }

        String[] connectionPairs = connectionParameters.split(";");
        for (String connectionPair : connectionPairs) {
            if (connectionPair == null || connectionPair.trim().equals("")) {
                continue;
            }
            String[] parameterParts = connectionPair.split("=");
            if (parameterParts.length != 2) {
                throw new IllegalArgumentException(String.format(
                        "Invalid connection string element: '%s' should be 'key=value'", connectionPair));
            }
            parametersMap.put(parameterParts[0], parameterParts[1]);
        }
    }

    /**
     * Get the Username of the Account accessing the bus
     * @return Username
     */
    public String getUsername() {
        return getValue("username", "guest");
    }

    /**
     * Set the username of the Account accessing the bus.
     * @param username user account
     */
    public void setUsername(String username) {
        setValue("username", username);
    }

    /**
     * Get the password of the Account accessing the bus.
     * @return Password
     */
    public String getPassword() {
        return getValue("password", "guest");
    }

    /**
     * Set the password of the Account accessing the bus.
     * @param password Password
     */
    public void setPassword(String password) {
        setValue("password", password);
    }

    /**
     * Get the hostname of the AMQP broker
     * @return Hostname
     */
    public String getHost() {
        return getValue("host", "rabbit");
    }
    
    /**
     * Set the hostname of the AMQP broker
     * @param host Hostname
     */
    public void setHost(String host) {
        setValue("host", host);
    }

    /**
     * Get the virtual host (if not the default) on the AMQP broker
     * (this setting is typically specific to RabbitMQ)
     * @return Virtual Host
     */
    public String getVirtualHost() {
        return getValue("vhost", "/");
    }

    /**
     * Set the virtual host name to use on the AMQP broker
     * (this setting is typically specific to RabbitMQ)
     * @param vhost Virtual Host
     */
    public void setVirtualHost(String vhost) {
        setValue("vhost", vhost);
    }

    /**
     * Get the port of the AMQP broker
     * @return Port number
     */
    public int getPort() {
        return Integer.parseInt(getValue("port", "5672"));
    }

    /**
     * Set the port of the AMQP broker
     * @param port Port Number
     */
    public void setPort(int port) {
        setValue("port", Integer.toString(port));
    }

    /**
     * Get the value of a configuration parameter
     * @param key Key of the parameter
     * @param defaultValue default value to return if no value is present in the configuration
     * @return Value (either in the map or default)
     */
    public String getValue(String key, String defaultValue) {
        return parametersMap.containsKey(key) ? parametersMap.get(key) : defaultValue;
    }

    /**
     * Set a configuration parameter
     * @param key Parameter
     * @param value Value of the Parameter
     */
    public void setValue(String key, String value) {
        parametersMap.put(key, value);
    }

}
