package gov.ment.esp.monitors;

import gov.ment.esp.InferredEvent;
import gov.ment.esp.TestUtils;
import gov.ment.esp.monitors.EventTypeDetector;

import org.junit.Test;

import gov.ment.eventbus.client.Envelope;

public class EventTypeDetectorTest extends AbstractDetectorTest {

  private static final String RESPONSE = "Response";

  @Test
  public void testEvents() {
    testRepository.addMonitor(new EventTypeDetector(RESPONSE));

    Envelope e;

    e = TestUtils.makeRequest("red");
    sendAndExpectNoResult(e);

    e = TestUtils.makeResponse("yellow");
    InferredEvent event = sendAndExpect(e, 1, RESPONSE).get(0);
    assertInferredEventReferences(event, e);

    e = TestUtils.makeResponse("red");
    InferredEvent event1 = sendAndExpect(e, 1, RESPONSE).get(0);
    assertInferredEventReferences(event1, e);

    e = TestUtils.makeRequest("green");
    sendAndExpectNoResult(e);

    e = TestUtils.makeRequest("chartreuse");
    sendAndExpectNoResult(e);

    e = TestUtils.makeResponse("purple");
    InferredEvent event2 = sendAndExpect(e, 1, RESPONSE).get(0);
    assertInferredEventReferences(event2, e);

    e = TestUtils.makeResponse("green");
    InferredEvent event3 = sendAndExpect(e, 1, RESPONSE).get(0);
    assertInferredEventReferences(event3, e);

    e = TestUtils.makeResponse("blue");
    InferredEvent event4 = sendAndExpect(e, 1, RESPONSE).get(0);
    assertInferredEventReferences(event4, e);

    e = TestUtils.makeRequest("orange");
    sendAndExpectNoResult(e);

    e = TestUtils.makeRequest("pink");
    sendAndExpectNoResult(e);

    e = TestUtils.makeResponse("orange");
    InferredEvent event5 = sendAndExpect(e, 1, RESPONSE).get(0);
    assertInferredEventReferences(event5, e);
  }

  private void sendAndExpectNoResult(Envelope env) {
    sendAndExpectNo(env, RESPONSE);
  }
}
