package pegasus.eventbus.amqp;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import pegasus.eventbus.client.EventManager;

public class Activator implements BundleActivator {

    protected static final Logger      LOG                          = LoggerFactory.getLogger(Activator.class);

    private static ServiceRegistration amqpEventManagerRegistration = null;

    @SuppressWarnings("rawtypes")
    public void start(BundleContext bundleContext) throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("eventbus-context.xml");
        EventManager eventManager = context.getBean(EventManager.class);
        eventManager.start();

        amqpEventManagerRegistration = bundleContext.registerService(AmqpEventManager.class.getName(), eventManager, new Hashtable());

        LOG.info("AMQP EventManager Started.");
    }

    public void stop(BundleContext bundleContext) throws Exception {
        if (amqpEventManagerRegistration != null) {
            bundleContext.ungetService(amqpEventManagerRegistration.getReference());
        }

        LOG.info("AMQP EventManager Stopped.");
    }

}
