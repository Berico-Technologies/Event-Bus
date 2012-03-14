package orion.esp.monitors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import orion.esp.EnvelopeUtils;
import orion.esp.InferredEvent;
import orion.esp.TestUtils;
import orion.esp.monitors.ConsensusSearchDetector;
import orion.esp.monitors.CorrelateRequestResponsesEventDetector;
import orion.esp.monitors.DocumentCollectionWithHitFrequencySearchResultsDetector;
import orion.esp.monitors.EventTypeDetector;
import orion.esp.monitors.UnauthorizedAccessAttemptsDetector;

import pegasus.eventbus.client.Envelope;

public class StorageRepositoryTest extends AbstractDetectorTest {

    private EventTypeDetector reqMonitor;
    private EventTypeDetector responseMonitor;

    private void populateRepository() {
        testRepository.addMonitor(new UnauthorizedAccessAttemptsDetector(3, "30 min"));
        testRepository.addMonitor(new ConsensusSearchDetector(3, "1 hour"));
        testRepository.addMonitor(new CorrelateRequestResponsesEventDetector());
        testRepository.addMonitor(new DocumentCollectionWithHitFrequencySearchResultsDetector());
        reqMonitor = new EventTypeDetector("Request");
        testRepository.addMonitor(reqMonitor);
        responseMonitor = new EventTypeDetector("Response");
        testRepository.addMonitor(responseMonitor);
    }

    @Test
    public void testDynamicallyAddedMonitors() {
        String signoffString = "Signoff";

        // Create the monitor for signoff's, but don't add it to the repository or event
        // stream processor
        EventTypeDetector signoffMonitor = new EventTypeDetector(signoffString);

        populateRepository();

        // Send a signoff notification, since we have no event monitors that match on  that,
        // no inferred events will occur
        Envelope signoff = TestUtils.makeEnvelope(signoffString, null, null,
                "Say goodnight Gracie", "George Burns");
        clearAndSend(signoff);
        assertNoDetectedEvents(UnauthorizedAccessAttemptsDetector.INFERRED_TYPE);
        assertNoDetectedEvents(ConsensusSearchDetector.INFERRED_TYPE);
        assertNoDetectedEvents(CorrelateRequestResponsesEventDetector.INFERRED_TYPE);
        assertNoDetectedEvents(DocumentCollectionWithHitFrequencySearchResultsDetector.INFERRED_TYPE);
        assertNoDetectedEvents(reqMonitor.getInferredType());
        assertNoDetectedEvents(responseMonitor.getInferredType());
        assertNoDetectedEvents(signoffString);

        // add the signoff events monitor to the repository, this adds it to the event
        // stream processor
        testRepository.addMonitor(signoffMonitor);
        // adding a new monitor should not detect any events yet
        assertEquals(0, envelopesDetected.size());

        // Send a second signoff notification after adding a monitor for it; we should detect
        // the second event, but not the first
        Envelope signoff2 = TestUtils.makeEnvelope(signoffString, null, null,
                "And that's the way it is.", "Walter Cronkite");
        clearAndSend(signoff2);
        assertNoDetectedEvents(UnauthorizedAccessAttemptsDetector.INFERRED_TYPE);
        assertNoDetectedEvents(ConsensusSearchDetector.INFERRED_TYPE);
        assertNoDetectedEvents(CorrelateRequestResponsesEventDetector.INFERRED_TYPE);
        assertNoDetectedEvents(DocumentCollectionWithHitFrequencySearchResultsDetector.INFERRED_TYPE);
        assertNoDetectedEvents(reqMonitor.getInferredType());
        assertNoDetectedEvents(responseMonitor.getInferredType());
        InferredEvent event = assertOneDetectedEvent(signoffString);
        assertInferredEventReferences(event, signoff2);
        assertFalse("Checking to see if " + event + " references " + signoff,
                event.getReferencedEvents().contains(signoff));


    }

