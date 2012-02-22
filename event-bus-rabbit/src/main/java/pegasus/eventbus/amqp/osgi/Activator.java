package pegasus.eventbus.amqp.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pegasus.eventbus.amqp.AmqpFactory;
import pegasus.eventbus.client.EventBusFactory;

public class Activator implements BundleActivator {

    private static final Logger LOG                     = LoggerFactory.getLogger(Activator.class);
    private static final String ServiceRegistrationName = EventBusFactory.class.getName();
    private static final String ServiceName             = AmqpFactory.class.getName();

    private ServiceRegistration serviceRegistration;

    public void start(BundleContext bundleContext) throws Exception {

        LOG.info("Registering OSGi Service: {}", ServiceName);

        serviceRegistration = bundleContext.registerService(ServiceRegistrationName, new AmqpFactory(), null);

        LOG.info("OSGi Service Registered: {}", ServiceName);
    }

    public void stop(BundleContext bundleContext) throws Exception {

        LOG.info("Unregistering OSGi Bundle: {}", ServiceName);

        if (serviceRegistration != null) {
            bundleContext.ungetService(serviceRegistration.getReference());
        }

        LOG.info("OSGi Service Unregistered: {}", ServiceName);
    }

}
