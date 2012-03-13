package pegasus.esp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;

import orion.esp.monitors.InferredEventCatcher;
import orion.esp.monitors.StorageRepository;

import pegasus.eventbus.client.Envelope;

import com.google.common.collect.Lists;

/**
 * This class does setup/teardown and supplies utilities for event detector
 * test classes.
 *
 * @author israel
 *
 */
public abstract class AbstractDetectorTest {

    protected final Log logger = LogFactory.getLog(this.getClass());

    private EventStreamProcessor esp;
    protected ArrayList<InferredEvent> envelopesDetected = Lists.newArrayList();
    protected StorageRepository testRepository;

    public AbstractDetectorTest() {
        super();
    }

    @Before
    public void setUp() throws Exception {
        testRepository = new StorageRepository();
        testRepository.addMonitor(new InferredEventCatcher(envelopesDetected));
        testRepository.addMonitor(new InferredEventPrinter());
        esp = new EventStreamProcessor(testRepository);
    }

    @After
    public void tearDown() throws Exception {
    }

    protected ArrayList<InferredEvent> sendAndExpectNo(Envelope e, String type) {
        return sendAndExpect(e, 0, type);
    }

    protected ArrayList<InferredEvent> sendAndExpect(Envelope e, int count, String type) {
        clearAndSend(e);
        ArrayList<InferredEvent> detected = assertDetectedEvents(count, type);
        return detected;
    }

    protected ArrayList<InferredEvent> assertDetectedEvents(int count, String type) {
        ArrayList<InferredEvent> detected = getDetected(type);
        assertEquals("Checking number of InferredEvents of type '" + type + "'",
                count, detected.size());
        return detected;
    }

    protected void assertNoDetectedEvents(String type) {
        assertDetectedEvents(0, type);
    }

    protected InferredEvent assertOneDetectedEvent(String type) {
        return assertDetectedEvents(1, type).get(0);
    }

    protected void assertInferredEventReferences(InferredEvent event, Envelope env) {
        assertTrue("Checking to see if " + event + " references " + env,
                event.getReferencedEvents().contains(env));
    }

    protected void clearAndSend(Envelope e) {
        envelopesDetected.clear();
        // log a separator for each event in the unit tests
        logger.info("============================================================================================");
        logger.info(" --> Event: " + e);
        send(e);
    }

    protected void send(Envelope e) {
        esp.sendEvent(e);
    }

    private ArrayList<InferredEvent> getDetected(String type) {
        ArrayList<InferredEvent> res = Lists.newArrayList();
        for (InferredEvent inferredEvent : envelopesDetected) {
            if (type.equals(inferredEvent.getType())) {
                res.add(inferredEvent);
            }
        }
        return res;
    }
}
