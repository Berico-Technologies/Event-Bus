package orion.crypto;

/**
 * OrionEncrypt is a class for doing static encryption/decryption for Orion
 * server communication.  This has two purposes; as a main routine this can be
 * used by install scripts to generate encryption strings that will be sent to
 * the server.  Additionally, the instantiation of this class gives an
 * entity that contains the static encryption string and which the server can
 * use for both encryption and decryption.
 *
 * @author israel
 *
 */
public class OrionEncrypt implements SimpleCrypto{

    private final String seedfirst = "Use me for doing the ";
    private SaltedCryptoImpl sci;


    public OrionEncrypt(){
        String wrapper = "--";
        // Construct the seed dynamically so that simple strings examination of the jar
        // file cannot find the key.  Note that this will not stop an attacker who
        // is able to reverse engineer the byte-code and run it.
        try{
            sci = new SaltedCryptoImpl(wrapper + seedfirst + getClass().getSimpleName() + wrapper, 10);        	
        }catch (Exception e) {
        	throw new RuntimeException(e);
		}
    }

    public String encrypt(String s) throws Exception {
        return sci.encrypt(s);
    }

    public String decrypt(String s) throws Exception {
        return sci.decrypt(s);
    }

    /**
     * Encrypt each of the command-line parameters and write the encrypted version
     * out to standard out.
     *
     * @param args a list of strings to be encrypted.
     * @throws Exception Should never be thrown
     */
    public static void main(String[] args) throws Exception {
        OrionEncrypt oe = new OrionEncrypt();
        for (String arg : args) {
            String encrypted = oe.encrypt(arg);
            System.out.format("%s\n", encrypted);
        }
    }

}
