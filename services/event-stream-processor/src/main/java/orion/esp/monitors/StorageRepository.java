package orion.esp.monitors;

import java.util.List;

import pegasus.esp.EventMonitor;
import pegasus.esp.EventMonitorRepository;
import pegasus.esp.EventStreamProcessor;

import com.google.common.collect.Lists;

public class StorageRepository implements EventMonitorRepository {

    List<EventMonitor> monitors = Lists.newArrayList();
    private EventStreamProcessor eventStreamProcessor = null;

    public StorageRepository() {
    }

    // TODO: extend this to take a list
    public StorageRepository(EventMonitor monitor) {
        this();
        addMonitor(monitor);
    }

    public StorageRepository(EventMonitor monitor, EventMonitor monitor2) {
        this(monitor);
        addMonitor(monitor2);
    }

    public List<EventMonitor> getMonitors() {
        return monitors;
    }

    public void setMonitors(List<EventMonitor> monitors) {
        this.monitors = monitors;
    }

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

    @Override
    public String toString() {
        StringBuffer sb =
                new StringBuffer(this.getClass().getSimpleName() + "#" + monitors.size() + "[");
        String sep = "";
        for (EventMonitor monitor : monitors) {
            sb.append(sep + monitor);
            sep = ", ";
        }
        return sb.append("]").toString();
    }

}
