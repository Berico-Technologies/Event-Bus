package pegasus.esp;

import java.util.List;

import com.google.common.collect.Lists;

public class StorageRepository implements EventMonitorRepository {

    List<EventMonitor> monitors = Lists.newArrayList();
    private EventStreamProcessor eventStreamProcessor = null;

    public EventMonitorRepository addMonitor(EventMonitor monitor) {
        monitors.add(monitor);
        if (eventStreamProcessor != null) {
            eventStreamProcessor.watchFor(monitor);
        }
        return this;
    }

    @Override
    public void registerWith(EventStreamProcessor eventStreamProcessor) {
        this.eventStreamProcessor = eventStreamProcessor;
        for (EventMonitor monitor : monitors) {
            eventStreamProcessor.watchFor(monitor);
        }
    }
}
