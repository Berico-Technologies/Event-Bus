package pegasus.eventbus.topology.integration_tests;

import static org.junit.Assert.*;

import java.util.concurrent.TimeoutException;

import org.junit.*;

import pegasus.eventbus.topology.events.RegisterClient;
import pegasus.eventbus.topology.events.TopologyUpdate;

public class GlobalTopologyManagerTest extends IntegrationTestBase {

    @Ignore
    @Test
    public void RegisteringAClientShouldReturnATopologyUpdateWithARegistry() throws InterruptedException, TimeoutException {
        @SuppressWarnings("unchecked")
        TopologyUpdate result = manager.getResponseTo(new RegisterClient("client-registration-test-client", "V1"), 1000, TopologyUpdate.class);
        assertNotNull(result);
        assertNotNull(result.getTopologyRegistry());
    }
}
