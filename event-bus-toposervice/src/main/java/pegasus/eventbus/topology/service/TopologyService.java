package pegasus.eventbus.topology.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class TopologyService {

    protected static final Logger LOG = LoggerFactory.getLogger(TopologyService.class);

    private RegistrationHandler   registrationHandler;

    public TopologyService(RegistrationHandler registrationHandler) {

        LOG.debug("Initializing topology service object.");

        this.registrationHandler = registrationHandler;
    }

    public void start() {

        LOG.trace("Starting topology service.");

        registrationHandler.start();
    }

    public void stop() {

        LOG.trace("Stopping topology service.");

        registrationHandler.stop();
    }

    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("topologyservice-context.xml");
        TopologyService topologyService = context.getBean(TopologyService.class);
        topologyService.start();
    }

}
