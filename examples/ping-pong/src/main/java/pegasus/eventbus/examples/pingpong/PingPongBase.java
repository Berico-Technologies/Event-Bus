package pegasus.eventbus.examples.pingpong;

public abstract class PingPongBase {

	protected String sender;
	protected long timestamp = System.nanoTime();
	protected long previousTimestamp;
	
	public PingPongBase(String sender) {
		
		this.sender = sender;
		this.previousTimestamp = -1l;
	}
	
	public PingPongBase(String sender, PingPongBase previous){
		
		this.sender = sender;
		this.previousTimestamp = previous.getTimestamp();
	}

	public String getSender() {
		return sender;
	}
	
	public long getTimestamp() {
		return timestamp;
	}
	
	public long getPreviousTimestamp() {
		return previousTimestamp;
	}
	
}
