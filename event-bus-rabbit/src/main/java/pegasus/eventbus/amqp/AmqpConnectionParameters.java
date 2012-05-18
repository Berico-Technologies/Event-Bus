package pegasus.eventbus.amqp;

import java.net.URL;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

import pegasus.eventbus.client.EventBusConnectionParameters;

/**
 * Configuration for AMQP specific connection parameters.
 * 
 * @author Asa Martin (Berico Technologies)
 */
public class AmqpConnectionParameters implements EventBusConnectionParameters {

    private static final Dictionary<String, String> DEFAULT_VALUES = initializeDefaults();

    private final Dictionary<String, String>        parametersMap  = new Hashtable<String, String>();  
    
    private boolean ssl;
    private boolean oneWaySSL;
    public boolean isOneWaySSL() {
		return oneWaySSL;
	}

	public void setOneWaySSL(boolean oneWaySSL) {
		this.oneWaySSL = oneWaySSL;
	}

	private URL trustStore;
    private String trustStorePassword;
    private URL keyStore;
    private String keyStorePassword;
    
    
	/**
     * Default Constructor
     */
    public AmqpConnectionParameters() {

    }

    /**
     * Initialize with a HashMap (Good for IOC containers)
     * 
     * @param parametersMap
     *            Parameter Map
     */
    public AmqpConnectionParameters(Dictionary<String, String> parametersMap) {
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
    public AmqpConnectionParameters(String connectionParameters) {

        if (connectionParameters == null) {
            throw new IllegalArgumentException("ConnectionParameters has no value.");
        }

        if (connectionParameters.startsWith("amqp://")) {

            parseAmqpUriString(connectionParameters);
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
    public String getVHost() {
        return getValue(VHOST_PROPERTY, DEFAULT_VALUES.get(VHOST_PROPERTY));
    }

    /**
     * Set the virtual host name to use on the AMQP broker (this setting is typically specific to RabbitMQ)
     * 
     * @param vhost
     *            Virtual Host
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
    
	public boolean isSsl() {
		return ssl;
	}

	public void setSsl(boolean ssl) {
		this.ssl = ssl;
	}

	public URL getTrustStore() {
		return trustStore;
	}

	public void setTrustStore(URL trustStore) {
		this.trustStore = trustStore;
	}

	public String getTrustStorePassword() {
		return trustStorePassword;
	}

	public void setTrustStorePassword(String trustStorePassword) {
		this.trustStorePassword = trustStorePassword;
	}

	public URL getKeyStore() {
		return keyStore;
	}

	public void setKeyStore(URL keyStore) {
		this.keyStore = keyStore;
	}

	public String getKeyStorePassword() {
		return keyStorePassword;
	}

	public void setKeyStorePassword(String keyStorePassword) {
		this.keyStorePassword = keyStorePassword;
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
    private void parseAmqpUriString(String uri) {

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
    private void parseDelimitedPropertyString(String propertyString) {

        String[] connectionPairs = propertyString.split(";");
        for (String connectionPair : connectionPairs) {
            if (connectionPair == null || connectionPair.trim().equals("")) {
                continue;
            }
            String[] parameterParts = connectionPair.split("=");
            if (parameterParts.length != 2) {
                throw new IllegalArgumentException(String.format("Invalid connection string element: '%s' should be 'key=value'", connectionPair));
            }
            if (parameterParts[0].equals("username")) {
                parametersMap.put(USERNAME_PROPERTY, parameterParts[1]);
            } else if (parameterParts[0].equals("password")) {
                parametersMap.put(PASSWORD_PROPERTY, parameterParts[1]);
            } else if (parameterParts[0].equals("host")) {
                parametersMap.put(HOST_PROPERTY, parameterParts[1]);
            } else if (parameterParts[0].equals("port")) {
                parametersMap.put(PORT_PROPERTY, parameterParts[1]);
            } else if (parameterParts[0].equals("vhost")) {
                parametersMap.put(VHOST_PROPERTY, parameterParts[1]);
            } else {
                parametersMap.put(parameterParts[0], parameterParts[1]);
            }
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
        defaults.put(HOST_PROPERTY, "rabbit");
        defaults.put(PORT_PROPERTY, "5672");
        defaults.put(VHOST_PROPERTY, "/");
        defaults.put(CONNECTION_RETRY_TIMEOUT_PROPERTY, "30000");
        defaults.put(SSL_PROPERTY, Boolean.FALSE.toString());

        return defaults;
    }
}
