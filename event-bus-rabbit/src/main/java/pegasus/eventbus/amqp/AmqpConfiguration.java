package pegasus.eventbus.amqp;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pegasus.eventbus.client.EventBusConfiguration;
import pegasus.eventbus.gson.GsonSerializer;
import pegasus.eventbus.rabbitmq.RabbitConnection;
import pegasus.eventbus.rabbitmq.RabbitMessageBus;
import pegasus.eventbus.topology.CompositeTopologyManager;
import pegasus.eventbus.topology.FallbackTopologyManager;
import pegasus.eventbus.topology.GlobalTopologyServiceManager;
import pegasus.eventbus.topology.StaticTopologyManager;

/**
 * Container for all the nasty settings and providers necessary to make the AmqpEventManager work. We recommend using the default configuration, accessed by the static functions "getDefault", which
 * simplifies
 * 
 * @author Asa Martin (Berico Technologies)
 */
public class AmqpConfiguration implements EventBusConfiguration {

    private static final Logger      LOG                          = LoggerFactory.getLogger(AmqpConfiguration.class);

    // Must start with Alpha, Digit or _ and be no more than 255 chars. Special
    // chars, spaces, etc. are allowed.
    // We are limiting name to 215 chars to allow us to append UUID.
    private static final Pattern     VALID_AMQP_NAME              = Pattern.compile("^\\w{1}.{0,214}+$");
    // AMQP name may not start with amq. as this is reserved
    private static final Pattern     FIRST_CHARS_INVALID_FOR_AMQP = Pattern.compile("^(\\W|(amq\\.))");

    // Assumes command is anything prior to the first whitespace and then
    // extracts the final ., / or \ delimited segment thereof
    // however . appearing within the final 8 characters of command are included
    // in command as a presumed extension.
    private static final Pattern     NAME_FROM_COMMAND            = Pattern
                                                                          .compile("((?:^([^\\s./\\\\]+?(?:\\.[^\\s./\\\\]{0,7})*?))|((?:(?:^\\S*?[./\\\\])|^)([^\\s./\\\\]+?(?:\\.[^\\s./\\\\]{0,7})*?)))(?:\\s|$)");

    private String                   clientName;
    private AmqpConnectionParameters connectionParameters;
    private AmqpMessageBus           amqpMessageBus;
    private TopologyManager          topologyManager;
    private Serializer               serializer;

    /**
     * Get the Name of the Client.
     * 
     * @return Client Name
     */
    public String getClientName() {
        return clientName;
    }

    /**
     * Set the Name of the Client.
     * 
     * @param clientName
     *            Client Name
     */
    public void setClientName(String clientName) {
        this.clientName = clientName;

        String tempName = getFallBackClientNameIfNeeded(clientName);
        this.clientName = fixNameIfInvalidForAmqp(tempName);

        // Because fixNameIfInvalidForAmqp should fixing any invalid names, the
        // validation method
        // is not really needed any more but we are keeping it in place just in
        // case something
        // gets past it.
        validateClientName();
    }

    /**
     * Get the AMQP Connection Parameters.
     * 
     * @return Connection Parameters
     */
    public AmqpConnectionParameters getConnectionParameters() {
        return connectionParameters;
    }

    /**
     * Set the AMQP Connection Parameters
     * 
     * @param connectionParameters
     */
    public void setConnectionParameters(AmqpConnectionParameters connectionParameters) {
        this.connectionParameters = connectionParameters;
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
     *            AMQP provider
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
     *            Serializer used to SerDe objects
     */
    public void setSerializer(Serializer serializer) {
        this.serializer = serializer;
    }

    /**
     * If client name is null, attempt to pull the host name from the environment or fall back to "UNKNOWN"
     * 
     * @param clientName
     *            Name of this instance of the AmqpEventManager
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
     *            Current Client Name
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
    private void validateClientName() {

        LOG.trace("Validating Client Name.");

        if (!VALID_AMQP_NAME.matcher(clientName).find()) {

            LOG.error("The client name must begin with a letter number or underscore and be no more than 215 characters long.");

            throw new IllegalArgumentException("The clientName must begin with a letter number or underscore and be no more than 215 characters long.");

        } else if (clientName.startsWith("amq.")) {

            LOG.error("The client name may not begin with 'amq.' as this is a reserved namespace.");

            throw new IllegalArgumentException("The clientName may not begin with 'amq.' as this is a reserved namespace.");
        }
    }

    /**
     * Get the default configuration for the EventManager
     * 
     * @param clientName
     *            Unique name for this client instance
     * @return Default Configuration
     */
    public static AmqpConfiguration getDefault(String clientName) {
        return getDefault(clientName, new AmqpConnectionParameters());
    }

    /**
     * Get the default configuration for the EventManager
     * 
     * @param clientName
     *            Unique name for this client instance
     * @param connectionParameters
     *            Connection Parameters.
     * @return Default Configuration
     */
    public static AmqpConfiguration getDefault(String clientName, AmqpConnectionParameters connectionParameters) {
        RabbitConnection rabbitConnection = new RabbitConnection(connectionParameters);
        AmqpMessageBus amqpMessageBus = new RabbitMessageBus(rabbitConnection);
        CompositeTopologyManager compositeTopologyManager = new CompositeTopologyManager();
        TopologyManager fixedTopologyManager = new StaticTopologyManager();
        compositeTopologyManager.addManager(fixedTopologyManager);
        TopologyManager globalTopologyService = new GlobalTopologyServiceManager(clientName);
        compositeTopologyManager.addManager(globalTopologyService);
        TopologyManager fallbackToplogyService = new FallbackTopologyManager();
        compositeTopologyManager.addManager(fallbackToplogyService);
        Serializer serializer = new GsonSerializer();

        AmqpConfiguration defaultConfiguration = new AmqpConfiguration();
        defaultConfiguration.setClientName(clientName);
        defaultConfiguration.setConnectionParameters(connectionParameters);
        defaultConfiguration.setAmqpMessageBus(amqpMessageBus);
        defaultConfiguration.setTopologyManager(compositeTopologyManager);
        defaultConfiguration.setSerializer(serializer);

        return defaultConfiguration;
    }

}
