package pegasus.eventbus.rabbitmq;

import javax.net.ssl.SSLContext;

import pegasus.eventbus.amqp.AmqpConnectionParameters;

public interface SSLContextFactory {
	
	public SSLContext getInstance(AmqpConnectionParameters params);
	
}
