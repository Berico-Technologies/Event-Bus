package pegasus.eventbus.topology.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pegasus.eventbus.amqp.AmqpConnectionParameters;
import pegasus.eventbus.client.EventBusConfiguration;
import pegasus.eventbus.client.EventBusConnectionParameters;
import pegasus.eventbus.client.EventBusFactory;
import pegasus.eventbus.client.EventManager;
import pegasus.eventbus.topology.TopologyRegistry;
import pegasus.eventbus.topology.service.ClientRegistry;
import pegasus.eventbus.topology.service.RegistrationHandler;
import pegasus.eventbus.topology.service.TopologyService;
import pegasus.eventbus.topology.service.UnknownEventTypeHandler;

public class Activator implements BundleActivator {

    protected static final Logger        LOG                     = LoggerFactory.getLogger(Activator.class);
    private static final String          ServiceRegistrationName = EventBusFactory.class.getName();

    private TopoServiceTrackerCustomizer topoServiceTrackerCustomizer;
    private ServiceTracker               serviceTracker;
    private TopologyService              topologyService;
    private Dictionary<String, String>   config                  = getConfig();

    public void start(BundleContext bundleContext) throws Exception {

        LOG.info("Topology Service OSGi start.");

        topoServiceTrackerCustomizer = new TopoServiceTrackerCustomizer(bundleContext);
        serviceTracker = new ServiceTracker(bundleContext, ServiceRegistrationName, topoServiceTrackerCustomizer);
        serviceTracker.open();
    }

    public void stop(BundleContext bundleContext) throws Exception {

        LOG.info("Topology Service OSGi stop.");

        serviceTracker.close();
    }

    private Dictionary<String, String> getConfig() {
        Dictionary<String, String> config = new Hashtable<String, String>();
        try {
            ResourceBundle properties = ResourceBundle.getBundle("eventbus");
            for (String key : properties.keySet()) {
                config.put(key, properties.getString(key));
            }
        } catch (MissingResourceException e) {

            LOG.warn("No Topology Service configuration found.");

        }

        return config;
    }

    private void createTopoService(EventBusFactory eventBusFactory) {
        EventBusConnectionParameters connectionParameters = new AmqpConnectionParameters(config);
        String clientName = config.get(EventBusConfiguration.CLIENT_NAME_PROPERTY);
        EventManager eventManager = eventBusFactory.getEventManager(clientName, connectionParameters);
        eventManager.start();

        TopologyRegistry topologyRegistry = new TopologyRegistry();
        ClientRegistry clientRegistry = new ClientRegistry();
        RegistrationHandler registrationHandler = new RegistrationHandler(eventManager, clientRegistry, topologyRegistry);
        UnknownEventTypeHandler unknownEventTypeHandler = new UnknownEventTypeHandler(eventManager, topologyRegistry);
        topologyService = new TopologyService(registrationHandler, unknownEventTypeHandler);
        topologyService.start();
    }

    private void destroyTopoService() {
        topologyService.stop();
    }

    private class TopoServiceTrackerCustomizer implements ServiceTrackerCustomizer {

        private BundleContext bundleContext;

        public TopoServiceTrackerCustomizer(BundleContext bundleContext) {
            this.bundleContext = bundleContext;
        }

        @Override
        public Object addingService(ServiceReference serviceReference) {

            LOG.info("Topology Service responding to {} service add.", topoServiceTrackerCustomizer);

            EventBusFactory eventBusFactory = (EventBusFactory) bundleContext.getService(serviceReference);
            createTopoService(eventBusFactory);
            return eventBusFactory;
        }

        @Override
        public void modifiedService(ServiceReference serviceReference, Object serviceObject) {

            LOG.info("Topology Service responding to {} service modified.", topoServiceTrackerCustomizer);

            // @todo - how should we handle modify?
        }

        @Override
        public void removedService(ServiceReference serviceReference, Object serviceObject) {

            LOG.info("Topology Service responding to {} service removed.", topoServiceTrackerCustomizer);

            destroyTopoService();
        }

    }

}
