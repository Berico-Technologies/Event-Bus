package com.berico.tweetstream.app;

import pegasus.eventbus.client.EventManager;
import twitter4j.Status;
import twitter4j.TwitterStream;

import com.berico.tweetstream.ModelAdaptors;
import com.berico.tweetstream.TwitterStreamMode;

public class TwitterSamplingDemo extends BaseDemo {

	@Override
	protected Object createEventBusRepresentation(Status status) {
		
		return ModelAdaptors.fromStatus(status);
	}

	@Override
	protected void wireUpBus(EventManager eventManager) {
		
	}

	@Override
	protected void startUpTwitter(TwitterStream twitterStream) {
		
		twitterStream.sample();
	}

	@Override
	protected TwitterStreamMode getTwitterStreamMode() {
		
		return new TwitterStreamMode();
	}

}
