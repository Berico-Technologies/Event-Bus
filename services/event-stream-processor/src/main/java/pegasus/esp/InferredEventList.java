package pegasus.esp;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

public class InferredEventList extends InferredEvent {

    ArrayList<InferredEvent> events = Lists.newArrayList();

    public InferredEventList() {
        super(null, null);
    }

    public InferredEventList addInferredEvent(InferredEvent event) {
        events.add(event);
        return this;
    }

    public List<InferredEvent> getList() {
        return events;
    }
}
