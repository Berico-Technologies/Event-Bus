package orion.esp.monitors;

import org.junit.Test;

import orion.esp.TestUtils;
import orion.esp.monitors.UnauthorizedAccessAttemptsDetector;

import pegasus.eventbus.client.Envelope;

public class UnauthorizedAccessAttemptsDectectorTest extends AbstractDetectorTest {

    private static final String UNAUTHORIZED_ACCESS_ATTEMPTS = "UnauthorizedAccessAttempts";

    @Test
    public void testThreeUnauthorized() {

        testRepository.addMonitor(new UnauthorizedAccessAttemptsDetector(3, "30 min"));

        Envelope reqPW1 = TestUtils.makeAuthRequest("Peewee Herman", "sat 1 imagery", "PW1");
        sendAndExpectNo(reqPW1, UNAUTHORIZED_ACCESS_ATTEMPTS);

        Envelope responsePW1 = TestUtils.makeAuthResponse(false, "PW1");
        sendAndExpectNo(responsePW1, UNAUTHORIZED_ACCESS_ATTEMPTS);

        Envelope reqPW2 = TestUtils.makeAuthRequest("Peewee Herman", "sat 2 imagery", "PW2");
        sendAndExpectNo(reqPW2, UNAUTHORIZED_ACCESS_ATTEMPTS);

        Envelope responsePW2 = TestUtils.makeAuthResponse(false, "PW2");
        sendAndExpectNo(responsePW2, UNAUTHORIZED_ACCESS_ATTEMPTS);

        Envelope reqPW3 = TestUtils.makeAuthRequest("Peewee Herman", "sat 3 imagery", "PW3");
        sendAndExpectNo(reqPW3, UNAUTHORIZED_ACCESS_ATTEMPTS);

        Envelope responsePW3 = TestUtils.makeAuthResponse(false, "PW3");
        sendAndExpect(responsePW3, 1, UNAUTHORIZED_ACCESS_ATTEMPTS);
    }

    @Test
    public void testThreeAuthorized() {

        testRepository.addMonitor(new UnauthorizedAccessAttemptsDetector(3, "30 min"));

        Envelope reqJB1 = TestUtils.makeAuthRequest("James Bond", "Dr No personnel file", "JB1");
        sendAndExpectNo(reqJB1, UNAUTHORIZED_ACCESS_ATTEMPTS);

        Envelope reqJB2 = TestUtils.makeAuthRequest("James Bond", "Kill Authorization", "JB2");
        sendAndExpectNo(reqJB2, UNAUTHORIZED_ACCESS_ATTEMPTS);

        Envelope responseJB1 = TestUtils.makeAuthResponse(true, "JB1");
        sendAndExpectNo(responseJB1, UNAUTHORIZED_ACCESS_ATTEMPTS);

        Envelope reqJB3 = TestUtils.makeAuthRequest("James Bond", "Underwater car", "JB3");
        sendAndExpectNo(reqJB3, UNAUTHORIZED_ACCESS_ATTEMPTS);

        Envelope responseJB2 = TestUtils.makeAuthResponse(true, "JB2");
        sendAndExpectNo(responseJB2, UNAUTHORIZED_ACCESS_ATTEMPTS);

        Envelope responseJB3 = TestUtils.makeAuthResponse(true, "JB3");
        sendAndExpectNo(responseJB3, UNAUTHORIZED_ACCESS_ATTEMPTS);
    }

