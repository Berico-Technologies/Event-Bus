package orion.crypto;

import java.security.SecureRandom;

public class SaltedCryptoImpl implements SimpleCrypto {

    private SaltedSeed saltedSeed;

    static class SaltedSeed {
        private int saltSize;
        private byte[] initialSeed;
        private byte[] salt;

        SaltedSeed(byte[] initialSeed, int saltSize) {
            this.initialSeed = initialSeed;
            this.saltSize = saltSize;
        }

        static private byte[] makeSalt(int saltbytes) {
            byte[] salt = new byte[saltbytes];
            new SecureRandom().nextBytes(salt);
            return salt;
        }

        public byte[] getRawKey() throws Exception {
            byte[] fullSeed = new byte[initialSeed.length + salt.length];
            System.arraycopy(initialSeed, 0, fullSeed, 0, initialSeed.length);
            System.arraycopy(salt, 0, fullSeed, initialSeed.length, salt.length);
            return AESUtils.getRawKey(fullSeed);
        }

        public String encrypt(String s) throws Exception {
            this.salt = makeSalt(saltSize);
            return AESUtils.encrypt(getRawKey(), s) + AESUtils.toHex(salt);
        }

        public String decrypt(String saltedEncr) throws Exception {
            String baseEncr = extractBaseEncrypted(saltedEncr);
            return AESUtils.decrypt(getRawKey(), baseEncr);
        }

        public String extractBaseEncrypted(String saltedEncr) {
            int splitPoint = saltedEncr.length() - (2 * saltSize);
            String hexSalt = saltedEncr.substring(splitPoint);
            salt = AESUtils.toByte(hexSalt);
            String baseEncr = saltedEncr.substring(0, splitPoint);
            return baseEncr;
        }
    }

    public SaltedCryptoImpl(String password, int saltSize) throws Exception {
        super();
        this.saltedSeed = new SaltedSeed(password.getBytes(), saltSize);
    }

    @Override
    public String encrypt(String s) throws Exception {
        return saltedSeed.encrypt(s);
    }

    @Override
    public String decrypt(String saltedEncr) throws Exception {
        return saltedSeed.decrypt(saltedEncr);
    }
}
