package pegasus.esp;

import java.util.Collection;
import java.util.HashSet;

import pegasus.eventbus.client.Envelope;

import com.espertech.esper.client.EventBean;

class DocumentCollectionWithHitFrequencySearchResultsDetector extends EventMonitor {

    public static final String INFERRED_TYPE = "DocumentCollectionWithHitFrequencySearchResult";

    @Override
    public InferredEvent receive(EventBean eventBean) {
        Envelope docs = (Envelope) eventBean.get("docs");
        Envelope freq = (Envelope) eventBean.get("freq");
        InferredEvent resultingEvent = makeInferredEvent();
        resultingEvent.addEnvelope(docs).addEnvelope(freq);
        return resultingEvent;
    }

    @Override
    public Collection<Publisher> registerPatterns(EventStreamProcessor esp) {

        String pattern = "every docs=Envelope(eventType='DocumentCollectionSearchResult')" +
                " -> freq=Envelope(eventType='HitFrequencySearchResult' and " +
                "correlationId=docs.correlationId)";
        esp.monitor(false, pattern, this);

        String pattern2 = "every freq=Envelope(eventType='HitFrequencySearchResult')" +
                " -> docs=Envelope(eventType='DocumentCollectionSearchResult' and " +
                "correlationId=freq.correlationId)";
        esp.monitor(false, pattern2, this);

        // @todo = this needs to be integrated
        return new HashSet<Publisher>();
    }

    @Override
    public String getInferredType() {
        return INFERRED_TYPE;
    }

}
