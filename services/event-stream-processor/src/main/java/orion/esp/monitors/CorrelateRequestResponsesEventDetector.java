package orion.esp.monitors;

import java.util.Collection;
import java.util.HashSet;

import orion.esp.EventMonitor;
import orion.esp.EventStreamProcessor;
import orion.esp.InferredEvent;
import orion.esp.publish.Publisher;

import pegasus.eventbus.client.Envelope;

import com.espertech.esper.client.EventBean;

class CorrelateRequestResponsesEventDetector extends EventMonitor {

    public static final String INFERRED_TYPE = "RequestResponse";

    @Override
    public InferredEvent receive(EventBean eventBean) {
        Envelope req = (Envelope) eventBean.get("request");
        Envelope resp = (Envelope) eventBean.get("response");
        return makeInferredEvent().addEnvelope(req).addEnvelope(resp);
    }

    @Override
    public Collection<Publisher> registerPatterns(EventStreamProcessor esp) {

        String pattern = "every request=Envelope(eventType='Request')" +
                " -> response=Envelope(eventType='Response' and " +
                "correlationId=request.id)";
        esp.monitor(false, pattern, this);

        // @todo = this needs to be integrated
        return new HashSet<Publisher>();
    }

    @Override
    public String getInferredType() {
        return INFERRED_TYPE;
    }

}
