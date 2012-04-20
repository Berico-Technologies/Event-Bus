package gov.ment.esp.monitors;

import gov.ment.esp.EventMonitor;
import gov.ment.esp.EventStreamProcessor;
import gov.ment.esp.InferredEvent;
import gov.ment.esp.publish.Publisher;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.espertech.esper.client.EventBean;

/**
 * Support class for testing monitors that generate Inferred Events. This takes
 * a List for storing InferredEvent objects, and catches and stores all
 * generated InferredEvents into it.
 * 
 * @author israel
 * 
 */
public class InferredEventCatcher extends EventMonitor {

  private final List<InferredEvent> detected;

  public InferredEventCatcher(List<InferredEvent> detected) {
    this.detected = detected;
  }

  @Override
  public InferredEvent receive(EventBean eventBean) {
    InferredEvent env = (InferredEvent) eventBean.get("res");
    detected.add(env);
    return null;
  }

  @Override
  public Collection<Publisher> registerPatterns(EventStreamProcessor esp) {
    esp.monitor(true, "select res from InferredEvent as res", this);

    // @todo = this needs to be integrated
    return new HashSet<Publisher>();
  }
}
