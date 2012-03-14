package orion.esp;

import java.util.ArrayList;
import java.util.Map;

import pegasus.eventbus.client.Envelope;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * An InferredEvent is the result of an EventMonitor detected a pattern of Events.
 *
 * @author israel
 *
 */
public class InferredEvent {

    @Override
    public String toString() {
        return super.toString() + " [type=" + type + ", label=" + label + ", info=" + eventInfo + "]";
    }

    public String getType() {
        return type;
    }

    public ArrayList<Envelope> getReferencedEvents() {
        return referencedEvents;
    }

    private String type;
    private ArrayList<Envelope> referencedEvents = Lists.newArrayList();
    private Map<String, String> eventInfo = Maps.newHashMap();
    private String label;

    public InferredEvent(String type, String label) {
        this.type = type;
        this.label = label;
    }

    public InferredEvent addEnvelope(Envelope env) {
        referencedEvents.add(env);
        return this;
    }

    public InferredEvent putData(String key, String val) {
        eventInfo.put(key, val);
        return this;
    }

    public String getData(String key) {
        return eventInfo.get(key);
    }
}
