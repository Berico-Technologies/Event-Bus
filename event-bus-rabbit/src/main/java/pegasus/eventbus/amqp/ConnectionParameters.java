package pegasus.eventbus.amqp;

import java.util.HashMap;

/**
 * Configuration for AMQP specific connection parameters.
 * 
 * @author Asa Martin (Berico Technologies)
 */
public class ConnectionParameters {

    private static final HashMap<String, String> DEFAULT_VALUES = initializeDefaults();

    private final HashMap<String, String>        parametersMap  = new HashMap<String, String>();

    /**
     * Default Constructor
     */
    public ConnectionParameters() {
    }

    /**
     * Initialize with a HashMap (Good for IOC containers)
     * 
     * @param parametersMap
     *            Parameter Map
     */
    public ConnectionParameters(HashMap<String, String> parametersMap) {
        this.parametersMap.putAll(parametersMap);
    }

    /**
     * Initialize Parameters using a Semicolon delimited property hash: "host=eventbus.orion.mil;port=5672;username=service1;password=guest;vhost=default" Or URI:
     * "amqp://test:password123@rabbit-master.pegasus.mil:1234/"
     * 
     * @param connectionParameters
     *            Semicolon delimited property hash
     */
    public ConnectionParameters(String connectionParameters) {

        if (connectionParameters == null) {
            throw new IllegalArgumentException("ConnectionParameters has no value.");
        }

        if (connectionParameters.startsWith("amqp://")) {

            parseUriString(connectionParameters);
        } else {

            parseDelimitedPropertyString(connectionParameters);
        }
    }

    /**
     * Get the Username of the Account accessing the bus
     * 
     * @return Username
     */
    public String getUsername() {
        return getValue("username", DEFAULT_VALUES.get("username"));
    }

    /**
     * Set the username of the Account accessing the bus.
     * 
     * @param username
     *            user account
     */
    public void setUsername(String username) {
        setValue("username", username);
    }

    /**
     * Get the password of the Account accessing the bus.
     * 
     * @return Password
     */
    public String getPassword() {
        return getValue("password", DEFAULT_VALUES.get("password"));
    }

    /**
     * Set the password of the Account accessing the bus.
     * 
     * @param password
     *            Password
     */
    public void setPassword(String password) {
        setValue("password", password);
    }

    /**
     * Get the hostname of the AMQP broker
     * 
     * @return Hostname
     */
    public String getHost() {
        return getValue("host", DEFAULT_VALUES.get("host"));
    }

    /**
     * Set the hostname of the AMQP broker
     * 
     * @param host
     *            Hostname
     */
    public void setHost(String host) {
        setValue("host", host);
    }

    /**
     * Get the virtual host (if not the default) on the AMQP broker (this setting is typically specific to RabbitMQ)
     * 
     * @return Virtual Host
     */
    public String getVirtualHost() {
        return getValue("vhost", DEFAULT_VALUES.get("vhost"));
    }

    /**
     * Set the virtual host name to use on the AMQP broker (this setting is typically specific to RabbitMQ)
     * 
     * @param vhost
     *            Virtual Host
     */
    public void setVirtualHost(String vhost) {
        setValue("vhost", vhost);
    }

    /**
     * Get the port of the AMQP broker
     * 
     * @return Port number
     */
    public int getPort() {
        return Integer.parseInt(getValue("port", DEFAULT_VALUES.get("port")));
    }

    /**
     * Set the port of the AMQP broker
     * 
     * @param port
     *            Port Number
     */
    public void setPort(int port) {
        setValue("port", Integer.toString(port));
    }

    /**
     * Get the connection retry timeout
     * 
     * @return connectionRetryTimeout
     */
    public long getConnectionRetryTimeout() {
        return Long.parseLong(getValue("connectionRetryTimeout", DEFAULT_VALUES.get("connectionRetryTimeout")));
    }

    /**
     * Set the connection retry timeout
     * 
     * @param connectionRetryTimeout
     */
    public void setConnectionRetryTimeout(long connectionRetryTimeout) {
        setValue("connectionRetryTimeout", Long.toString(connectionRetryTimeout));
    }

    /**
     * Get the value of a configuration parameter
     * 
     * @param key
     *            Key of the parameter
     * @param defaultValue
     *            default value to return if no value is present in the configuration
     * @return Value (either in the map or default)
     */
    public String getValue(String key, String defaultValue) {
        return parametersMap.containsKey(key) ? parametersMap.get(key) : defaultValue;
    }

    /**
     * Set a configuration parameter
     * 
     * @param key
     *            Parameter
     * @param value
     *            Value of the Parameter
     */
    public void setValue(String key, String value) {
        parametersMap.put(key, value);
    }

    /**
     * Parse a URI connection string, setting the connection parameters.
     * 
     * @param uri
     *            URI to Rabbit
     */
    public void parseUriString(String uri) {

        int position = 7;

        int usernameSep = uri.indexOf(":", position);

        setValue("username", uri.substring(position, usernameSep));

        position = usernameSep + 1;

        int hostSep = uri.indexOf("@", position);

        setValue("password", uri.substring(position, hostSep));

        position = hostSep + 1;

        int portSep = uri.indexOf(":", position);

        setValue("host", uri.substring(position, portSep));

        position = portSep + 1;

        int vhostSep = uri.indexOf("/", position);

        setValue("port", uri.substring(position, vhostSep));

        position = vhostSep;

        if (vhostSep == -1) {

            setValue("vhost", "/");

        } else {

            setValue("vhost", uri.substring(position));
        }
    }

    /**
     * Parse a semicolon delimited property string for the connection info
     * 
     * @param propertyString
     *            Property String
     */
    public void parseDelimitedPropertyString(String propertyString) {

        String[] connectionPairs = propertyString.split(";");
        for (String connectionPair : connectionPairs) {
            if (connectionPair == null || connectionPair.trim().equals("")) {
                continue;
            }
            String[] parameterParts = connectionPair.split("=");
            if (parameterParts.length != 2) {
                throw new IllegalArgumentException(String.format("Invalid connection string element: '%s' should be 'key=value'", connectionPair));
            }
            parametersMap.put(parameterParts[0], parameterParts[1]);
        }
    }

    /**
     * Initialize the Default Map of Parameters.
     * 
     * @return Default Parameter Map
     */
    private static HashMap<String, String> initializeDefaults() {

        HashMap<String, String> defaults = new HashMap<String, String>();

        defaults.put("username", "guest");
        defaults.put("password", "guest");
        defaults.put("host", "rabbit.pegasus.gov");
        defaults.put("port", "5672");
        defaults.put("vhost", "/");
        defaults.put("connectionRetryTimeout", "30000");

        return defaults;
    }

}
