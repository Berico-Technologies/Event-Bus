package pegasus.eventbus.topology.service;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Activator implements BundleActivator {

    protected static final Logger      LOG                         = LoggerFactory.getLogger(Activator.class);

    private static ServiceRegistration topologyServiceRegistration = null;

    @SuppressWarnings("rawtypes")
    public void start(BundleContext bundleContext) throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("topologyservice-context.xml");
        TopologyService topologyService = context.getBean(TopologyService.class);
        topologyService.start();

        topologyServiceRegistration = bundleContext.registerService(TopologyService.class.getName(), topologyService, new Hashtable());

        LOG.info("TopologyService Started.");
    }

    public void stop(BundleContext bundleContext) throws Exception {
        if (topologyServiceRegistration != null) {
            bundleContext.ungetService(topologyServiceRegistration.getReference());
        }

        LOG.info("TopologyService Stopped.");
    }

}
