package com.berico.tweetstream.retweet;

import com.berico.tweetstream.Tweet;

import pegasus.eventbus.client.EventHandler;
import pegasus.eventbus.client.EventManager;
import pegasus.eventbus.client.EventResult;

public class TopRetweetHandler implements EventHandler<Tweet> {

	private TopRetweets topRetweets = new TopRetweets();
	
	private EventManager eventManager = null;
	
	public TopRetweetHandler(EventManager eventManager) {
	
		this.eventManager = eventManager;
	}

	@SuppressWarnings("unchecked")
	public Class<? extends Tweet>[] getHandledEventTypes() {
		
		return new Class[]{ Tweet.class };
	}

	public EventResult handleEvent(Tweet tweet) {
		
		if(topRetweets.isTopRetweet(tweet)){
			
			this.eventManager.publish(topRetweets);
		}
		
		return EventResult.Handled;
	}

}
