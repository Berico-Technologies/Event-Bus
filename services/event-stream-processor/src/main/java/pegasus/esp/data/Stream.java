package pegasus.esp.data;

import java.util.ArrayList;
import java.util.List;

import pegasus.esp.data.StreamRange.TimeValFunc;

public class Stream {
    private String name;
    private StreamRange streamref;
    private List<ActiveRange> activeRanges = new ArrayList<ActiveRange>();
    private long lastTick;

    public Stream(String name) {
        super();
        this.name = name;
        this.streamref = new StreamRange();
    }

    public void addInfo(long timestamp, int value) {
        long lastTimestamp = streamref.getLast().getTimestamp();
        if (timestamp < lastTimestamp) {
            // TODO: deal with this situation in a cleaner fashion
            // HACK alert!  To avoid losing data when information comes in out of order
            // (this may not actually be possible), this will adjust earlier data to be
            // the same as the time for the already stored later data.
	    boolean showTimeWarnings = false;
            if (showTimeWarnings && lastTimestamp > timestamp + 100) {
                System.err.println(String.format("WARNING: timestamp=%d inserted after last timestamp=%d in stream %s", timestamp, lastTimestamp, name));
            }
            timestamp = lastTimestamp;
        }
        TimeEntry newEntry = new TimeEntry(timestamp, value);
        streamref.append(newEntry);
    }

    public void addActiveRange(int periodInMillis, String category, String item) {
        ActiveRange ar = new ActiveRange(streamref, periodInMillis, category, item);
        activeRanges.add(ar);
    }

    public void fixActiveRanges() {
        for (ActiveRange ar : activeRanges) {
            ar.initialize();
        }
        // TODO Auto-generated method stub

    }

    public int size() {
        return streamref.size();
    }

    public TimeEntry get(int i) {
        return streamref.get(i);
    }

//     public void dumpActiveRanges(String vsname) {
//         for (ActiveRange ar : activeRanges) {
//             ar.adjust(lastTick).dumpActiveRanges(vsname, name);
//         }
//     }

//     public void dump() {
//         TimeValFunc<Object> dumper = new TimeValFunc<Object>() {
//             int ct = 0;
//             @Override
//             public Object apply(long time, int value, boolean last) {
//                 System.out.println("  " + (last ? "* " : "  ") + (ct++) + ": " +
//                         value + " @ " + time);
//                 return null;
//             }
//         };

//         System.out.println("  " + name + ":");
//         streamref.applyTo(dumper);
//         System.out.println();
//     }

//     public void dbgdump() {
//         int ct = 0;
//         TimeEntry itm = get(0);
//         showItem(ct, itm);
//         while (itm.getNext() != itm) {
//             itm = itm.getNext();
//             ct++;
//             showItem(ct, itm);
//         }
//         System.out.println();
//     }

//     private void showItem(int i, TimeEntry itm) {
//         System.out.println(i + ": " + itm);
//     }

    public List<ActiveRange> getActiveRanges() {
        return activeRanges;
    }

    public ActiveRange getActiveRange(String rangeName) {
        for (ActiveRange range : activeRanges) {
            if (rangeName.equals(range.getCategory())) {
                return range;
            }
        }
        return null;
    }

    public void setLastTick(long timestamp) {
        this.lastTick = timestamp;
    }
}
