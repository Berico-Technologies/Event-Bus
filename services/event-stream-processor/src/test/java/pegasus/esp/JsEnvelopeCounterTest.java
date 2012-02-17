package pegasus.esp;

import java.util.ArrayList;

import javax.script.ScriptException;

import org.junit.Assert;
import org.junit.Test;

import pegasus.eventbus.client.Envelope;

public class JsEnvelopeCounterTest extends AbstractDetectorTest {

	private static final String COUNT_EVENT = "BusEvents";

	@Test
	public void testSearches() throws ScriptException {
		JsEnvelopeCounter counter;
		counter = new JsEnvelopeCounter();
		testRepository.addMonitor(counter);

		int envCount = 1;

		Envelope signoff = TestUtils.makeEnvelope("Signoff", null, null,
				"Say goodnight Gracie", "Gracie Allen");
		sendAndCheckCount(signoff, envCount ++);
		//**********************************************************************

		// Send a request (that will be granted); there should be a 'Request' inferred event
		Envelope reqMS1 = TestUtils.makeAuthRequest("Maxwell Smart", "Shoe Phone", "MS1");
		sendAndCheckCount(reqMS1, envCount++);

		// Send a request (that will be denied); there should be a 'Request' inferred event
		Envelope reqPW1 = TestUtils.makeAuthRequest("Peewee Herman", "sat 1 imagery", "PW1");
		sendAndCheckCount(reqPW1, envCount++);

		// Send a granted response; there should be a response inferred event and a correlated
		// request-response inferred event
		Envelope responseMS1 = TestUtils.makeAuthResponse(true, "MS1");
		sendAndCheckCount(responseMS1, envCount++);

		// Send a denied response; there should be a response inferred event and a correlated
		// request-response inferred event
		Envelope responsePW1 = TestUtils.makeAuthResponse(false, "PW1");
		sendAndCheckCount(responsePW1, envCount++);

		// Send a request (that will be denied); there should be a 'Request' inferred event
		Envelope reqPW2 = TestUtils.makeAuthRequest("Peewee Herman", "sat 2 imagery", "PW2");
		sendAndCheckCount(reqPW2, envCount++);

		// Send a request (that will be granted); there should be a 'Request' inferred event
		Envelope reqJB1 = TestUtils.makeAuthRequest("James Bond", "Dr No personnel file", "JB1");
		sendAndCheckCount(reqJB1, envCount++);

		// Send a request (that will be granted); there should be a 'Request' inferred event
		Envelope reqJB2 = TestUtils.makeAuthRequest("James Bond", "Kill Authorization", "JB2");
		sendAndCheckCount(reqJB2, envCount++);

		// Send a denied response; there should be a response inferred event and a correlated
		// request-response inferred event
		Envelope responsePW2 = TestUtils.makeAuthResponse(false, "PW2");
		sendAndCheckCount(responsePW2, envCount++);

		// Send a request (that will be denied); there should be a 'Request' inferred event
		Envelope reqPW3 = TestUtils.makeAuthRequest("Peewee Herman", "sat 3 imagery", "PW3");
		sendAndCheckCount(reqPW3, envCount++);

		// Send a denied response (third for user; there should be a response inferred event,
		// a correlated  request-response inferred event, and an UnauthorizedAccessAttempts
		// inferred event
		Envelope responsePW3 = TestUtils.makeAuthResponse(false, "PW3");
		sendAndCheckCount(responsePW3, envCount++);

		// Send a granted response; there should be a response inferred event and a correlated
		// request-response inferred event
		Envelope responseJB1 = TestUtils.makeAuthResponse(true, "JB1");
		sendAndCheckCount(responseJB1, envCount++);

		// Send a request (that will be granted); there should be a 'Request' inferred event
		Envelope reqJB3 = TestUtils.makeAuthRequest("James Bond", "Underwater car", "JB3");
		sendAndCheckCount(reqJB3, envCount++);

		// Send a granted response; there should be a response inferred event and a correlated
		// request-response inferred event
		Envelope responseJB2 = TestUtils.makeAuthResponse(true, "JB2");
		sendAndCheckCount(responseJB2, envCount++);

		// Send a granted response (third for user); there should be a response inferred event
		Envelope responseJB3 = TestUtils.makeAuthResponse(true, "JB3");
		sendAndCheckCount(responseJB3, envCount++);
	}

	private void sendAndCheckCount(Envelope env, int expectedCount) {
		InferredEvent ie = sendAndExpect(env, 1, COUNT_EVENT).get(0);
		String countStr = ie.getData("Count");
		int count = Integer.parseInt(countStr);
		Assert.assertEquals(expectedCount, count);
	}
}
