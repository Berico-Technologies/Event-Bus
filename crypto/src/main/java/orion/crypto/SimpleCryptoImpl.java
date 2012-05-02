package orion.crypto;

import java.math.BigInteger;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;



public class SimpleCryptoImpl implements SimpleCrypto {

	private byte[] rawKey;

	public SimpleCryptoImpl(String password) throws Exception {
		super();
		this.rawKey = AESUtils.getRawKey(password.getBytes());
	}

	@Override
	public String encrypt(String s) throws Exception {
		return AESUtils.encrypt(rawKey, s);
	}

	@Override
	public String decrypt(String s) throws Exception {
	    return AESUtils.decrypt(rawKey, s);
	}

}
