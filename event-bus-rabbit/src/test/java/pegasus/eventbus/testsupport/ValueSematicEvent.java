package pegasus.eventbus.testsupport;

import java.util.UUID;

/**
 * An event class that uses value semantics for equality instead of reference semantics
 */
public class ValueSematicEvent {
	public final UUID id ;
	
	public ValueSematicEvent(UUID id){
		this.id = id;
	}
	
	@Override
	public int hashCode() {
		return id.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
	       if (this == obj) {
	            return true;
	        }
	        if (obj == null) {
	            return false;
	        }
	        if (!(obj instanceof ValueSematicEvent)) {
	            return false;
	        } else {
	        	return ((ValueSematicEvent) obj).id.equals(id);
	        }
	}
}