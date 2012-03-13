package orion.esp.monitors;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;

import pegasus.esp.EnvelopeUtils;
import pegasus.esp.EventMonitor;
import pegasus.esp.EventStreamProcessor;
import pegasus.esp.InferredEvent;
import pegasus.esp.Publisher;
import pegasus.esp.data.ActiveRange;
import pegasus.esp.data.ValueStreams;
import pegasus.esp.data.ValueStreamsDataProvider;
import pegasus.esp.metric.TopNMetricPublisher;
import pegasus.eventbus.client.Envelope;

import com.espertech.esper.client.EventBean;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class EnvelopeCounter extends EventMonitor {

    int[] defaultperiods = {
            ValueStreams.seconds(30),
            ValueStreams.minutes(1),
            ValueStreams.minutes(5),
            ValueStreams.hours(1)
            };

    public interface EnvelopeRetriever {
        public String retrieve(Envelope e);
        public String key();
        public int[] periods();
        public int getValue(String type, Envelope env);
    }

    Collection<EnvelopeRetriever> metrics = Lists.newArrayList();
    private Map<String, ValueStreams> streamsMap = Maps.newHashMap();
    private Set<Publisher> publishers = new HashSet<Publisher>();

    public EnvelopeCounter() {

        setupMetrics();

        int metricCount = 0;

        for (EnvelopeRetriever envelopeRetriever : metrics) {
            String type = extractKey(envelopeRetriever);
            ValueStreams valueStreams = new ValueStreams(type);
            streamsMap.put(type, valueStreams);
            for (int per : envelopeRetriever.periods()) {
                String desc = valueStreams.addPeriod(per);
//                 System.out.println(String.format("Metric %d: %s", metricCount++, desc));
                ValueStreamsDataProvider provider = new ValueStreamsDataProvider(valueStreams, desc);
                TopNMetricPublisher publisher = new TopNMetricPublisher();
                publisher.setDataProvider(provider);
                publishers.add(publisher);
            }
        }
//         System.out.println(String.format("%d Total Metrics", metricCount));
    }

    private void setupMetrics() {
        metrics.add(new EnvelopeRetriever() {
            public String retrieve(Envelope e) { return e.getEventType(); }
            public String key() { return "Event Type"; }
            public int[] periods() { return defaultperiods; }
            public int getValue(String type, Envelope env) { return 1; }
        });

        metrics.add(new EnvelopeRetriever() {
            public String retrieve(Envelope e) { return "BodyLength"; }
            public String key() { return "Total Body Length"; }
            public int[] periods() { return defaultperiods; }
            public int getValue(String type, Envelope env) { return env.getBody().length; }
        });

        metrics.add(new EnvelopeRetriever() {
            public String retrieve(Envelope e) { return e.getEventType(); }
            public String key() { return "Total Body Length by Type"; }
            public int[] periods() { return defaultperiods; }
            public int getValue(String type, Envelope env) { return env.getBody().length; }
        });

        metrics.add(new EnvelopeRetriever() {
            public String retrieve(Envelope e) {
                if (e.getEventType().equals("pegasus.core.search.event.TextSearchEvent")) {
                    return EnvelopeUtils.getBodyValue(e, "queryText");
                }
                return null;
            }
            public String key() { return "Search Query"; }
            public int[] periods() { return defaultperiods; }
            public int getValue(String type, Envelope env) { return 1; }
        });

        metrics.add(new EnvelopeRetriever() {
            public String retrieve(Envelope e) {
                if (e.getEventType().equals("pegasus.core.search.event.TextSearchEvent")) {
                    String query = EnvelopeUtils.getBodyValue(e, "queryText");
                    String terms = EnvelopeUtils.makeSearchTermList(query);
                    return terms;
                }
                return null;
            }
            public String key() { return "*Individual Search Terms"; }
            public int[] periods() { return defaultperiods; }
            public int getValue(String type, Envelope env) { return 1; }
        });
    }

//    public void dumpFreqs() {
//        for (EnvelopeRetriever counter : metrics) {
//            String type = extractKey(counter);
//           ValueStreams streams = streamsMap.get(type);
//            streams.display();
//        }
//    }

    @Override
    public InferredEvent receive(EventBean eventBean) {
        Envelope env = (Envelope) eventBean.get("env");
        recordValues(env);
        return null;
    }

    long lastDisplayTime = Long.MIN_VALUE;
    int displayFrequency = ValueStreams.seconds(10);
    int envelopesSeen = 0;

//    private void gotEnvelopeCheckForDumping() {
//        boolean showFrequencies = false;
//         showFrequencies = true;
//
//        if (showFrequencies) {
//            envelopesSeen++;
//            long curtime = new Date().getTime();
//            if (curtime > lastDisplayTime + displayFrequency) {
//                System.out.println("EC: After envelope " + envelopesSeen);
//                dumpFreqs();
//                lastDisplayTime = curtime;
//            }
//        }
//    }

    private void recordValues(Envelope env) {
        Date timestamp = env.getTimestamp();
        // If the envelope doesn't have a timestamp, use the current time
        if (timestamp == null) timestamp = new Date();
        long time = timestamp.getTime();
        for (EnvelopeRetriever metric : metrics) {
            String item = metric.retrieve(env);
            if (item == null) continue;
            String inittype = metric.key();
            String type = extractKey(metric);
            int value = metric.getValue(type, env);
            // handle multiples (HACK,HACK, HACK!!!)
            // Multiples are denoted by the type starting with a "*", and the
            // string will be comma-separated concatenation of the items to
            // be processed.
            if (inittype.startsWith("*")) {
                ValueStreams valueStreams = streamsMap.get(type);
                String[] items = item.split(",");
                for (String realItem : items) {
                    valueStreams.addValue(realItem, time, value);
                }
            } else {
                ValueStreams valueStreams = streamsMap.get(type);
                valueStreams.addValue(item, time, value);
            }
        }
//         gotEnvelopeCheckForDumping();
    }

    private String extractKey(EnvelopeRetriever metric) {
        String key = metric.key();
        if (key.startsWith("*")) {
            key = key.substring(1);  // remove the "*"
        }
        return key;
    }

    @Override
    public Collection<Publisher> registerPatterns(EventStreamProcessor esp) {
        esp.monitor(true, getPattern(), this);

        return publishers;
    }

    private String getPattern() {
        return "select env from Envelope as env";
    }
}
