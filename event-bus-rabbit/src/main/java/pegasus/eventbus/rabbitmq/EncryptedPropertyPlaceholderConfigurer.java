package pegasus.eventbus.rabbitmq;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

public class EncryptedPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer {
	
	private static final String ENCRYPTED = "ENCRYPTED:";
	private SimpleCrypto crypto = new SimpleCrypto() {
		
		@Override
		public String encrypt(String s) {
			return s;
		}
		
		@Override
		public String decrypt(String s) {
			return s;
		}
	};
	
	protected String convertPropertyValue(String originalValue) {
		if(originalValue != null && originalValue.startsWith(ENCRYPTED)){
			crypto.decrypt(originalValue.replaceFirst(ENCRYPTED, ""));
		}
		return originalValue;
	}

}
