package pegasus.eventbus.rabbitmq;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Address;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.impl.AMQConnection;
import com.rabbitmq.client.impl.DefaultExceptionHandler;
import com.rabbitmq.client.impl.ExceptionHandler;
import com.rabbitmq.client.impl.FrameHandler;

public class BericoConnectionFactory extends ConnectionFactory {
	
	private static final Logger logger = LoggerFactory.getLogger(BericoConnectionFactory.class);
	private int threadPoolSize;
	
	public BericoConnectionFactory(){
		logger.debug("init");
	}

    /**
     *	@return				the threadPoolSize
     */
    public final int getThreadPoolSize() {
        return threadPoolSize;
    }

    /**
     *	@param	threadPoolSize	the threadPoolSize to set
     */
    public final void setThreadPoolSize(int threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
    }

    /**
     * Create a new broker connection
     * @param addrs an array of known broker addresses (hostname/port pairs) to try in order
     * @return an interface to the connection
     * @throws IOException if it encounters a problem
     */
    public Connection newConnection(Address[] addrs) throws IOException {
        return newConnection(Executors.newFixedThreadPool(threadPoolSize), addrs);
    }

    /**
     * Create a new broker connection
     * @return an interface to the connection
     * @throws IOException if it encounters a problem
     */
    public Connection newConnection() throws IOException {
        return newConnection(Executors.newFixedThreadPool(threadPoolSize),
                             new Address[] {new Address(getHost(), getPort())}
                            );
    }

    /**
     * Create a new broker connection
     * @param executor thread execution service for consumers on the connection
     * @return an interface to the connection
     * @throws IOException if it encounters a problem
     */
    public Connection newConnection(ExecutorService executor) throws IOException {
        return newConnection(executor,
                             new Address[] {new Address(getHost(), getPort())}
                            );
    }
    

	/**
     * Create a new broker connection
     * @param executor thread execution service for consumers on the connection
     * @param addrs an array of known broker addresses (hostname/port pairs) to try in order
     * @return an interface to the connection
     * @throws IOException if it encounters a problem
     */
    public Connection newConnection(ExecutorService executor, Address[] addrs)
        throws IOException
    {
        IOException lastException = null;
        for (Address addr : addrs) {
            try { 
            	logger.debug("Creating new connection for address: " + addr);
                FrameHandler frameHandler = createFrameHandler(addr);
                AMQConnection conn =
                    new AMQConnection(getUsername(),
                                      getPassword(),
                                      frameHandler,
                                      executor,
                                      getVirtualHost(),
                                      getClientProperties(),
                                      getRequestedFrameMax(),
                                      getRequestedChannelMax(),
                                      getRequestedHeartbeat(),
                                      getSaslConfig(), new BericoExceptionHandler());
                conn.start();
                return conn;
            } catch (IOException e) {
            	logger.error("Error creating connection for addr: " + addr, e);
                lastException = e;
            }
        }

        throw (lastException != null) ? lastException
                                      : new IOException("failed to connect");
    }

}
