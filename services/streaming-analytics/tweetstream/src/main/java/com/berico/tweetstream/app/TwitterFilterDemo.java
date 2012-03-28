package com.berico.tweetstream.app;

import java.util.ArrayList;
import java.util.List;

import com.berico.tweetstream.ConsoleOutTweetHandler;
import com.berico.tweetstream.ModelAdaptors;
import com.berico.tweetstream.handlers.MentionedUserCounterTweetHandler;
import com.berico.tweetstream.handlers.NaiveTopicCounterTweetHandler;
import com.berico.tweetstream.handlers.TopicMatchAggregate;
import com.berico.tweetstream.handlers.UserCounterTweetHandler;
import com.berico.tweetstream.handlers.WordCounterTweetHandler;
import com.berico.tweetstream.wordcount.ConcurrentMapWordCountRepository;
import com.berico.tweetstream.wordcount.StopFilterWordSplitter;
import com.berico.tweetstream.wordcount.WordCountPublisher;
import com.berico.tweetstream.wordcount.WordCountRepository;

import pegasus.eventbus.client.EventManager;
import twitter4j.FilterQuery;
import twitter4j.Status;
import twitter4j.TwitterStream;

public class TwitterFilterDemo extends BaseDemo {

	@Override
	protected Object createEventBusRepresentation(Status status) {
		
		return ModelAdaptors.fromStatus(status);
	}

	@Override
	protected void wireUpBus(EventManager eventManager) {
		
		//Subscribe one of our own EventHandlers to display
        //Tweets on the bus from this console.
        this.eventManager.subscribe(new ConsoleOutTweetHandler());
        
        WordCountRepository tweetWordsCount = new ConcurrentMapWordCountRepository();
        this.eventManager.subscribe(new WordCounterTweetHandler(tweetWordsCount, new StopFilterWordSplitter()));
        new WordCountPublisher(this.eventManager, tweetWordsCount, "tweet.words").start();
        
        WordCountRepository userCount = new ConcurrentMapWordCountRepository();
        this.eventManager.subscribe(new UserCounterTweetHandler(userCount));
        new WordCountPublisher(this.eventManager, userCount, "tweet.users").start();
        
        WordCountRepository mentionedCount = new ConcurrentMapWordCountRepository();
        this.eventManager.subscribe(new MentionedUserCounterTweetHandler(mentionedCount));
        new WordCountPublisher(this.eventManager, mentionedCount, "tweet.mentioned").start();
        
        List<TopicMatchAggregate> aggregates = new ArrayList<TopicMatchAggregate>();
        
        aggregates.add(new TopicMatchAggregate("", new String[]{}, 3));
        aggregates.add(new TopicMatchAggregate("", new String[]{}, 3));
        aggregates.add(new TopicMatchAggregate("", new String[]{}, 3));
        aggregates.add(new TopicMatchAggregate("", new String[]{}, 3));
        aggregates.add(new TopicMatchAggregate("", new String[]{}, 3));
        aggregates.add(new TopicMatchAggregate("", new String[]{}, 3));
        aggregates.add(new TopicMatchAggregate("", new String[]{}, 3));
        aggregates.add(new TopicMatchAggregate("", new String[]{}, 3));
        aggregates.add(new TopicMatchAggregate("", new String[]{}, 3));
        aggregates.add(new TopicMatchAggregate("", new String[]{}, 3));
        
        NaiveTopicCounterTweetHandler naiveTopicCounter = new NaiveTopicCounterTweetHandler(this.eventManager, aggregates, new StopFilterWordSplitter());
	}

	@Override
	protected void startUpTwitter(TwitterStream twitterStream) {
		
		//Keywords to Filter on
    	String[] filters = "China,Xilai,Lashkar-e-Tayyibba,Lashkar-e-Taibba,Lashkar,Tayyibba,Taibba,LeT,Kashmir,Bhartiya,Janata,Iran,Pakistan,ISS,Taliban".split(",");
    	
    	//Locations to Filter on
    	double[][] locations = new double[][]{ 
    			new double[]{ 67.236328, 7.71099 }, 
    			new double[]{ 92.548828, 32.990236 }
    	};
		
    	//Initialize the stream, supplying the filter
        twitterStream.filter(
        	new FilterQuery(0, new long[]{}, filters));
	}
	
}
