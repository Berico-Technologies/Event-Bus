package pegasus.eventbus.rabbitmq;

import java.math.BigInteger;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;


public class SimpleCryptoImpl implements SimpleCrypto {

	private static final String ALGORITHM = "AES";
	private static final String RNG_ALGORITHM = "SHA1PRNG";

	private String password;
	private byte[] rawKey;

	public SimpleCryptoImpl(String password) throws Exception {
		super();
		this.password = password;
		this.rawKey = getRawKey(password);
	}

	private static byte[] getRawKey(String seed) throws Exception {
		KeyGenerator kgen = KeyGenerator.getInstance(ALGORITHM);
		SecureRandom sr = SecureRandom.getInstance(RNG_ALGORITHM);
		sr.setSeed(seed.getBytes());
		kgen.init(128, sr); // 192 and 256 bits may not be available
		SecretKey skey = kgen.generateKey();
		byte[] raw = skey.getEncoded();
		return raw;
	}

	@Override
	public String encrypt(String s) throws Exception {
		return encrypt(rawKey, s.getBytes());
	}

	private static String encrypt(byte[] rawKey, byte[] cleartext) throws Exception {
		SecretKeySpec skeySpec = new SecretKeySpec(rawKey, ALGORITHM);
		Cipher cipher = Cipher.getInstance(ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
		byte[] encrypted = cipher.doFinal(cleartext);
		return toHex(encrypted);
	}

	@Override
	public String decrypt(String s) throws Exception {
		return decrypt(rawKey, toByte(s));
	}

	private static String decrypt(byte[] rawKey, byte[] encrypted) throws Exception {
		SecretKeySpec skeySpec = new SecretKeySpec(rawKey, ALGORITHM);
		Cipher cipher = Cipher.getInstance(ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, skeySpec);
		byte[] decrypted = cipher.doFinal(encrypted);
		return new String(decrypted);
	}

	private static byte[] toByte(String hexString) {
		int len = hexString.length()/2;
		byte[] result = new byte[len];
		for (int i = 0; i < len; i++)
			result[i] = Integer.valueOf(hexString.substring(2*i, 2*i+2), 16).byteValue();
		return result;
	}

	private static String toHex(byte[] bytes) {
		BigInteger bigint = new BigInteger(1, bytes);
		return String.format("%0" + (bytes.length << 1) + "X", bigint);
	}
}