    @Test
    public void testTwoUsersEachWithThreeUnauthorized() {

        testRepository.addMonitor(new UnauthorizedAccessAttemptsDetector(3, "30 min"));

        Envelope reqPW1 = TestUtils.makeAuthRequest("Peewee Herman", "sat 1 imagery", "PW1");
        sendAndExpectNo(reqPW1, UNAUTHORIZED_ACCESS_ATTEMPTS);

        Envelope responsePW1 = TestUtils.makeAuthResponse(false, "PW1");
        sendAndExpectNo(responsePW1, UNAUTHORIZED_ACCESS_ATTEMPTS);

        Envelope reqSK1 = TestUtils.makeAuthRequest("Siegfried@Kaos.com", "Agent 99 datebook", "SK1");
        sendAndExpectNo(reqSK1, UNAUTHORIZED_ACCESS_ATTEMPTS);

        Envelope responseSK1 = TestUtils.makeAuthResponse(false, "SK1");
        sendAndExpectNo(responseSK1, UNAUTHORIZED_ACCESS_ATTEMPTS);

        Envelope reqSK2 = TestUtils.makeAuthRequest("Siegfried@Kaos.com", "shoephone plans", "SK2");
        sendAndExpectNo(reqSK2, UNAUTHORIZED_ACCESS_ATTEMPTS);

        Envelope reqPW2 = TestUtils.makeAuthRequest("Peewee Herman", "sat 2 imagery", "PW2");
        sendAndExpectNo(reqPW2, UNAUTHORIZED_ACCESS_ATTEMPTS);

        Envelope responsePW2 = TestUtils.makeAuthResponse(false, "PW2");
        sendAndExpectNo(responsePW2, UNAUTHORIZED_ACCESS_ATTEMPTS);

        Envelope reqPW3 = TestUtils.makeAuthRequest("Peewee Herman", "sat 3 imagery", "PW3");
        sendAndExpectNo(reqPW3, UNAUTHORIZED_ACCESS_ATTEMPTS);

        Envelope responsePW3 = TestUtils.makeAuthResponse(false, "PW3");
        sendAndExpect(responsePW3, 1, UNAUTHORIZED_ACCESS_ATTEMPTS);

        Envelope responseSK2 = TestUtils.makeAuthResponse(false, "SK2");
        sendAndExpectNo(responseSK2, UNAUTHORIZED_ACCESS_ATTEMPTS);

        Envelope reqSK3 = TestUtils.makeAuthRequest("Siegfried@Kaos.com", "cone-of-silence user manual", "SK3");
        sendAndExpectNo(reqSK3, UNAUTHORIZED_ACCESS_ATTEMPTS);

        Envelope responseSK3 = TestUtils.makeAuthResponse(false, "SK3");
        sendAndExpect(responseSK3, 1, UNAUTHORIZED_ACCESS_ATTEMPTS);
    }

    @Test
    public void testInterspersedSuccessAndFailureAuthorizations() {
        testRepository.addMonitor(new UnauthorizedAccessAttemptsDetector(3, "30 min"));

        Envelope reqMS1 = TestUtils.makeAuthRequest("Maxwell Smart", "Shoe Phone", "MS1");
        sendAndExpectNo(reqMS1, UNAUTHORIZED_ACCESS_ATTEMPTS);

        Envelope reqPW1 = TestUtils.makeAuthRequest("Peewee Herman", "sat 1 imagery", "PW1");
        sendAndExpectNo(reqPW1, UNAUTHORIZED_ACCESS_ATTEMPTS);

        Envelope responseMS1 = TestUtils.makeAuthResponse(true, "MS1");
        sendAndExpectNo(responseMS1, UNAUTHORIZED_ACCESS_ATTEMPTS);

        Envelope responsePW1 = TestUtils.makeAuthResponse(false, "PW1");
        sendAndExpectNo(responsePW1, UNAUTHORIZED_ACCESS_ATTEMPTS);

        Envelope reqPW2 = TestUtils.makeAuthRequest("Peewee Herman", "sat 2 imagery", "PW2");
        sendAndExpectNo(reqPW2, UNAUTHORIZED_ACCESS_ATTEMPTS);

        Envelope reqJB1 = TestUtils.makeAuthRequest("James Bond", "Dr No personnel file", "JB1");
        sendAndExpectNo(reqJB1, UNAUTHORIZED_ACCESS_ATTEMPTS);

        Envelope reqJB2 = TestUtils.makeAuthRequest("James Bond", "Kill Authorization", "JB2");
        sendAndExpectNo(reqJB2, UNAUTHORIZED_ACCESS_ATTEMPTS);

        Envelope responsePW2 = TestUtils.makeAuthResponse(false, "PW2");
        sendAndExpectNo(responsePW2, UNAUTHORIZED_ACCESS_ATTEMPTS);

        Envelope reqPW3 = TestUtils.makeAuthRequest("Peewee Herman", "sat 3 imagery", "PW3");
        sendAndExpectNo(reqPW3, UNAUTHORIZED_ACCESS_ATTEMPTS);

        Envelope responsePW3 = TestUtils.makeAuthResponse(false, "PW3");
        sendAndExpect(responsePW3, 1, UNAUTHORIZED_ACCESS_ATTEMPTS);

        Envelope responseJB1 = TestUtils.makeAuthResponse(true, "JB1");
        sendAndExpectNo(responseJB1, UNAUTHORIZED_ACCESS_ATTEMPTS);

        Envelope reqJB3 = TestUtils.makeAuthRequest("James Bond", "Underwater car", "JB3");
        sendAndExpectNo(reqJB3, UNAUTHORIZED_ACCESS_ATTEMPTS);

        Envelope responseJB2 = TestUtils.makeAuthResponse(true, "JB2");
        sendAndExpectNo(responseJB2, UNAUTHORIZED_ACCESS_ATTEMPTS);

        Envelope responseJB3 = TestUtils.makeAuthResponse(true, "JB3");
        sendAndExpectNo(responseJB3, UNAUTHORIZED_ACCESS_ATTEMPTS);
    }

