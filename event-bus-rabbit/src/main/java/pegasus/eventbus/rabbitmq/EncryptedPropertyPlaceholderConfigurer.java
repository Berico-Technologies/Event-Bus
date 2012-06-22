package pegasus.eventbus.rabbitmq;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

import com.berico.crypto.OrionEncrypt;
import com.berico.crypto.SimpleCrypto;

public class EncryptedPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer {
	
	private static final String ENCRYPTED = "ENCRYPTED:";
	private SimpleCrypto crypto = new OrionEncrypt();
	
	protected String convertPropertyValue(String originalValue) {
		try {
			if (originalValue != null && originalValue.startsWith(ENCRYPTED)) {
				String strippedVal = originalValue.substring(ENCRYPTED.length());
				return crypto.decrypt(strippedVal);
			}
		} catch (Exception e) {
		}
		return originalValue;
	}

}
