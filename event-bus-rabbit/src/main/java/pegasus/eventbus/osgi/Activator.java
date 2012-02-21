package pegasus.eventbus.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pegasus.eventbus.amqp.AmqpConfiguration;
import pegasus.eventbus.amqp.AmqpEventManager;
import pegasus.eventbus.amqp.ConnectionParameters;
import pegasus.eventbus.client.EventManager;

public class Activator implements BundleActivator {

    private static final Logger        LOG                      = LoggerFactory.getLogger(Activator.class);

    private static ServiceRegistration eventManagerRegistration = null;
    private static EventManager        eventManager;

    public void start(BundleContext bundleContext) throws Exception {
        register(bundleContext, getConfig());
    }

    public void stop(BundleContext bundleContext) throws Exception {
        unregister(bundleContext);
    }

    private void register(BundleContext bundleContext, Dictionary<String, String> config) {

        LOG.info("Registering OSGi Service: {}", EventManager.class.getName());

        ConnectionParameters connectionParameters = new ConnectionParameters(config);
        eventManager = new AmqpEventManager(AmqpConfiguration.getDefault(config.get(AmqpConfiguration.CLIENT_NAME_PROPERTY), connectionParameters));
        eventManager.start();

        eventManagerRegistration = bundleContext.registerService(EventManager.class.getName(), eventManager, null);

        LOG.info("OSGi Service Registered: {}", EventManager.class.getName());
    }

    private void unregister(BundleContext bundleContext) {

        LOG.info("Unregistering OSGi Bundle: {}", EventManager.class.getName());

        if (eventManagerRegistration != null) {
            eventManager.close();

            bundleContext.ungetService(eventManagerRegistration.getReference());
        }

        LOG.info("OSGi Service Unregistered: {}", EventManager.class.getName());
    }

    private Dictionary<String, String> getConfig() {
        Dictionary<String, String> config = new Hashtable<String, String>();
        try {
            ResourceBundle properties = ResourceBundle.getBundle("eventbus");
            for (String key : properties.keySet()) {
                config.put(key, properties.getString(key));
            }
        } catch (MissingResourceException e) {

            LOG.warn("No RabbitMQ Client configuration found.");

        }

        return config;
    }

}
