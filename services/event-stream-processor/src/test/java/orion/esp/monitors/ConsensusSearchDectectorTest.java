package orion.esp.monitors;

import org.junit.Test;

import orion.esp.monitors.ConsensusSearchDetector;

import pegasus.esp.AbstractDetectorTest;
import pegasus.esp.TestUtils;
import pegasus.eventbus.client.Envelope;

public class ConsensusSearchDectectorTest extends AbstractDetectorTest {

    private static final String CONSENSUS_SEARCH_EVENT = "ConsensusSearchEvent";

    @Test
    public void testSearches() {
        testRepository.addMonitor(new ConsensusSearchDetector(3, "1 hour"));

        Envelope reqMS1 = TestUtils.makeSearchRequest("Maxwell Smart", "Shoe Phone with cam", "MS1");
        sendAndExpectNo(reqMS1, CONSENSUS_SEARCH_EVENT);

        Envelope reqPW1 = TestUtils.makeSearchRequest("Peewee Herman", "sat one imagery", "PW1");
        sendAndExpectNo(reqPW1, CONSENSUS_SEARCH_EVENT);

        Envelope reqPW2 = TestUtils.makeSearchRequest("Peewee Herman", "sat two imagery", "PW2");
        sendAndExpectNo(reqPW2, CONSENSUS_SEARCH_EVENT);

        Envelope reqJB1 = TestUtils.makeSearchRequest("James Bond", "Dr No personnel file and cam feed", "JB1");
        sendAndExpectNo(reqJB1, CONSENSUS_SEARCH_EVENT);

        Envelope reqJB2 = TestUtils.makeSearchRequest("James Bond", "Kill Authorization", "JB2");
        sendAndExpectNo(reqJB2, CONSENSUS_SEARCH_EVENT);

        Envelope reqPW3 = TestUtils.makeSearchRequest("Peewee Herman", "sat three imagery", "PW3");
        sendAndExpect(reqPW3, 2, CONSENSUS_SEARCH_EVENT);
        // TODO: check contents of the two CSEs (expecting 'sat' and 'imagery' with freq 3 each)

        Envelope reqJB3 = TestUtils.makeSearchRequest("James Bond", "Underwater car", "JB3");
        sendAndExpectNo(reqJB3, CONSENSUS_SEARCH_EVENT);

        Envelope reqJB4 = TestUtils.makeSearchRequest("James Bond", "sat imagery for secret island", "JB4");
        sendAndExpect(reqJB4, 2, CONSENSUS_SEARCH_EVENT);
        // TODO: check contents of the two CSEs (expecting 'sat' and 'imagery' with freq 4 each)

        Envelope reqCB1 = TestUtils.makeSearchRequest("Chuck Bartowski", "Buy More cam", "CB1");
        sendAndExpect(reqCB1, 1, CONSENSUS_SEARCH_EVENT);
        // TODO: check contents of the two CSEs (expecting 'cam' with freq 3)
    }
}
