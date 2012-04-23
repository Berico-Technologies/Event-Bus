package com.berico.tweetstream.app;

import com.berico.tweetstream.TwitterStreamMode;
import com.berico.tweetstream.publishers.TwitterStreamModePublisher;

import pegasus.eventbus.amqp.AmqpConfiguration;
import pegasus.eventbus.amqp.AmqpConnectionParameters;
import pegasus.eventbus.amqp.AmqpEventManager;
import pegasus.eventbus.client.EventManager;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;


public abstract class BaseDemo {

	protected String[] cmdlineArgs = null;
	
	protected EventManager eventManager = null;
	
	protected TwitterStream twitterStream = null;
	
	public BaseDemo(){}
	
	public void initialize(String[] args){
		
		String connectionString =  "amqp://guest:guest@localhost:5672/";
		
		if(args.length > 0 && args[0].startsWith("amqp://")){
			
			connectionString = args[0];
			
			this.cmdlineArgs = pruneFirst(args);
		}
		else {
			
			this.cmdlineArgs = args;
		}
						
		//Manually configure the EventManager
    	AmqpConfiguration config = AmqpConfiguration.getDefault("tweetstream", new AmqpConnectionParameters(connectionString));
    	
    	//Initialize the EventManager
    	this.eventManager = new AmqpEventManager(config);
    	
    	//Start the EventManager
    	this.eventManager.start();
    	
    	//Create a Twitter Stream instance (3rd Party API)
    	this.twitterStream = new TwitterStreamFactory().getInstance();
    	
    	
    	//Create a new instance of our Twitter listener that will
    	//publish incoming Tweets onto the bus.
    	TweetPublisher publishOnBusListener = new TweetPublisher(this.eventManager);
    	
    	//Register our listener with the Twitter Stream API
        twitterStream.addListener(publishOnBusListener);
        
        //Start publishing the TwitterStreamMode
        new TwitterStreamModePublisher(this.getTwitterStreamMode(), this.eventManager).start();
        
        wireUpBus(this.eventManager);
        
        startUpTwitter(twitterStream);
	}
	
	protected abstract Object createEventBusRepresentation(Status status);
	
	protected abstract void wireUpBus(EventManager eventManager);
	
	protected abstract void startUpTwitter(TwitterStream twitterStream);
	
	protected abstract TwitterStreamMode getTwitterStreamMode();
	
	public static void main(String[] args){
	
    	try {
			
    		BaseDemo demo = (BaseDemo)(Class.forName(args[0]).newInstance());
    		
			demo.initialize(pruneFirst(args));
			
		} catch (Exception e) {

			e.printStackTrace();
		}
	    
	}
	
    public class TweetPublisher implements StatusListener {
    	
    	EventManager em = null;
    	
    	public TweetPublisher(EventManager em){
    		
    		this.em = em;	
    	}
    	
        public void onStatus(Status status) {
        	
            em.publish(createEventBusRepresentation(status));
        }

        public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {}

        public void onTrackLimitationNotice(int numberOfLimitedStatuses) {}

        public void onScrubGeo(long userId, long upToStatusId) {}

        public void onException(Exception ex) { ex.printStackTrace(); }
    }
    
    public static String[] pruneFirst(String[] args){
    	String[] newArgs = new String[args.length - 1];
    	System.arraycopy(args, 1, newArgs, 0, args.length - 1);
    	return newArgs;
    }
	
}
