package pegasus.esp;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;

import pegasus.esp.data.ActiveRange;
import pegasus.esp.data.ValueStreams;
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

    Collection<EnvelopeRetriever> counters = Lists.newArrayList();
    private Map<String, ValueStreams> streamsMap = Maps.newHashMap();

    public EnvelopeCounter() {

        counters.add(new EnvelopeRetriever() {
            public String retrieve(Envelope e) { return e.getEventType(); }
            public String key() { return "EventType"; }
            public int[] periods() { return defaultperiods; }
            public int getValue(String type, Envelope env) { return 1; }
        });

        counters.add(new EnvelopeRetriever() {
            public String retrieve(Envelope e) { return e.getEventType(); }
            public String key() { return "Total Body Length by Type"; }
            public int[] periods() { return defaultperiods; }
            public int getValue(String type, Envelope env) { return env.getBody().length; }
        });

        boolean countTopics = false;

        if (countTopics) {
            counters.add(new EnvelopeRetriever() {
                public String retrieve(Envelope e) { return e.getTopic(); }
                public String key() { return "Topic"; }
                public int[] periods() { return defaultperiods; }
                public int getValue(String type, Envelope env) { return 1; }
            });
        }

        for (EnvelopeRetriever envelopeRetriever : counters) {
            String type = envelopeRetriever.key();
            ValueStreams valueStreams = new ValueStreams(type);
            streamsMap.put(type, valueStreams);
            for (int per : envelopeRetriever.periods()) {
                valueStreams.addPeriod(per);
            }
        }
    }

    public void dumpFreqs() {
        for (EnvelopeRetriever counter : counters) {
            String type = counter.key();
            dumpFreq(type);
        }
    }

    public void dumpFreq(String type) {
        System.out.println(type + ":");
        ValueStreams stream = streamsMap.get(type);
        for (String category : stream.getActiveRanges()) {
            System.out.println(String.format("\nTopN Label: %s", category));
            for (String item : stream.getValues()) {
                ActiveRange activeRange = stream.getActiveRange(category, item);
                int total = activeRange.getTotal();
                int trend = activeRange.getTrend();
                String desc = activeRange.getTrendDesc();
                System.out.println(String.format("  Trend Label: %s", item));
                System.out.println(String.format("  Trend Value: %s", total));
                System.out.println(String.format("  Trend info: %s", desc));
                System.out.println(String.format("  Trend change: %s", trend));
                System.out.println();
            }
        }
        System.out.println();
    }

    @Override
    public InferredEvent receive(EventBean eventBean) {
        Envelope env = (Envelope) eventBean.get("env");
        recordValues(env);
        return null;
    }

    private void recordValues(Envelope env) {
        for (EnvelopeRetriever counter : counters) {
            String type = counter.key();
            String item = counter.retrieve(env);
            int value = counter.getValue(type, env);
            ValueStreams valueStreams = streamsMap.get(type);
            Date timestamp = env.getTimestamp();
            long time = timestamp.getTime();
            valueStreams.addValue(item, time, value);
        }
    }

    @Override
    public void registerPatterns(EventStreamProcessor esp) {
        esp.monitor(true, getPattern(), this);
    }

    private String getPattern() {
        return "select env from Envelope as env";
    }
}
