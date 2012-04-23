package pegasus.eventbus.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.impl.DefaultExceptionHandler;

public class BericoExceptionHandler extends DefaultExceptionHandler {

	private static final Logger logger = LoggerFactory.getLogger(BericoExceptionHandler.class);

	public BericoExceptionHandler(){
		logger.debug("init");
	}

	@Override
	public void handleUnexpectedConnectionDriverException(Connection conn, Throwable exception) {
		logger.error("handleUnexpectedConnectionDriverException. Connection: " + conn, exception);		
		super.handleUnexpectedConnectionDriverException(conn, exception);
	}

	@Override
	protected void handleChannelKiller(Channel channel, Throwable exception, String what) {
		try{
			logger.error(what + " threw exception for channel: " + channel, exception);
			super.handleChannelKiller(channel, exception, what);			
		}catch (RuntimeException e) {
			// TODO: Determine if this Exception should be caught or thrown
			logger.error("Error calling DefaultExceptionHandler.handleChannelKiller(): " + what + ": OrigException: " + exception, e);
			logger.error("Re-Throwing exception!  Is this the correct behavior:" + e);
			throw e;
		}
	}

}