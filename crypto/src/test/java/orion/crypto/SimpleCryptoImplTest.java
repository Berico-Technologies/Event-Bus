package orion.crypto;

import static org.junit.Assert.*;

import org.junit.Test;

import orion.crypto.SimpleCryptoImpl;

public class SimpleCryptoImplTest {
	
	private static void assertEncryptionDecryptionWorks(String pass, String text) throws Exception {
		SimpleCryptoImpl crypto = new SimpleCryptoImpl(pass);
		String enctext = crypto.encrypt(text);
		assertNotNull(enctext);
		assertFalse(enctext.equals(text));
		String dectext = crypto.decrypt(enctext);
		assertNotNull(dectext);
		assertTrue(dectext.equals(text));
	}

	@Test
	public void testRoundTrip() throws Exception {
		assertEncryptionDecryptionWorks("pass", "text for encryption");
	}
	
	@Test
	public void testEmptyPassword() throws Exception {
		assertEncryptionDecryptionWorks("", "text for encryption");
	}
	
	@Test
	public void testEmptyString() throws Exception {
		assertEncryptionDecryptionWorks("pass", "");
	}
	
	@Test
	public void testEmptyStringAndPassword() throws Exception {
		assertEncryptionDecryptionWorks("", "");
	}
	
	
	@Test
	public void testOneCharPassword() throws Exception {
		assertEncryptionDecryptionWorks("x", "more text for encryption");
	}
	
	@Test
	public void testOneCharString() throws Exception {
		assertEncryptionDecryptionWorks("a key", "y");
	}
	
	@Test
	public void testNullPassword() throws Exception {
		try {
			SimpleCryptoImpl crypto = new SimpleCryptoImpl(null);
		} catch (NullPointerException e) {
			// Expected; Should throw exception
		}
	}

	@Test
	public void testEncryptNullString() throws Exception {
		SimpleCryptoImpl crypto = new SimpleCryptoImpl("pass");
		try {
			String enctext = crypto.encrypt(null);
			fail("Should not be able to encrypt a null string");
		} catch (Exception e) {
			// Expected
		}
	}
	
	@Test
	public void testDecryptNullString() throws Exception {
		SimpleCryptoImpl crypto = new SimpleCryptoImpl("pass");
		try {
			String dectext = crypto.decrypt(null);
			fail("Should not be able to decrypt a null string");
		} catch (Exception e) {
			// Expected
		}
	}
}
