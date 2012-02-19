package pegasus.eventbus.amqp;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pegasus.eventbus.client.EventManager;

public class Activator implements BundleActivator {

    private static final String        CLIENTNAME                   = "default client name";

    protected static final Logger      LOG                          = LoggerFactory.getLogger(Activator.class);

    private static ServiceRegistration amqpEventManagerRegistration = null;
    private static EventManager        eventManager;

    public void start(BundleContext bundleContext) throws Exception {

        LOG.info("OSGi Starting: {}", EventManager.class.getName());

        eventManager = new AmqpEventManager(AmqpConfiguration.getDefault(CLIENTNAME));
        eventManager.start();

        amqpEventManagerRegistration = bundleContext.registerService(EventManager.class.getName(), eventManager, null);

        LOG.info("OSGi Started: {}", EventManager.class.getName());
    }

    public void stop(BundleContext bundleContext) throws Exception {

        LOG.info("OSGi Stopping: {}", EventManager.class.getName());

        if (amqpEventManagerRegistration != null) {
            eventManager.close();

            bundleContext.ungetService(amqpEventManagerRegistration.getReference());
        }

        LOG.info("OSGi Stopped: {}", EventManager.class.getName());
    }

}
