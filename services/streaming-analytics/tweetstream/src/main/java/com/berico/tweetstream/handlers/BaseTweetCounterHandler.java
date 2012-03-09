package com.berico.tweetstream.handlers;

import com.berico.tweetstream.Tweet;
import com.berico.tweetstream.wordcount.WordCountRepository;

import pegasus.eventbus.client.EventHandler;
import pegasus.eventbus.client.EventResult;

public abstract class BaseTweetCounterHandler implements EventHandler<Tweet> {

	WordCountRepository wordCountRepository = null;
	
	public BaseTweetCounterHandler(WordCountRepository userCounter){

		this.wordCountRepository = userCounter;
	}
	
	public abstract String[] provideWordsToCount(Tweet tweet);
	
	
	@SuppressWarnings("unchecked")
	public Class<? extends Tweet>[] getHandledEventTypes() {
		return new Class[]{ Tweet.class };
	}

	public EventResult handleEvent(Tweet tweet) {
		
		for(String word : provideWordsToCount(tweet)){
			
			this.wordCountRepository.increment(word);
		}

		return EventResult.Handled;
	}

}
