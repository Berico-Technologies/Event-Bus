package pegasus.eventbus.examples.pingpong;

/**
 * The "Pong" object.  Pong shares the same properties as Ping,
 * but we use two seperate classes to distinguish between the events.
 * Admittedly, this serves no actual purpose other than to demonstrate
 * how two services can communicate between each other.
 * 
 * @author Richard Clayton (Berico Technologies)
 */
public class Pong extends PingPongBase {

	public Pong(String sender) {
		super(sender);
	}

	public Pong(String sender, PingPongBase previous) {
		super(sender, previous);
	}

	/**
	 * Time difference between this event and the last received "Ping".
	 * @return timespan in nanoseconds
	 */
	public long timeSinceLastPing(){
		return (this.previousTimestamp == -1l 
				 || this.timestamp < this.previousTimestamp)
				 	? -1l : this.timestamp - this.previousTimestamp;
	}

	@Override
	public String toString() {
		
		return String.format(
				"PONG! from %s at %s [with a %s ns latency]", 
				this.getSender(), 
				this.getTimestamp(),
				this.timeSinceLastPing());
	}	
	
	
}
