package pegasus.eventbus.rabbitmq;

import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import com.rabbitmq.client.NullTrustManager;

import pegasus.eventbus.amqp.AmqpConnectionParameters;

public class SSLContextFactoryImpl implements SSLContextFactory {

	private static final String DEFAULT_SSL_PROTOCOL = "TLSv1";
	
	
	private TrustManager[] getTrustManagerFactory(URL path, String password) throws GeneralSecurityException, IOException{
		TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(getKeyStore(path, password));
        return tmf.getTrustManagers();
	}
	
	private KeyManager[] getKeyManagerFactory(URL path, String password) throws GeneralSecurityException, IOException{
		KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
		kmf.init(getKeyStore(path, password), password.toCharArray());
		return kmf.getKeyManagers();
	}
	
	private KeyStore getKeyStore(URL path, String password) throws IOException, GeneralSecurityException{
		KeyStore ks;		
		if(path.toExternalForm().endsWith("p12")){
			ks = java.security.KeyStore.getInstance("PKCS12");			
		}else{			
			ks = KeyStore.getInstance("JKS");
		}
		
		ks.load(path.openStream(), password.toCharArray());			
		return ks;
	}
	
	private SSLContext getInstance(URL keyStore, String keyStorePassword, URL trustStore, String trustStorePassword) throws GeneralSecurityException, IOException{
		SSLContext sslContext = SSLContext.getInstance(DEFAULT_SSL_PROTOCOL);
		KeyManager km[] = null;
		if(keyStore != null && keyStorePassword != null){
			km = getKeyManagerFactory(keyStore, keyStorePassword);
		}
		
		TrustManager tm[] = null;
		if(trustStore != null && trustStorePassword != null){
			tm = getTrustManagerFactory(keyStore, keyStorePassword);
		}
		sslContext.init(km, tm, null);
		return sslContext;
	}
	
	private SSLContext getPermissive() throws GeneralSecurityException{
		SSLContext instance = SSLContext.getInstance(DEFAULT_SSL_PROTOCOL);
		instance.init(null, new TrustManager[]{new NullTrustManager()}, null);
		return instance;
	}
	
	@Override
	public SSLContext getInstance(AmqpConnectionParameters params) {		
		try{
			if(params.isOneWaySSL()){
				return getPermissive();
			}else{
				return getInstance(params.getKeyStore(), params.getKeyStorePassword(), params.getTrustStore(), params.getTrustStorePassword());
			}
		}catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