    @Test
    public void testLimitFiveUnauthorized() {

        testRepository.addMonitor(new UnauthorizedAccessAttemptsDetector(5, "30 min"));

        Envelope reqSK1 = TestUtils.makeAuthRequest("Siegfried@Kaos.com", "sat 1 imagery", "SK1");
        sendAndExpectNo(reqSK1, UNAUTHORIZED_ACCESS_ATTEMPTS);

        Envelope responseSK1 = TestUtils.makeAuthResponse(false, "SK1");
        sendAndExpectNo(responseSK1, UNAUTHORIZED_ACCESS_ATTEMPTS);

        Envelope reqSK2 = TestUtils.makeAuthRequest("Siegfried@Kaos.com", "sat 2 imagery", "SK2");
        sendAndExpectNo(reqSK2, UNAUTHORIZED_ACCESS_ATTEMPTS);

        Envelope responseSK2 = TestUtils.makeAuthResponse(false, "SK2");
        sendAndExpectNo(responseSK2, UNAUTHORIZED_ACCESS_ATTEMPTS);

        Envelope reqSK3 = TestUtils.makeAuthRequest("Siegfried@Kaos.com", "sat 3 imagery", "SK3");
        sendAndExpectNo(reqSK3, UNAUTHORIZED_ACCESS_ATTEMPTS);

        Envelope responseSK3 = TestUtils.makeAuthResponse(false, "SK3");
        sendAndExpectNo(responseSK3, UNAUTHORIZED_ACCESS_ATTEMPTS);

        Envelope reqSK4 = TestUtils.makeAuthRequest("Siegfried@Kaos.com", "Agent 99 datebook", "SK4");
        sendAndExpectNo(reqSK4, UNAUTHORIZED_ACCESS_ATTEMPTS);

        Envelope responseSK4 = TestUtils.makeAuthResponse(false, "SK4");
        sendAndExpectNo(responseSK4, UNAUTHORIZED_ACCESS_ATTEMPTS);

        Envelope reqSK5 = TestUtils.makeAuthRequest("Siegfried@Kaos.com", "shoephone plans", "SK5");
        sendAndExpectNo(reqSK5, UNAUTHORIZED_ACCESS_ATTEMPTS);

        Envelope responseSK5 = TestUtils.makeAuthResponse(false, "SK5");
        sendAndExpect(responseSK5, 1, UNAUTHORIZED_ACCESS_ATTEMPTS);

        Envelope reqSK6 = TestUtils.makeAuthRequest("Siegfried@Kaos.com", "cone-of-silence user manual", "SK6");
        sendAndExpectNo(reqSK6, UNAUTHORIZED_ACCESS_ATTEMPTS);

        Envelope responseSK6 = TestUtils.makeAuthResponse(false, "SK6");
        sendAndExpect(responseSK6, 1, UNAUTHORIZED_ACCESS_ATTEMPTS);
    }
}

