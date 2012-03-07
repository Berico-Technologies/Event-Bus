package pegasus.esp.data;

import pegasus.esp.data.StreamRange.TimeValFunc;

/**
 * A {@link StreamRange} is an entity on top of a full stream with
 * START and END markers to delimit the range of items of interest.
 * Note that START and END represent an exclusive interval, i.e. the
 * START and END items are not part of this sequence (allowing for an
 * empty interval).
 * 
 * @author israel
 *
 */
public class StreamRange {
	
	public interface TimeValFunc<T extends Object> {
		public T apply(long time, int value, boolean last);
	}

	protected TimeEntry start;
	protected TimeEntry end;
	
	public StreamRange(TimeEntry start, TimeEntry end) {
		super();
		this.start = start;
		this.end = end;
	}

	public StreamRange() {
		this.start = TimeEntry.makeStartEntry();
		this.end = TimeEntry.makeEndEntry(this.start);
	}

	public void append(TimeEntry newEntry) {
			TimeEntry last = this.end;
			while (newEntry.getTimestamp() < last.getTimestamp()) { last = last.getPrev(); }
			newEntry.insertAfter(last);
	}
	
	public String toString() {
	    TimeValFunc<String> makeString = new TimeValFunc<String>() {

	        StringBuffer sb = new StringBuffer();
	        String sep = "";
	        
            @Override
            public String apply(long time, int value, boolean last) {
                sb.append(String.format("%s%s@%s", sep, value, time));
                sep = ", ";
                if (last) return sb.toString();
                return null;
            }
	        
        };
        return applyTo(makeString);
	}
	
	public int size() {
		TimeValFunc<Integer> sizer = new TimeValFunc<Integer>() {
			
			int size = 0;

			@Override
			public Integer apply(long time, int value, boolean last) {
				size += 1;
				if (last) {
					return size;
				}
				return null;
			}
		};
		
		Integer size = applyTo(sizer);
		if (size == null) return 0;
		return size;
	}

	public <T> T applyTo(TimeValFunc<T> tvf) {
		TimeEntry cur = getFirst();
		while (cur != this.end) {
			T res = tvf.apply(cur.getTimestamp(), cur.getValue(), cur == getLast());
			if (res != null) return res;
			cur = cur.getNext();
		}
		return null;
	}

	protected TimeEntry getFirst() {
		return this.start.getNext();
	}

	protected TimeEntry getLast() {
		return this.end.getPrev();
	}

	public TimeEntry get(int i) {
		TimeEntry itm = getFirst();
		while (i > 0) {
			itm = itm.getNext();
			i--;
		}
		return itm;
	}

}
