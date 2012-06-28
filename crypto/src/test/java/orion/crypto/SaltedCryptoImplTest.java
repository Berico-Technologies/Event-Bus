package orion.crypto;

import static org.junit.Assert.*;

import org.junit.Test;

import orion.crypto.SaltedCryptoImpl;

public class SaltedCryptoImplTest {

    private static void assertEncryptionDecryptionWorks(String pass, String text) throws Exception {
        SaltedCryptoImpl crypto = new SaltedCryptoImpl(pass, 10);
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
            SaltedCryptoImpl crypto = new SaltedCryptoImpl(null, 10);
        } catch (NullPointerException e) {
            // Expected; Should throw exception
        }
    }

    @Test
    public void testEncryptNullString() throws Exception {
        SaltedCryptoImpl crypto = new SaltedCryptoImpl("pass", 10);
        try {
            String enctext = crypto.encrypt(null);
            fail("Should not be able to encrypt a null string");
        } catch (Exception e) {
            // Expected
        }
    }

    @Test
    public void testDecryptNullString() throws Exception {
        SaltedCryptoImpl crypto = new SaltedCryptoImpl("pass", 0);
        try {
            String dectext = crypto.decrypt(null);
            fail("Should not be able to decrypt a null string");
        } catch (Exception e) {
            // Expected
        }
    }

    @Test
    public void testEncryptDecryptWithNoSalt() throws Exception {
        String password = "Sword Fish";
        String messageText = "Why a Duck?";
        SaltedCryptoImpl sci = new SaltedCryptoImpl(password, 00);
        String enctext = sci.encrypt(messageText);
        String roundTripProcessed = sci.decrypt(enctext);
        assertTrue(roundTripProcessed.equals(messageText));
    }

    @Test
    public void testEncryptDecryptWithSeparateInstances() throws Exception {
        String password = "Sword Fish";
        String messageText = "Why a Duck?";
        SaltedCryptoImpl encrypter = new SaltedCryptoImpl(password, 10);
        SaltedCryptoImpl decrypter = new SaltedCryptoImpl(password, 10);
        String enctext = encrypter.encrypt(messageText);
        String roundTripProcessed = decrypter.decrypt(enctext);
        assertTrue(roundTripProcessed.equals(messageText));
    }

    @Test
    public void testEncryptDecryptWithDifferentSaltSizes() throws Exception {
        String password = "Sword Fish";
        String messageText = "Why a Duck?";

        SaltedCryptoImpl sci1 = new SaltedCryptoImpl(password, 10);
        SaltedCryptoImpl sci2 = new SaltedCryptoImpl(password, 5);

        String enctext1 = sci1.encrypt(messageText);
        String enctext2 = sci2.encrypt(messageText);

        String decrypt1 = sci1.decrypt(enctext1);
        String decrypt2 = sci2.decrypt(enctext2);

        assertFalse(enctext1.equals(enctext2));
        assertTrue(decrypt1.equals(decrypt2));
        assertTrue(decrypt1.equals(messageText));
    }

    @Test
    public void testMultipleEncryptionsEncryptDifferently() throws Exception {
        String password = "n Ebg Guvegrra'rq cnffjbeq";
        String messageText = "Vs lbh pna ernq guvf, gura vg'f boivbhf gung guvf rapelcgvba zrgubq qbrfa'g jbex irel jryy";
        SaltedCryptoImpl sci = new SaltedCryptoImpl(password, 10);
        String enctext1 = sci.encrypt(messageText);
        String enctext2 = sci.encrypt(messageText);
        String decrypt1 = sci.decrypt(enctext1);
        String decrypt2 = sci.decrypt(enctext2);

        assertFalse(enctext1.equals(enctext2));
        assertTrue(decrypt1.equals(decrypt2));
        assertTrue(decrypt1.equals(messageText));
    }
}
