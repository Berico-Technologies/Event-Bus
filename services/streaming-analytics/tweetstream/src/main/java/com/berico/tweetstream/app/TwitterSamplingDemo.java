package com.berico.tweetstream.app;

import com.berico.tweetstream.ModelAdaptors;

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
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void startUpTwitter(TwitterStream twitterStream) {
		// TODO Auto-generated method stub
		
	}

	
}
