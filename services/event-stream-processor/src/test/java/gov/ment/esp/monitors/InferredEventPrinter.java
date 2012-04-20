package gov.ment.esp.monitors;

import gov.ment.esp.EventMonitor;
import gov.ment.esp.EventStreamProcessor;
import gov.ment.esp.InferredEvent;
import gov.ment.esp.publish.Publisher;

import java.util.Collection;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.espertech.esper.client.EventBean;

/**
 * Monitor for testing that catches all inferred events and logs them.
 * 
 * @author israel
 * 
 */
class InferredEventPrinter extends EventMonitor {

  private static final Logger LOG = LoggerFactory.getLogger(InferredEventPrinter.class);

  @Override
  public InferredEvent receive(EventBean eventBean) {
    InferredEvent event = (InferredEvent) eventBean.get("event");
    LOG.info("<-- " + event);
    return null;
  }

  @Override
  public Collection<Publisher> registerPatterns(EventStreamProcessor esp) {
    esp.monitor(true, "select event from InferredEvent as event", this);

    // @todo = this needs to be integrated
    return new HashSet<Publisher>();
  }

  @Override
  public String getInferredType() {
    return null;
  }
}
