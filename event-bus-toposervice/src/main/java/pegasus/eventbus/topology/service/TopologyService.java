package pegasus.eventbus.topology.service;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class TopologyService {

    protected static final Logger LOG = LoggerFactory.getLogger(TopologyService.class);

    private final RegistrationHandler   registrationHandler;
    private final UnknownEventTypeHandler unknownEventTypeHandler;
    
    public TopologyService(
    		RegistrationHandler registrationHandler,
    		UnknownEventTypeHandler unknownEventTypeHandler) {

        LOG.debug("Initializing topology service object.");

        this.registrationHandler = registrationHandler;
        this.unknownEventTypeHandler = unknownEventTypeHandler;
    }

    public void start() {

        LOG.trace("Starting topology service.");
        registrationHandler.start();
        unknownEventTypeHandler.start();
    }

    public void stop() {

        LOG.trace("Stopping topology service.");

        unknownEventTypeHandler.stop();
        registrationHandler.stop();
    }

    public static void main(String[] args) throws IOException {
    	ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("console-topologyservice-context.xml");
        System.out.println("Topology Service started... Hit any key to stop.");
        System.in.read();
        context.close();
    }

}
