package orion.crypto;

import java.math.BigInteger;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Static utility methods for encrypting strings using AES-128.
 *
 * @author Bruce Israel <bisrael@bericotechnologies.com>
 *
 */
public class AESUtils {

    private static final String ALGORITHM = "AES";
    private static final String RNG_ALGORITHM = "SHA1PRNG";

    protected static byte[] getRawKey(byte[] seed) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance(ALGORITHM);
        SecureRandom sr = SecureRandom.getInstance(RNG_ALGORITHM);
        sr.setSeed(seed);
        kgen.init(128, sr); // 192 and 256 bits may not be available
        SecretKey skey = kgen.generateKey();
        byte[] raw = skey.getEncoded();
        return raw;
    }

    protected static String encrypt(byte[] rawKey, String message) throws Exception {
        byte[] cleartext = message.getBytes();
        SecretKeySpec skeySpec = new SecretKeySpec(rawKey, ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] encrypted = cipher.doFinal(cleartext);
        return toHex(encrypted);
    }

    protected static String decrypt(byte[] rawKey, String s) throws Exception {
        byte[] encrypted = toByte(s);
        SecretKeySpec skeySpec = new SecretKeySpec(rawKey, ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        byte[] decrypted = cipher.doFinal(encrypted);
        return new String(decrypted);
    }

    protected static byte[] toByte(String hexString) {
        int len = hexString.length()/2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++)
            result[i] = Integer.valueOf(hexString.substring(2*i, 2*i+2), 16).byteValue();
        return result;
    }

    protected static String toHex(byte[] bytes) {
        if (bytes.length == 0) return "";
        BigInteger bigint = new BigInteger(1, bytes);
        return String.format("%0" + (bytes.length << 1) + "X", bigint);
    }
}
