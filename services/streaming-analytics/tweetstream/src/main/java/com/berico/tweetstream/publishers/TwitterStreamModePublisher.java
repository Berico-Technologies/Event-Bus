package com.berico.tweetstream.publishers;

import pegasus.eventbus.client.EventManager;

import com.berico.tweetstream.TwitterStreamMode;

public class TwitterStreamModePublisher extends IntervalPublisher<TwitterStreamMode> {

	protected TwitterStreamMode tsm;
	
	public TwitterStreamModePublisher(TwitterStreamMode tsm, EventManager em) {
		super(em);
		this.tsm = tsm;
	}
	
	public TwitterStreamModePublisher(TwitterStreamMode tsm, EventManager em, long interval) {
		super(em, interval);
		this.tsm = tsm;
	}

	@Override
	protected TwitterStreamMode nextEvent() {
		System.out.println("Publishing TwitterStreamMode");
		return this.tsm;
	}

}
