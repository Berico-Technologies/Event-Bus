package com.berico.tweetstream.app;

import java.util.ArrayList;
import java.util.List;

import com.berico.tweetstream.ConsoleOutTweetHandler;
import com.berico.tweetstream.ModelAdaptors;
import com.berico.tweetstream.TwitterStreamMode;
import com.berico.tweetstream.TwitterStreamMode.StreamState;
import com.berico.tweetstream.handlers.MentionedUserCounterTweetHandler;
import com.berico.tweetstream.handlers.NaiveTopicCounterTweetHandler;
import com.berico.tweetstream.handlers.TopicMatchAggregate;
import com.berico.tweetstream.handlers.UserCounterTweetHandler;
import com.berico.tweetstream.handlers.WordCounterTweetHandler;
import com.berico.tweetstream.publishers.WordCountPublisher;
import com.berico.tweetstream.retweet.TopRetweetHandler;
import com.berico.tweetstream.wordcount.ConcurrentMapWordCountRepository;
import com.berico.tweetstream.wordcount.StopFilterWordSplitter;
import com.berico.tweetstream.wordcount.WordCountRepository;

import pegasus.eventbus.client.EventManager;
import twitter4j.FilterQuery;
import twitter4j.Status;
import twitter4j.TwitterStream;

public class TwitterFilterDemo extends BaseDemo {

	//Keywords to Filter on
	protected static String[] keywordFilters = "China,Xilai,Lashkar-e-Tayyibba,Lashkar-e-Taibba,Lashkar,Tayyibba,Taibba,LeT,Kashmir,Bhartiya,Janata,Iran,Pakistan,ISS,Taliban".split(",");
	
	//Locations to Filter on
	protected static double[][] locations = new double[][]{ 
			new double[]{ 67.236328, 7.71099 }, 
			new double[]{ 92.548828, 32.990236 }
	};
	
	@Override
	protected Object createEventBusRepresentation(Status status) {
		
		return ModelAdaptors.fromStatus(status);
	}

	@Override
	protected void wireUpBus(EventManager eventManager) {
		
	}

	@Override
	protected void startUpTwitter(TwitterStream twitterStream) {
		
    	FilterQuery fq = new FilterQuery(0, new long[]{}, keywordFilters);
		
    	fq.locations(locations);
    	
    	//Initialize the stream, supplying the filter
        twitterStream.filter(fq);
	}

	@Override
	protected TwitterStreamMode getTwitterStreamMode() {
		
		return new TwitterStreamMode(StreamState.Live, keywordFilters, locations);
	}
	
}
