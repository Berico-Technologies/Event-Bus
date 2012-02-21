package pegasus.eventbus.amqp;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Configuration for AMQP specific connection parameters.
 * 
 * @author Asa Martin (Berico Technologies)
 */
public class ConnectionParameters {

    public static final String USERNAME_PROPERTY = "event.bus.username";
    public static final String PASSWORD_PROPERTY = "event.bus.password";
    public static final String HOST_PROPERTY = "event.bus.host";
    public static final String PORT_PROPERTY = "event.bus.port";
    public static final String VHOST_PROPERTY = "event.bus.vhost";
    public static final String CONNECTION_RETRY_TIMEOUT_PROPERTY = "event.bus.connectionRetryTimeout";

    private static final Dictionary<String, String> DEFAULT_VALUES = initializeDefaults();

    private final Dictionary<String, String>        parametersMap  = new Hashtable<String, String>();

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
    public ConnectionParameters(Dictionary<String, String> parametersMap) {
        Enumeration<String> keys = parametersMap.keys();
        for (String key = keys.nextElement(); keys.hasMoreElements(); keys.nextElement()) {
            this.parametersMap.put(key, parametersMap.get(key));
        }
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
        return getValue(USERNAME_PROPERTY, DEFAULT_VALUES.get(USERNAME_PROPERTY));
    }

    /**
     * Set the username of the Account accessing the bus.
     * 
     * @param username
     *            user account
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
     *            Password
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
     *            Hostname
     */
    public void setHost(String host) {
        setValue(HOST_PROPERTY, host);
    }

    /**
     * Get the virtual host (if not the default) on the AMQP broker (this setting is typically specific to RabbitMQ)
     * 
     * @return Virtual Host
     */
    public String getVirtualHost() {
        return getValue(VHOST_PROPERTY, DEFAULT_VALUES.get(VHOST_PROPERTY));
    }

    /**
     * Set the virtual host name to use on the AMQP broker (this setting is typically specific to RabbitMQ)
     * 
     * @param vhost
     *            Virtual Host
     */
    public void setVirtualHost(String vhost) {
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
     *            Port Number
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
        return Long.parseLong(getValue(CONNECTION_RETRY_TIMEOUT_PROPERTY, DEFAULT_VALUES.get(CONNECTION_RETRY_TIMEOUT_PROPERTY)));
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
     * Get the value of a configuration parameter
     * 
     * @param key
     *            Key of the parameter
     * @param defaultValue
     *            default value to return if no value is present in the configuration
     * @return Value (either in the map or default)
     */
    public String getValue(String key, String defaultValue) {
        return parametersMap.get(key) != null ? parametersMap.get(key) : defaultValue;
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

        setValue(USERNAME_PROPERTY, uri.substring(position, usernameSep));

        position = usernameSep + 1;

        int hostSep = uri.indexOf("@", position);

        setValue(PASSWORD_PROPERTY, uri.substring(position, hostSep));

        position = hostSep + 1;

        int portSep = uri.indexOf(":", position);

        setValue(HOST_PROPERTY, uri.substring(position, portSep));

        position = portSep + 1;

        int vhostSep = uri.indexOf("/", position);

        setValue(PORT_PROPERTY, uri.substring(position, vhostSep));

        position = vhostSep;

        if (vhostSep == -1) {

            setValue(VHOST_PROPERTY, "/");

        } else {

            setValue(VHOST_PROPERTY, uri.substring(position));
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
    private static Dictionary<String, String> initializeDefaults() {

        Dictionary<String, String> defaults = new Hashtable<String, String>();

        defaults.put(USERNAME_PROPERTY, "guest");
        defaults.put(PASSWORD_PROPERTY, "guest");
        defaults.put(HOST_PROPERTY, "rabbit.pegasus.gov");
        defaults.put(PORT_PROPERTY, "5672");
        defaults.put(VHOST_PROPERTY, "/");
        defaults.put(CONNECTION_RETRY_TIMEOUT_PROPERTY, "30000");

        return defaults;
    }

}
