package pegasus.eventbus.amqp;

import java.util.HashMap;

public class ConnectionParameters {

    private final HashMap<String, String> parametersMap = new HashMap<String, String>();

    public ConnectionParameters() {

    }

    public ConnectionParameters(HashMap<String, String> parametersMap) {
        this.parametersMap.putAll(parametersMap);
    }

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

    public String getUsername() {
        return getValue("username", "guest");
    }

    public void setUsername(String username) {
        setValue("username", username);
    }

    public String getPassword() {
        return getValue("password", "guest");
    }

    public void setPassword(String password) {
        setValue("password", password);
    }

    public String getHost() {
        return getValue("host", "rabbit");
    }

    public void setHost(String host) {
        setValue("host", host);
    }

    public String getVirtualHost() {
        return getValue("vhost", "/");
    }

    public void setVirtualHost(String vhost) {
        setValue("vhost", vhost);
    }

    public int getPort() {
        return Integer.parseInt(getValue("port", "5672"));
    }

    public void setPort(int port) {
        setValue("port", Integer.toString(port));
    }

    public String getValue(String key, String defaultValue) {
        return parametersMap.containsKey(key) ? parametersMap.get(key) : defaultValue;
    }

    public void setValue(String key, String value) {
        parametersMap.put(key, value);
    }

}
