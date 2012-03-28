package com.berico.tweetstream.app;

import com.berico.tweetstream.ConsoleOutTweetHandler;
import com.berico.tweetstream.ModelAdaptors;
import com.berico.tweetstream.handlers.MentionedUserCounterTweetHandler;
import com.berico.tweetstream.handlers.UserCounterTweetHandler;
import com.berico.tweetstream.handlers.WordCounterTweetHandler;
import com.berico.tweetstream.wordcount.ConcurrentMapWordCountRepository;
import com.berico.tweetstream.wordcount.StopFilterWordSplitter;
import com.berico.tweetstream.wordcount.WordCountPublisher;
import com.berico.tweetstream.wordcount.WordCountRepository;

import pegasus.eventbus.client.EventManager;
import twitter4j.Status;
import twitter4j.TwitterStream;

public class TwitterSamplingDemo extends BaseDemo {

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
		
	}

	@Override
	protected void startUpTwitter(TwitterStream twitterStream) {
		
		twitterStream.sample();
	}

	
}
