package pegasus.esp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.espertech.esper.client.EventBean;

/**
 * This interface describes a monitor that watches the event stream for patterns
 * of events and produces an InferredEvent.
 *
 * @author israel
 *
 */
public abstract class EventMonitor {

    protected final Log logger = LogFactory.getLog(this.getClass());

    /** register the patterns to be matched by this monitor with the event stream processor */
    public abstract void registerPatterns(EventStreamProcessor esp);

    /** Processes the matching event or events and return an inferred event, if any */
    public abstract InferredEvent receive(EventBean eventBean);

    public String getInferredType() {
        return null;
    }

    public InferredEvent makeInferredEvent() {
        String inferredType = getInferredType();
        String label = getLabel();
        return new InferredEvent(inferredType, label);
    }

    public String getLabel() {
        return this.getClass().getSimpleName();
    }

    public static int len(EventBean[] events) {
        if (events == null) return 0;
        return events.length;
    }

}
