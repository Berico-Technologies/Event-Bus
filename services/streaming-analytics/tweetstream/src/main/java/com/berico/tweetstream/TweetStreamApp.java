/**
 *   ______     ______     ______     __     ______     ______    
 *  /\  == \   /\  ___\   /\  == \   /\ \   /\  ___\   /\  __ \   
 *  \ \  __<   \ \  __\   \ \  __<   \ \ \  \ \ \____  \ \ \/\ \  
 *   \ \_____\  \ \_____\  \ \_\ \_\  \ \_\  \ \_____\  \ \_____\ 
 *    \/_____/   \/_____/   \/_/ /_/   \/_/   \/_____/   \/_____/ 
 *   ______   ______     ______     __  __     __   __     ______     __         ______     ______     __     ______     ______    
 *  /\__  _\ /\  ___\   /\  ___\   /\ \_\ \   /\ "-.\ \   /\  __ \   /\ \       /\  __ \   /\  ___\   /\ \   /\  ___\   /\  ___\   
 *  \/_/\ \/ \ \  __\   \ \ \____  \ \  __ \  \ \ \-.  \  \ \ \/\ \  \ \ \____  \ \ \/\ \  \ \ \__ \  \ \ \  \ \  __\   \ \___  \  
 *     \ \_\  \ \_____\  \ \_____\  \ \_\ \_\  \ \_\\"\_\  \ \_____\  \ \_____\  \ \_____\  \ \_____\  \ \_\  \ \_____\  \/\_____\ 
 *      \/_/   \/_____/   \/_____/   \/_/\/_/   \/_/ \/_/   \/_____/   \/_____/   \/_____/   \/_____/   \/_/   \/_____/   \/_____/ 
 *                                                                                                                              
 *  All rights reserved - Feb. 2012
 *  Richard Clayton (rclayton@bericotechnologies.com)                                                                                                                
 */
package com.berico.tweetstream;

import pegasus.eventbus.amqp.AmqpConfiguration;
import pegasus.eventbus.amqp.AmqpConnectionParameters;
import pegasus.eventbus.amqp.AmqpEventManager;
import pegasus.eventbus.client.EventManager;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;

import com.berico.tweetstream.handlers.MentionedUserCounterTweetHandler;
import com.berico.tweetstream.handlers.UserCounterTweetHandler;
import com.berico.tweetstream.handlers.WordCounterTweetHandler;
import com.berico.tweetstream.publishers.WordCountPublisher;
import com.berico.tweetstream.wordcount.ConcurrentMapWordCountRepository;
import com.berico.tweetstream.wordcount.StopFilterWordSplitter;
import com.berico.tweetstream.wordcount.WordCountRepository;

/**
 * A simple example of how to adapt communication from one source
 * (in this case Twitter) to the Event Bus.
 * @author Richard Clayton (Berico Technologies)
 */
public class TweetStreamApp 
{
    public static void main( String[] args )
    {	
    	//Keywords to Filter on
    	String[] filters = "China,Xilai,Lashkar-e-Tayyibba,Lashkar-e-Taibba,Lashkar,Tayyibba,Taibba,LeT,Kashmir,Bhartiya,Janata,Iran,Pakistan,ISS,Taliban".split(",");
    	
    	//Locations to Filter on
    	double[][] locations = new double[][]{ 
    			new double[]{ 67.236328, 7.71099 }, 
    			new double[]{ 92.548828, 32.990236 }
    	};
    	
    	//Manually configure the EventManager
    	AmqpConfiguration config = AmqpConfiguration.getDefault(
				"tweetstream", 
				new AmqpConnectionParameters(
					"amqp://guest:guest@localhost:5672/"));
    	
    	//Initialize the EventManager
    	EventManager em = new AmqpEventManager(config);
    	
    	//Start the EventManager
    	em.start();
    	
    	//Create a new instance of our Twitter listener that will
    	//publish incoming Tweets onto the bus.
    	//TweetPublisher publishOnBusListener = new TweetPublisher(em);
    	
    	//Create a Twitter Stream instance (3rd Party API)
    	//TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
    	
    	//Register our listener with the Twitter Stream API
        //twitterStream.addListener(publishOnBusListener);
        
        //Subscribe one of our own EventHandlers to display
        //Tweets on the bus from this console.
        em.subscribe(new ConsoleOutTweetHandler());
        
        WordCountRepository tweetWordsCount = new ConcurrentMapWordCountRepository();
        em.subscribe(new WordCounterTweetHandler(tweetWordsCount, new StopFilterWordSplitter()));
        new WordCountPublisher(em, tweetWordsCount, "tweet.words").start();
        
        WordCountRepository userCount = new ConcurrentMapWordCountRepository();
        em.subscribe(new UserCounterTweetHandler(userCount));
        new WordCountPublisher(em, userCount, "tweet.users").start();
        
        WordCountRepository mentionedCount = new ConcurrentMapWordCountRepository();
        em.subscribe(new MentionedUserCounterTweetHandler(mentionedCount));
        new WordCountPublisher(em, mentionedCount, "tweet.mentioned").start();
        
        //Initialize the stream, supplying the filter
        //twitterStream.filter(
        //	new FilterQuery(0, new long[]{}, filters));
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
            	ModelAdaptors.fromStatus(status));
        }

        public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {}

        public void onTrackLimitationNotice(int numberOfLimitedStatuses) {}

        public void onScrubGeo(long userId, long upToStatusId) {}

        public void onException(Exception ex) { ex.printStackTrace(); }
    }
}
