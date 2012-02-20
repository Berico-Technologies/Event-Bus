package pegasus.eventbus.topology.osgi;

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pegasus.eventbus.client.EventManager;
import pegasus.eventbus.topology.TopologyRegistry;
import pegasus.eventbus.topology.service.ClientRegistry;
import pegasus.eventbus.topology.service.RegistrationHandler;
import pegasus.eventbus.topology.service.TopologyService;
import pegasus.eventbus.topology.service.UnknownEventTypeHandler;

public class Activator implements BundleActivator {

    protected static final Logger  LOG = LoggerFactory.getLogger(Activator.class);

    private static TopologyService topologyService;

    public void start(BundleContext bundleContext) throws Exception {
        register(bundleContext, defaults());
    }

    public void stop(BundleContext bundleContext) throws Exception {
        unregister(bundleContext);
    }

    private void register(BundleContext bundleContext, Dictionary<String, String> config) {

        LOG.info("Registering Topology Service: {}", TopologyService.class.getName());

        ServiceReference eventManagerServiceReference = bundleContext.getServiceReference(EventManager.class.getName());
        if (eventManagerServiceReference != null) {
            EventManager eventManager = (EventManager) bundleContext.getService(eventManagerServiceReference);
            TopologyRegistry topologyRegistry = new TopologyRegistry();
            ClientRegistry clientRegistry = new ClientRegistry();
            RegistrationHandler registrationHandler = new RegistrationHandler(eventManager, clientRegistry, topologyRegistry);
            UnknownEventTypeHandler unknownEventTypeHandler = new UnknownEventTypeHandler(eventManager, topologyRegistry);
            topologyService = new TopologyService(registrationHandler, unknownEventTypeHandler);
            topologyService.start();

            LOG.info("Topology Service Registered: {}", TopologyService.class.getName());

        } else {

            LOG.error("Unable to find EventManager service.");

        }
    }

    private void unregister(BundleContext bundleContext) {

        LOG.info("Unregistering Topology Service: {}", TopologyService.class.getName());

        if (topologyService != null) {
            topologyService.stop();
        }

        LOG.info("Topology Service Unregistered: {}", TopologyService.class.getName());
    }

    private Dictionary<String, String> defaults() {
        Dictionary<String, String> defaults = new Hashtable<String, String>();
        return defaults;
    }

}
