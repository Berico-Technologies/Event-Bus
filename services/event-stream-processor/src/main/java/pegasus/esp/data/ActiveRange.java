package pegasus.esp.data;

import pegasus.esp.data.StreamRange.TimeValFunc;

/*
 * An ActiveRange is a range of a stream that represents an interval with a
 * maximum time width, specified by the periodInMillis initialization parameter.
 * 
 * @author israel
 *
 */
public class ActiveRange extends StreamRange {
    public String getCategory() {
        return category;
    }

    public String getItem() {
        return item;
    }

    private int periodInMillis;
    private String category;
    private String item;
    private ActiveRange prev = null;
    private int total = 0;
    private int adjustedSize = 0;
    private TimeEntry lastEnd;
    private StreamRange range;

    public int getTotal() {
        return total;
    }

    public int getAdjustedSize() {
        return adjustedSize;
    }
    
    public int getTrend() {
        int curtotal = getTotal();
        if (prev == null || curtotal == 0) return 0;
        int prevtotal = prev.getTotal();
        int trendval = (curtotal - prevtotal) * 100 / prevtotal;
        System.out.println(String.format("%s -> %s = %s%%", prevtotal, curtotal, trendval));
        return trendval;
    }

    public ActiveRange(StreamRange range, int periodInMillis, String category, String item) {
        this(range, range.start, range.end, periodInMillis, category, item);
    }

    public ActiveRange(StreamRange range, 
            TimeEntry start, TimeEntry end, int periodInMillis, String category, String item) {
        super(start, end);
        this.range = range;
        this.periodInMillis = periodInMillis;
        this.category = category;
        this.item = item;
    }

    /**
     *  Write debugging output to a file
     *
     * @param fname the file to be written to
     * @param data The debug string to be written
     **/

    public static void fprint(String fname, String data) {
      java.io.PrintWriter file;
      boolean append = true;
      try {
        file = new java.io.PrintWriter(new java.io.FileOutputStream(fname, append));
      } catch (Exception exc) {
        return;
      }
      file.print(data + "\n");
      file.close();
    }
    
    private void dbgAdj(String str) {
        fprint("/tmp/ranges.dbg", str);
//        System.out.println("@@@@ " + str);
    }
    
    public ActiveRange getPrev() {
        return prev;
    }

    private void dbg(String str) {
        boolean show = true;
        show = false;
        if (show) {
            System.out.println(this + "(" + category + ")" + str);
        }
    }
    
    // TODO: fix bug where no events within a period doesn't cause the ActiveRange to become empty
    public ActiveRange adjust(long now) {
        dbgAdj("Pre-Adjust: " + this + " based on: " + range);

        dbg("INIT: " + total + "/" + adjustedSize + String.format(" with [%s,%s]", start, lastEnd));
        lastEnd = updateEnd(end);
        dbgAdj("Mid-Adjust: " + this);
        start = updateStart(getLast().getTimestamp());
        dbgAdj("Post-Adjust: " + this);
        return this;
    }

    private TimeEntry updateEnd(TimeEntry stopEntry) {
        long timestamp = stopEntry.getTimestamp();
        while (lastEnd.getNext() != stopEntry) {
            lastEnd = lastEnd.getNext();
            total += lastEnd.getValue();
            adjustedSize++;
            dbg("=> looking at " + lastEnd + "=" + total + "/" + adjustedSize);
        }
        return lastEnd;
    }

    private TimeEntry updateStart(long finaltime) {
        TimeEntry wkgstart = getFirst();
        boolean adjusted = false;
        long startlimit = finaltime - periodInMillis;
        dbg("Moving start (currently at " + wkgstart.getTimestamp() + "->" + wkgstart.getNext().getTimestamp() + ") with a limit of " + startlimit);
        while (wkgstart.getTimestamp() < startlimit) {
            total -= wkgstart.getValue();
            adjustedSize--;
            dbgAdj("<= looking at " + wkgstart + "=" + total + "/" + adjustedSize);
            wkgstart = wkgstart.getNext();
            adjusted = true;
        }

        if (adjusted) {
            if (prev == null) {
                prev = new ActiveRange(range, range.start, wkgstart, periodInMillis, 
                        "PREV PERIOD " + category, item); 
                prev.initialize();
            } else {
                prev.updateEnd(wkgstart);
                prev.updateStart(prev.getLast().getTimestamp());
            }
        }
        return wkgstart.getPrev();
    }

    public void initialize() {
        adjustedSize = 0;
        total = 0;
        TimeEntry working = getFirst();
        while (working != this.end) {
            total += working.getValue();
            adjustedSize++;
            working = working.getNext();
        }
        lastEnd = getLast();
        dbgAdj("Initialize: " + this + " from " + range);
        
    }

    public void dumpActiveRanges(String vsname, String name) {
        dumpActiveRanges(vsname, name, "");
    }
    
    public void dumpActiveRanges(String vsname, String name, String indent) {
        System.out.println(String.format(indent + "/%s/%s[%d] %s = %s", 
                vsname, name, periodInMillis, category, this));
        if (prev != null) {
            prev.dumpActiveRanges(vsname, name, "  " + indent);
        }
    }
    
    @Override
    public String toString() {
        
        TimeValFunc<String> makeString = new TimeValFunc<String>() {

            StringBuffer sb = new StringBuffer();
            String sep = "";
            
            @Override
            public String apply(long time, int value, boolean last) {
                if (time == lastEnd.getTimestamp()) sep = sep + "&";
                sb.append(String.format("%s%s@%s", sep, value, time));
                sep = ", ";
                if (last) return sb.toString();
                return null;
            }
            
        };
        String itemList = applyTo(makeString);

        return String.format("%s(%s) = %s < %s/%s >", category, item, itemList, total, adjustedSize);
    }
}
