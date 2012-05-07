package orion.crypto;

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
