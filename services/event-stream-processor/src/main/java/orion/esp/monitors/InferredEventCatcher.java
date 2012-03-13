package orion.esp.monitors;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import pegasus.esp.EventMonitor;
import pegasus.esp.EventStreamProcessor;
import pegasus.esp.InferredEvent;
import pegasus.esp.Publisher;

import com.espertech.esper.client.EventBean;

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
