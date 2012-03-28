package com.berico.tweetstream.handlers;

import java.util.List;

import com.berico.tweetstream.Tweet;
import com.berico.tweetstream.wordcount.WordSplitter;

import pegasus.eventbus.client.EventHandler;
import pegasus.eventbus.client.EventResult;

public class NaiveTopicCounterTweetHandler implements EventHandler<Tweet> {

	WordSplitter wordSplitter = null;
	List<TopicMatchAggregate> topicMatchAggregates = null;
	
	public NaiveTopicCounterTweetHandler(List<TopicMatchAggregate> topicMatchAggregates, WordSplitter splitter) {
	
		this.topicMatchAggregates = topicMatchAggregates;
		
		this.wordSplitter = splitter;
	}

	@SuppressWarnings("unchecked")
	public Class<? extends Tweet>[] getHandledEventTypes() {
		
		return new Class[]{ Tweet.class };
	}

	public EventResult handleEvent(Tweet tweet) {
		
		String[] words = this.wordSplitter.split(tweet.getMessage());
		
		for(TopicMatchAggregate tma : this.topicMatchAggregates){
			
			tma.observe(words);
		}
		
		return EventResult.Handled;
	}
}
