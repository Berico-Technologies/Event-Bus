package pegasus.eventbus.rabbitmq;

public interface SimpleCrypto {
	
	public String encrypt(String s) throws Exception;
	public String decrypt(String s) throws Exception;
	
}
