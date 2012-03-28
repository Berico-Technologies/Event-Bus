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
		
		//Subscribe one of our own EventHandlers to display
        //Tweets on the bus from this console.
        this.eventManager.subscribe(new ConsoleOutTweetHandler());
        
        WordCountRepository tweetWordsCount = new ConcurrentMapWordCountRepository();
        this.eventManager.subscribe(new WordCounterTweetHandler(tweetWordsCount, new StopFilterWordSplitter(keywordFilters)));
        new WordCountPublisher(this.eventManager, tweetWordsCount, "tweet.words").start();
        
        WordCountRepository userCount = new ConcurrentMapWordCountRepository();
        this.eventManager.subscribe(new UserCounterTweetHandler(userCount));
        new WordCountPublisher(this.eventManager, userCount, "tweet.users").start();
        
        WordCountRepository mentionedCount = new ConcurrentMapWordCountRepository();
        this.eventManager.subscribe(new MentionedUserCounterTweetHandler(mentionedCount));
        new WordCountPublisher(this.eventManager, mentionedCount, "tweet.mentioned").start();
        
        List<TopicMatchAggregate> aggregates = new ArrayList<TopicMatchAggregate>();
        
        aggregates.add(new TopicMatchAggregate("Afghanistan, US Service Member Accused of Massacre", new String[]{ "bales", "afghanistan", "massacre", "civilians"}, 2));
        aggregates.add(new TopicMatchAggregate("Egypt Coptic Pope Laid to Rest", new String[]{ "egypt", "coptic", "pope", "shenouda", "funeral" }, 2));
        aggregates.add(new TopicMatchAggregate("Police Siege France", new String[]{ "police", "siege", "france", "merah", "qaeda", "french"}, 3));
        aggregates.add(new TopicMatchAggregate("Mexican Earthquake", new String[]{ "mexico", "earthquake", "ometepec", "7.4", "tremor", "quake" }, 3));
        aggregates.add(new TopicMatchAggregate("Iran-Israel Relations", new String[]{ "iran","israel" }, 2));
        aggregates.add(new TopicMatchAggregate("Mali Coup", new String[]{ "renegade","mali","coup","amadou","toumani","toure", "seizure" }, 3));
        aggregates.add(new TopicMatchAggregate("Syria Rebellion", new String[]{ "syria","violence","homs","rebellion","syrian", "al-assad" }, 3));
        aggregates.add(new TopicMatchAggregate("Somali Pirates Release Hostage", new String[]{ "somolia","somali","pirates","release","british", "judith", "tebbutt" }, 3));
        aggregates.add(new TopicMatchAggregate("Syria Sanctions & Peace Initiative", new String[]{ "syria","sanctions","un","u.n.","peace" }, 3));
        aggregates.add(new TopicMatchAggregate("Uganda - Joseph Kony", new String[]{ "joseph","kony","uganda","warlord","lra", "children", "child" }, 3));
        
        this.eventManager.subscribe(new NaiveTopicCounterTweetHandler(this.eventManager, aggregates, new StopFilterWordSplitter()));
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
