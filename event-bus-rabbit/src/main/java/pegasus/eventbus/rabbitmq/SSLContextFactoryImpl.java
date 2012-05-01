package pegasus.eventbus.rabbitmq;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import com.rabbitmq.client.NullTrustManager;

import pegasus.eventbus.amqp.AmqpConnectionParameters;

public class SSLContextFactoryImpl implements SSLContextFactory {

	private static final String DEFAULT_SSL_PROTOCOL = "SSLv3";
	
	@Override
	public SSLContext getInstance(AmqpConnectionParameters params) {
		try{
			SSLContext c = SSLContext.getInstance(DEFAULT_SSL_PROTOCOL);
			c.init(null, new TrustManager[] { new NullTrustManager() }, null);
			return c;
		}catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

}
