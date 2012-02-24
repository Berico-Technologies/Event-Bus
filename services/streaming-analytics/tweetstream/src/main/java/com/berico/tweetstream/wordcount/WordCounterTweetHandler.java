package com.berico.tweetstream.wordcount;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.berico.tweetstream.Tweet;

import pegasus.eventbus.client.EventHandler;
import pegasus.eventbus.client.EventResult;

public class WordCounterTweetHandler implements EventHandler<Tweet> {

	private static final Logger LOG = LoggerFactory.getLogger(WordCounterTweetHandler.class);
	
	WordCountRepository wordCountRepo = null;
	
	WordSplitter wordSplitter = null;
	
	public WordCounterTweetHandler(WordCountRepository repo, WordSplitter splitter){
		this.wordCountRepo = repo;
		this.wordSplitter = splitter;
	}
	
	@SuppressWarnings("unchecked")
	public Class<? extends Tweet>[] getHandledEventTypes() {
		return new Class[]{ Tweet.class };
	}

	
	public EventResult handleEvent(Tweet tweet) {
		
		LOG.trace("Tweet Received.");
		
		String[] words = this.wordSplitter.split(tweet.getMessage());
		
		for(String word : words){
			
			wordCountRepo.increment(word);
		}
		
		return EventResult.Handled;
	}
}
