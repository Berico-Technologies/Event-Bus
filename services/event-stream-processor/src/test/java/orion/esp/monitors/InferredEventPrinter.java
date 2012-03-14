package orion.esp.monitors;

import java.util.Collection;
import java.util.HashSet;

import orion.esp.EventMonitor;
import orion.esp.EventStreamProcessor;
import orion.esp.InferredEvent;
import orion.esp.publish.Publisher;


import com.espertech.esper.client.EventBean;

class InferredEventPrinter extends EventMonitor {

    @Override
    public InferredEvent receive(EventBean eventBean) {
        InferredEvent event = (InferredEvent) eventBean.get("event");
        logger.info("<-- " + event);
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