    @Test
    public void testMultiPatternRepository() {
        populateRepository();

        //**********************************************************************
        // Send a signoff notification, since we have no event monitors that match on  that,
        // no inferred events will occur
        Envelope signoff = TestUtils.makeEnvelope("Signoff", null, null,
                "Say goodnight Gracie", "Gracie Allen");
        clearAndSend(signoff);
        assertNoDetectedEvents(UnauthorizedAccessAttemptsDetector.INFERRED_TYPE);
        assertNoDetectedEvents(ConsensusSearchDetector.INFERRED_TYPE);
        assertNoDetectedEvents(CorrelateRequestResponsesEventDetector.INFERRED_TYPE);
        assertNoDetectedEvents(DocumentCollectionWithHitFrequencySearchResultsDetector.INFERRED_TYPE);
        assertNoDetectedEvents(reqMonitor.getInferredType());
        assertNoDetectedEvents(responseMonitor.getInferredType());
        //**********************************************************************

        // Send a request (that will be granted); there should be a 'Request' inferred event
        Envelope reqMS1 = TestUtils.makeAuthRequest("Maxwell Smart", "Shoe Phone", "MS1");
        clearAndSend(reqMS1);
        assertNoDetectedEvents(UnauthorizedAccessAttemptsDetector.INFERRED_TYPE);
        assertNoDetectedEvents(ConsensusSearchDetector.INFERRED_TYPE);
        assertNoDetectedEvents(CorrelateRequestResponsesEventDetector.INFERRED_TYPE);
        assertNoDetectedEvents(DocumentCollectionWithHitFrequencySearchResultsDetector.INFERRED_TYPE);
        InferredEvent event1a = assertOneDetectedEvent(reqMonitor.getInferredType());
        assertInferredEventReferences(event1a, reqMS1);
        assertNoDetectedEvents(responseMonitor.getInferredType());

        // Send a request (that will be denied); there should be a 'Request' inferred event
        Envelope reqPW1 = TestUtils.makeAuthRequest("Peewee Herman", "sat 1 imagery", "PW1");
        clearAndSend(reqPW1);
        assertNoDetectedEvents(UnauthorizedAccessAttemptsDetector.INFERRED_TYPE);
        assertNoDetectedEvents(ConsensusSearchDetector.INFERRED_TYPE);
        assertNoDetectedEvents(CorrelateRequestResponsesEventDetector.INFERRED_TYPE);
        assertNoDetectedEvents(DocumentCollectionWithHitFrequencySearchResultsDetector.INFERRED_TYPE);
        InferredEvent event2a = assertOneDetectedEvent(reqMonitor.getInferredType());
        assertInferredEventReferences(event2a, reqPW1);
        assertNoDetectedEvents(responseMonitor.getInferredType());

        // Send a granted response; there should be a response inferred event and a correlated
        // request-response inferred event
        Envelope responseMS1 = TestUtils.makeAuthResponse(true, "MS1");
        clearAndSend(responseMS1);
        assertNoDetectedEvents(UnauthorizedAccessAttemptsDetector.INFERRED_TYPE);
        assertNoDetectedEvents(ConsensusSearchDetector.INFERRED_TYPE);
        InferredEvent event3a = assertOneDetectedEvent(CorrelateRequestResponsesEventDetector.INFERRED_TYPE);
        assertInferredEventReferences(event3a, responseMS1);
        assertInferredEventReferences(event3a, reqMS1);
        assertNoDetectedEvents(DocumentCollectionWithHitFrequencySearchResultsDetector.INFERRED_TYPE);
        assertNoDetectedEvents(reqMonitor.getInferredType());
        InferredEvent event3b = assertOneDetectedEvent(responseMonitor.getInferredType());
        assertInferredEventReferences(event3b, responseMS1);

        // Send a denied response; there should be a response inferred event and a correlated
        // request-response inferred event
        Envelope responsePW1 = TestUtils.makeAuthResponse(false, "PW1");
        clearAndSend(responsePW1);
        assertNoDetectedEvents(UnauthorizedAccessAttemptsDetector.INFERRED_TYPE);
        assertNoDetectedEvents(ConsensusSearchDetector.INFERRED_TYPE);
        InferredEvent event4a = assertOneDetectedEvent(CorrelateRequestResponsesEventDetector.INFERRED_TYPE);
        assertInferredEventReferences(event4a, responsePW1);
        assertInferredEventReferences(event4a, reqPW1);
        assertNoDetectedEvents(DocumentCollectionWithHitFrequencySearchResultsDetector.INFERRED_TYPE);
        assertNoDetectedEvents(reqMonitor.getInferredType());
        InferredEvent event4b = assertOneDetectedEvent(responseMonitor.getInferredType());
        assertInferredEventReferences(event4b, responsePW1);

        // Send a request (that will be denied); there should be a 'Request' inferred event
        Envelope reqPW2 = TestUtils.makeAuthRequest("Peewee Herman", "sat 2 imagery", "PW2");
        clearAndSend(reqPW2);
        assertNoDetectedEvents(UnauthorizedAccessAttemptsDetector.INFERRED_TYPE);
        assertNoDetectedEvents(ConsensusSearchDetector.INFERRED_TYPE);
        assertNoDetectedEvents(CorrelateRequestResponsesEventDetector.INFERRED_TYPE);
        assertNoDetectedEvents(DocumentCollectionWithHitFrequencySearchResultsDetector.INFERRED_TYPE);
        InferredEvent event5a = assertOneDetectedEvent(reqMonitor.getInferredType());
        assertInferredEventReferences(event5a, reqPW2);
        assertNoDetectedEvents(responseMonitor.getInferredType());

        // Send a request (that will be granted); there should be a 'Request' inferred event
        Envelope reqJB1 = TestUtils.makeAuthRequest("James Bond", "Dr No personnel file", "JB1");
        clearAndSend(reqJB1);
        assertNoDetectedEvents(UnauthorizedAccessAttemptsDetector.INFERRED_TYPE);
        assertNoDetectedEvents(ConsensusSearchDetector.INFERRED_TYPE);
        assertNoDetectedEvents(CorrelateRequestResponsesEventDetector.INFERRED_TYPE);
        assertNoDetectedEvents(DocumentCollectionWithHitFrequencySearchResultsDetector.INFERRED_TYPE);
        InferredEvent event6a = assertOneDetectedEvent(reqMonitor.getInferredType());
        assertInferredEventReferences(event6a, reqJB1);
        assertNoDetectedEvents(responseMonitor.getInferredType());

        // Send a request (that will be granted); there should be a 'Request' inferred event
        Envelope reqJB2 = TestUtils.makeAuthRequest("James Bond", "Kill Authorization", "JB2");
        clearAndSend(reqJB2);
        assertNoDetectedEvents(UnauthorizedAccessAttemptsDetector.INFERRED_TYPE);
        assertNoDetectedEvents(ConsensusSearchDetector.INFERRED_TYPE);
        assertNoDetectedEvents(CorrelateRequestResponsesEventDetector.INFERRED_TYPE);
        assertNoDetectedEvents(DocumentCollectionWithHitFrequencySearchResultsDetector.INFERRED_TYPE);
        InferredEvent event7a = assertOneDetectedEvent(reqMonitor.getInferredType());
        assertInferredEventReferences(event7a, reqJB2);
        assertNoDetectedEvents(responseMonitor.getInferredType());

        // Send a denied response; there should be a response inferred event and a correlated
        // request-response inferred event
        Envelope responsePW2 = TestUtils.makeAuthResponse(false, "PW2");
        clearAndSend(responsePW2);
        assertNoDetectedEvents(UnauthorizedAccessAttemptsDetector.INFERRED_TYPE);
        assertNoDetectedEvents(ConsensusSearchDetector.INFERRED_TYPE);
        InferredEvent event8a = assertOneDetectedEvent(CorrelateRequestResponsesEventDetector.INFERRED_TYPE);
        assertInferredEventReferences(event8a, responsePW2);
        assertInferredEventReferences(event8a, reqPW2);
        assertNoDetectedEvents(DocumentCollectionWithHitFrequencySearchResultsDetector.INFERRED_TYPE);
        assertNoDetectedEvents(reqMonitor.getInferredType());
        InferredEvent event8b = assertOneDetectedEvent(responseMonitor.getInferredType());
        assertInferredEventReferences(event8b, responsePW2);

        // Send a request (that will be denied); there should be a 'Request' inferred event
        Envelope reqPW3 = TestUtils.makeAuthRequest("Peewee Herman", "sat 3 imagery", "PW3");
        clearAndSend(reqPW3);
        assertNoDetectedEvents(UnauthorizedAccessAttemptsDetector.INFERRED_TYPE);
        assertNoDetectedEvents(ConsensusSearchDetector.INFERRED_TYPE);
        assertNoDetectedEvents(CorrelateRequestResponsesEventDetector.INFERRED_TYPE);
        assertNoDetectedEvents(DocumentCollectionWithHitFrequencySearchResultsDetector.INFERRED_TYPE);
        InferredEvent event9a = assertOneDetectedEvent(reqMonitor.getInferredType());
        assertInferredEventReferences(event9a, reqPW3);
        assertNoDetectedEvents(responseMonitor.getInferredType());

        // Send a denied response (third for user; there should be a response inferred event,
        // a correlated  request-response inferred event, and an UnauthorizedAccessAttempts
        // inferred event
        Envelope responsePW3 = TestUtils.makeAuthResponse(false, "PW3");
        clearAndSend(responsePW3);
        assertOneDetectedEvent(UnauthorizedAccessAttemptsDetector.INFERRED_TYPE);
        assertNoDetectedEvents(ConsensusSearchDetector.INFERRED_TYPE);
        InferredEvent event10a = assertOneDetectedEvent(CorrelateRequestResponsesEventDetector.INFERRED_TYPE);
        assertInferredEventReferences(event10a, responsePW3);
        assertInferredEventReferences(event10a, reqPW3);
        assertNoDetectedEvents(DocumentCollectionWithHitFrequencySearchResultsDetector.INFERRED_TYPE);
        assertNoDetectedEvents(reqMonitor.getInferredType());
        InferredEvent event10b = assertOneDetectedEvent(responseMonitor.getInferredType());
        assertInferredEventReferences(event10b, responsePW3);

        // Send a granted response; there should be a response inferred event and a correlated
        // request-response inferred event
        Envelope responseJB1 = TestUtils.makeAuthResponse(true, "JB1");
        clearAndSend(responseJB1);
        assertNoDetectedEvents(UnauthorizedAccessAttemptsDetector.INFERRED_TYPE);
        assertNoDetectedEvents(ConsensusSearchDetector.INFERRED_TYPE);
        InferredEvent event11a = assertOneDetectedEvent(CorrelateRequestResponsesEventDetector.INFERRED_TYPE);
        assertInferredEventReferences(event11a, responseJB1);
        assertInferredEventReferences(event11a, reqJB1);
        assertNoDetectedEvents(DocumentCollectionWithHitFrequencySearchResultsDetector.INFERRED_TYPE);
        assertNoDetectedEvents(reqMonitor.getInferredType());
        InferredEvent event11b = assertOneDetectedEvent(responseMonitor.getInferredType());
        assertInferredEventReferences(event11b, responseJB1);

        // Send a request (that will be granted); there should be a 'Request' inferred event
        Envelope reqJB3 = TestUtils.makeAuthRequest("James Bond", "Underwater car", "JB3");
        clearAndSend(reqJB3);
        assertNoDetectedEvents(UnauthorizedAccessAttemptsDetector.INFERRED_TYPE);
        assertNoDetectedEvents(ConsensusSearchDetector.INFERRED_TYPE);
        assertNoDetectedEvents(CorrelateRequestResponsesEventDetector.INFERRED_TYPE);
        assertNoDetectedEvents(DocumentCollectionWithHitFrequencySearchResultsDetector.INFERRED_TYPE);
        InferredEvent event12a = assertOneDetectedEvent(reqMonitor.getInferredType());
        assertInferredEventReferences(event12a, reqJB3);
        assertNoDetectedEvents(responseMonitor.getInferredType());

        // Send a granted response; there should be a response inferred event and a correlated
        // request-response inferred event
        Envelope responseJB2 = TestUtils.makeAuthResponse(true, "JB2");
        clearAndSend(responseJB2);
        assertNoDetectedEvents(UnauthorizedAccessAttemptsDetector.INFERRED_TYPE);
        assertNoDetectedEvents(ConsensusSearchDetector.INFERRED_TYPE);
        InferredEvent event13a = assertOneDetectedEvent(CorrelateRequestResponsesEventDetector.INFERRED_TYPE);
        assertInferredEventReferences(event13a, responseJB2);
        assertInferredEventReferences(event13a, reqJB2);
        assertNoDetectedEvents(DocumentCollectionWithHitFrequencySearchResultsDetector.INFERRED_TYPE);
        assertNoDetectedEvents(reqMonitor.getInferredType());
        InferredEvent event13b = assertOneDetectedEvent(responseMonitor.getInferredType());
        assertInferredEventReferences(event13b, responseJB2);

        // Send a granted response (third for user); there should be a response inferred event
        Envelope responseJB3 = TestUtils.makeAuthResponse(true, "JB3");
        clearAndSend(responseJB3);
        assertNoDetectedEvents(UnauthorizedAccessAttemptsDetector.INFERRED_TYPE);
        assertNoDetectedEvents(ConsensusSearchDetector.INFERRED_TYPE);
        InferredEvent event14a = assertOneDetectedEvent(CorrelateRequestResponsesEventDetector.INFERRED_TYPE);
        assertInferredEventReferences(event14a, responseJB3);
        assertInferredEventReferences(event14a, reqJB3);
        assertNoDetectedEvents(DocumentCollectionWithHitFrequencySearchResultsDetector.INFERRED_TYPE);
        assertNoDetectedEvents(reqMonitor.getInferredType());
        InferredEvent event14b = assertOneDetectedEvent(responseMonitor.getInferredType());
        assertInferredEventReferences(event14b, responseJB3);
    }

}

