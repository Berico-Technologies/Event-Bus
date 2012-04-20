package gov.ment.esp.monitors;

import gov.ment.esp.EventMonitor;
import gov.ment.esp.EventStreamProcessor;
import gov.ment.esp.InferredEvent;
import gov.ment.esp.publish.Publisher;

import java.util.Collection;
import java.util.HashSet;

import gov.ment.eventbus.client.Envelope;

import com.espertech.esper.client.EventBean;

class EventTypeDetector extends EventMonitor {

  private String eventType;

  public EventTypeDetector(String eventType) {
    this.eventType = eventType;
  }

  @Override
  public InferredEvent receive(EventBean eventBean) {
    Envelope env = (Envelope) eventBean.get("resp");
    return makeInferredEvent().addEnvelope(env);
  }

  @Override
  public Collection<Publisher> registerPatterns(EventStreamProcessor esp) {
    esp.monitor(true, "select resp from Envelope as resp where eventType = '" + eventType + "'",
            this);

    // @todo = this needs to be integrated
    return new HashSet<Publisher>();
  }

  @Override
  public String getInferredType() {
    return eventType;
  }

  @Override
  public String getLabel() {
    return this.getClass().getSimpleName() + "(" + eventType + ")";
  }

}
