package pegasus.eventbus.rabbitmq;

public interface SimpleCrypto {
	
	public String encrypt(String s);
	public String decrypt(String s);
	
}
