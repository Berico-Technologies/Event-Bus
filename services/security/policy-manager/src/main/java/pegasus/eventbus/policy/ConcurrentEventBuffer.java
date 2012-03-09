package pegasus.eventbus.policy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;

public class ConcurrentEventBuffer implements EventBuffer {

	protected static int DEFAULT_BUFFER_CAPACITY = 10000;
	
	protected int bufferCapacity = -1;
	
	protected ArrayBlockingQueue<EventSubmission> buffer;

	public ConcurrentEventBuffer(){
		
		this(DEFAULT_BUFFER_CAPACITY);
	}
	
	public ConcurrentEventBuffer(int bufferCapacity){
		
		this.bufferCapacity = bufferCapacity;
		this.buffer = new ArrayBlockingQueue<EventSubmission>(bufferCapacity);
	}
	
	public void addEvent(EventSubmission event) {
		if(buffer.size() < this.bufferCapacity){
			
			buffer.add(event);
		
		} else {
		
			throw new RuntimeException("Buffer is full.  Consider increasing the capacity.");
		}
	}

	public Collection<EventSubmission> drain(int maxCapacity) {
		
		ArrayList<EventSubmission> eventSink = new ArrayList<EventSubmission>();
		
		this.buffer.drainTo(eventSink, maxCapacity);
		
		return eventSink;
	}
	
	
	
}
