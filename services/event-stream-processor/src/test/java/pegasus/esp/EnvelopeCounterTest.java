package pegasus.esp;

import static org.junit.Assert.*;

import org.junit.Test;

public class EnvelopeCounterTest extends AbstractDetectorTest {


    @Test
    public void testCounter() {
        EnvelopeCounter ec = new EnvelopeCounter();
        testRepository.addMonitor(ec);

        send(EnvelopeUtils.makeEnvelope("Signoff", null, null, "Say goodnight Gracie", "Gracie Allen"));
        send(TestUtils.makeAuthRequest("Maxwell Smart", "Shoe Phone", "MS1"));
        send(TestUtils.makeSearchRequest("Maxwell Smart", "Shoe Phone with cam", "MS1"));
        send(TestUtils.makeAuthRequest("Peewee Herman", "sat 1 imagery", "PW1"));
        send(TestUtils.makeAuthResponse(true, "MS1"));
        send(TestUtils.createHitFrequency("red"));
        send(TestUtils.makeAuthResponse(false, "PW1"));
        send(TestUtils.makeAuthRequest("Peewee Herman", "sat 2 imagery", "PW2"));
        send(TestUtils.makeSearchRequest("James Bond", "Dr No personnel file and cam feed", "JB1"));
        send(TestUtils.makeAuthRequest("James Bond", "Dr No personnel file", "JB1"));
        send(TestUtils.makeAuthRequest("James Bond", "Kill Authorization", "JB2"));
        send(TestUtils.makeAuthResponse(false, "PW2"));
        send(TestUtils.makeAuthRequest("Peewee Herman", "sat 3 imagery", "PW3"));
        send(TestUtils.makeAuthResponse(false, "PW3"));
        send(TestUtils.makeSearchRequest("Peewee Herman", "sat one imagery", "PW1"));
        send(TestUtils.makeAuthResponse(true, "JB1"));
        send(TestUtils.createHitFrequency("green"));
        send(TestUtils.makeAuthRequest("James Bond", "Underwater car", "JB3"));
        send(TestUtils.makeAuthResponse(true, "JB2"));
        send(TestUtils.makeAuthResponse(true, "JB3"));
        send(TestUtils.createHitFrequency("red"));
        send(TestUtils.makeSearchRequest("Peewee Herman", "sat two imagery", "PW2"));
        send(TestUtils.createHitFrequency("red"));
        send(TestUtils.makeSearchRequest("Peewee Herman", "sat three imagery", "PW3"));
        send(TestUtils.makeSearchRequest("James Bond", "Underwater car", "JB3"));
        send(TestUtils.makeSearchRequest("Chuck Bartowski", "Buy More cam", "CB1"));
        send(TestUtils.createDocumentCollection("red"));
        send(TestUtils.makeSearchRequest("James Bond", "Kill Authorization", "JB2"));
        send(TestUtils.createDocumentCollection("red"));
        send(TestUtils.createDocumentCollection("red"));
        send(TestUtils.createHitFrequency("red"));
        send(TestUtils.createDocumentCollection("green"));
        send(TestUtils.createDocumentCollection("red"));
        send(TestUtils.makeSearchRequest("James Bond", "sat imagery for secret island", "JB4"));
        send(TestUtils.createHitFrequency("green"));
        send(TestUtils.createHitFrequency("red"));
        send(TestUtils.createDocumentCollection("green"));
        send(TestUtils.createDocumentCollection("red"));

        ec.dumpFreqs();
    }

}
