package com.berico.tweetstream;

import com.berico.tweetstream.wordcount.ConcurrentMapWordCountRepository;
import com.berico.tweetstream.wordcount.StopFilterWordSplitter;
import com.berico.tweetstream.wordcount.WordCountPublisher;
import com.berico.tweetstream.wordcount.WordCountRepository;
import com.berico.tweetstream.wordcount.WordCounterTweetHandler;

import pegasus.eventbus.amqp.AmqpConfiguration;
import pegasus.eventbus.amqp.AmqpEventManager;
import pegasus.eventbus.amqp.ConnectionParameters;
import pegasus.eventbus.client.EventManager;
import twitter4j.FilterQuery;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;

/**
 * A simple example of how to adapt communication from one source
 * (in this case Twitter) to the Event Bus.
 * @author Richard Clayton (Berico Technologies)
 */
public class TweetStreamApp 
{
    public static void main( String[] args )
    {	
    	//Manually configure the EventManager
    	AmqpConfiguration config = AmqpConfiguration.getDefault(
				"tweetstream", 
				new ConnectionParameters(
					"amqp://guest:guest@localhost:5672/"));
    	
    	//Initialize the EventManager
    	EventManager em = new AmqpEventManager(config);
    	
    	//Start the EventManager
    	em.start();
    	
    	//Create a new instance of our Twitter listener that will
    	//publish incoming Tweets onto the bus.
    	TweetPublisher publishOnBusListener = new TweetPublisher(em);
    	
    	//Create a Twitter Stream instance (3rd Party API)
    	TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
    	
    	//Register our listener with the Twitter Stream API
        twitterStream.addListener(publishOnBusListener);
        
        //Subscribe one of our own EventHandlers to display
        //Tweets on the bus from this console.
        //em.subscribe(new ConsoleOutTweetHandler());
        
        WordCountRepository wcr = new ConcurrentMapWordCountRepository();
        
        
        
        em.subscribe(new WordCounterTweetHandler(wcr, new StopFilterWordSplitter()));
        
        new WordCountPublisher(em, wcr).start();
        
        //Initialize the stream, supplying the filter
        twitterStream.filter(
        	new FilterQuery(0, new long[]{}, 
        		new String[]{ "Al Shabaab", "Somolia", "Bin Laden", "Al Qaeda", "Africa", "Iraq", "Afghanistan", "Iran" }));
    }
    
    /**
     * Places Tweets coming from the Twitter Stream onto the Event Bus.
     * @author Richard Clayton (Berico Technologies)
     */
    public static class TweetPublisher implements StatusListener {
    	
    	EventManager em = null;
    	
    	/**
    	 * Initialize the handler.
    	 * @param em Event Manager
    	 */
    	public TweetPublisher(EventManager em){
    		
    		this.em = em;	
    	}
    	
    	/**
    	 * Called when a Tweet is received by the client.
    	 * @param status Context of the Tweet
    	 */
        public void onStatus(Status status) {
        	
        	//Publish the adapted Tweet on the bus
            em.publish(
            	//We adapt the Twitter4j Status object
            	//to our own Model.
            	Tweet.fromStatus(status));
        }

        public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {}

        public void onTrackLimitationNotice(int numberOfLimitedStatuses) {}

        public void onScrubGeo(long userId, long upToStatusId) {}

        public void onException(Exception ex) { ex.printStackTrace(); }
    }
}
