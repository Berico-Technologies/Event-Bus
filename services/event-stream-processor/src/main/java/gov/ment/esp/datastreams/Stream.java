package gov.ment.esp.datastreams;

import gov.ment.esp.datastreams.StreamRange.TimeValFunc;

import java.util.ArrayList;
import java.util.List;

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
      // HACK alert! To avoid losing data when information comes in out of order
      // (this may not actually be possible), this will adjust earlier data to
      // be
      // the same as the time for the already stored later data.
      boolean showTimeWarnings = false;
      if (showTimeWarnings && lastTimestamp > timestamp + 100) {
        System.err.println(String.format(
                "WARNING: timestamp=%d inserted after last timestamp=%d in stream %s", timestamp,
                lastTimestamp, name));
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
  }

  public int size() {
    return streamref.size();
  }

  public TimeEntry get(int i) {
    return streamref.get(i);
  }

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
