package orion.esp.datastreams;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ValueStreams {
    public String getName() {
        return name;
    }

    private Map<String, Stream> streams = Maps.newHashMap();
    private Map<String, Integer> periodMap = Maps.newHashMap();
    private String name;
    long lastTick = 0;

    public ValueStreams(String name) {
        super();
        this.name = name;
    }

    public String addPeriod(int millis) {
        String desc = toEnglish(name, millis);
        periodMap.put(desc, millis);
        return desc;
    }

    public Stream getStream(String item) {
        return streams.get(item);
    }

    // a stream should only be created if data will immediately be added so streams are
    // guaranteed to have data
    private void createStream(String item, long timestamp, int value) {
        Stream stream = new Stream(item);
        for (String desc : periodMap.keySet()) {
            Integer period = periodMap.get(desc);
            stream.addActiveRange(period, desc, item);
        }
        streams.put(item, stream);
        stream.addInfo(timestamp, value);
        stream.fixActiveRanges();
    }

    /**
     * Add a value with timestamp to a named stream.  Note: due to the way ActiveRanges
     * perform incremental calculation, this requires that timestamp be monotonically
     * increasing (i.e. you can't insert a timestamp after you've inserted a later
     * stamp.  To avoid losing information if this occurs,
     *
     * @param item
     * @param timestamp
     * @param value
     */
    public void addValue(String item, long timestamp, int value) {
        if (timestamp < lastTick) {
            timestamp = lastTick;
        } else {
            for (Stream stream : streams.values()) {
                stream.setLastTick(timestamp);
            }
        }
        Stream str = getStream(item);
        if (str != null) {
            str.addInfo(timestamp, value);
        } else {
            createStream(item, timestamp, value);
        }
    }

    public Set<String> getValues() {
        return streams.keySet();
    }

    // Utility methods to allow writing time periods in comprehensible units and converting them in millis

    public static int millis(int amt) {
        return amt;
    }

    public static int seconds(int amt) {
        return amt * millis(1000);
    }

    public static int minutes(int amt) {
        return  amt * seconds(60);
    }

    public static int hours(int amt) {
        return  amt * minutes(60);
    }

    public static int days(int amt) {
        return  amt * hours(24);
    }

    private static String toEnglish(String item, int millis) {
        String units;
        if (millis >= days(1)) units = makePhrase(millis, days(1), "day");
        else if (millis >= hours(1)) units = makePhrase(millis, hours(1), "hour");
        else if (millis >= minutes(1)) units = makePhrase(millis, minutes(1), "minute");
        else if (millis >= seconds(1)) units = makePhrase(millis, seconds(1), "second");
        else units = String.format("%d millis", millis);
        return String.format("%s over last %s", item, units);
    }

    private static String makePhrase(int amt, int unitAmt, String unitName) {
        if (amt == unitAmt) { return "one " + unitName; }
        double unitfreq = ((double) amt) / unitAmt;
        int intfreq = (int) unitfreq;
        if (unitfreq == intfreq) {
            return String.format("%d %ss", intfreq, unitName);
        }
        return String.format("%f %ss", unitfreq, unitName);
    }

    public Set<String> getActiveRanges() {
        return periodMap.keySet();
    }

    public ActiveRange getActiveRange(String range, String item) {
        Stream str = getStream(item);
        ActiveRange activeRange = str.getActiveRange(range);
        return activeRange.adjust(lastTick);
    }
}
