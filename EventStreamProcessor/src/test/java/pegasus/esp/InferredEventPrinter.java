package pegasus.esp;

import com.espertech.esper.client.EventBean;

class InferredEventPrinter extends EventMonitor {

    @Override
    public InferredEvent receive(EventBean eventBean) {
        InferredEvent event = (InferredEvent) eventBean.get("event");
        logger.info("<-- " + event);
        return null;
    }

    @Override
    public void registerPatterns(EventStreamProcessor esp) {
        esp.monitor(true, "select event from InferredEvent as event", this);
    }

    @Override
    public String getInferredType() {
        return null;
    }
}
