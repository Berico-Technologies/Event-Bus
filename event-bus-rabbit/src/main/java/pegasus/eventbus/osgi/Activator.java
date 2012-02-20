package pegasus.eventbus.osgi;

import java.util.Dictionary;
import java.util.Hashtable;

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
        register(bundleContext, defaults());
    }

    public void stop(BundleContext bundleContext) throws Exception {
        unregister(bundleContext);
    }

    private void register(BundleContext bundleContext, Dictionary<String, String> config) {

        LOG.info("Registering OSGi Service: {}", EventManager.class.getName());

        ConnectionParameters connectionParameters = new ConnectionParameters(config);
        eventManager = new AmqpEventManager(AmqpConfiguration.getDefault(config.get("clientName"), connectionParameters));
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

    private Dictionary<String, String> defaults() {
        Dictionary<String, String> defaults = new Hashtable<String, String>();
        defaults.put("clientName", "unique-application-name");
        defaults.put("username", "guest");
        defaults.put("password", "guest");
        defaults.put("host", "rabbit.pegasus.gov");
        defaults.put("port", "5672");
        defaults.put("vhost", "/");
        defaults.put("connectionRetryTimeout", "30000");
        return defaults;
    }

}
