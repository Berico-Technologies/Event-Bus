package pegasus.esp;

import pegasus.eventbus.client.Envelope;

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
    public void registerPatterns(EventStreamProcessor esp) {
        esp.monitor(true, "select resp from Envelope as resp where eventType = '" + eventType + "'", this);
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
