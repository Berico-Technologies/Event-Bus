package pegasus.esp.data;

/**
 * a Bidirectional linked list for values at timestamps.
 * 
 * @author israel
 *
 */
public class TimeEntry {

	private TimeEntry prev;
	private long timestamp;
	private int value;
	private TimeEntry next;
	
	public TimeEntry getPrev() {
		return prev;
	}
	
	public long getTimestamp() {
		return timestamp;
	}

	public int getValue() {
		return value;
	}

	public TimeEntry getNext() {
		return next;
	}

	public TimeEntry(TimeEntry prev, long timestamp, int value, TimeEntry next) {
		super();
		this.prev = prev;
		this.timestamp = timestamp;
		this.value = value;
		this.next = next;
	}

	public TimeEntry(long timestamp, int value) {
		this(null, timestamp, value, null);
	}

	public static TimeEntry makeStartEntry() {
		TimeEntry entry = new TimeEntry(null, 0, 0, null);
		entry.next = entry;
		entry.prev = entry;
		return entry;
	}
	
	@Override
	public String toString() {
		return "TimeEntry [prev=" + prev.getTimestamp() + ", timestamp=" + timestamp
				+ ", value=" + value + ", next=" + next.getTimestamp() + "]";
	}

	public static TimeEntry makeEndEntry(TimeEntry start) {
		TimeEntry entry = new TimeEntry(start, Long.MAX_VALUE, 0, null);
		start.next = entry;
		entry.next = entry;
		return entry;
	}

	public void insertAfter(TimeEntry nprev) {
		TimeEntry nnext = nprev.next;
		this.prev = nprev;
		this.next = nnext;
		nprev.next = this;
		nnext.prev = this;
	}
	

}
