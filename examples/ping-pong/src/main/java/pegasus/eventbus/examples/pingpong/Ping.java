package pegasus.eventbus.examples.pingpong;

/**
 * The "Ping" object.  Ping shares the same properties as Pong,
 * but we use two seperate classes to distinguish between the events.
 * Admittedly, this serves no actual purpose other than to demonstrate
 * how two services can communicate between each other.
 * 
 * @author Richard Clayton (Berico Technologies)
 */
public class Ping extends PingPongBase {

	public Ping(String sender) {
		super(sender);
	}
	
	public Ping(String sender, PingPongBase previous) {
		super(sender, previous);
	}

	/**
	 * Time difference between this event and the last received "Pong".
	 * @return timespan in nanoseconds
	 */
	public long timeSinceLastPong(){
		return (this.previousTimestamp == -1l 
				 || this.timestamp < this.previousTimestamp)
				 	? -1l : this.timestamp - this.previousTimestamp;
	}

	@Override
	public String toString() {
		
		return String.format(
				"PING! from %s at %s [with a %s ns latency]", 
				this.getSender(), 
				this.getTimestamp(),
				this.timeSinceLastPong());
	}	
	
	
	
}
