package com.berico.tweetstream.app;

import java.util.ArrayList;
import java.util.List;

import com.berico.tweetstream.ConsoleOutTweetHandler;
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

import pegasus.eventbus.amqp.AmqpConfiguration;
import pegasus.eventbus.amqp.AmqpConnectionParameters;
import pegasus.eventbus.amqp.AmqpEventManager;
import pegasus.eventbus.client.EventManager;

public class AnalyticServices {

	EventManager eventManager = null;
	
	String[] keywordFilters = null;
	
	public AnalyticServices(EventManager eventManager){
		
		this(eventManager, new String[]{});
	}
	
	public AnalyticServices(EventManager eventManager, String[] keywordFilters){
		
		this.eventManager = eventManager;
		
		this.keywordFilters = keywordFilters;
	}
	
	public void initialize(){
		
		//Subscribe one of our own EventHandlers to display
        //Tweets on the bus from this console.
        this.eventManager.subscribe(new ConsoleOutTweetHandler());
        
        WordCountRepository tweetWordsCount = new ConcurrentMapWordCountRepository();
        this.eventManager.subscribe(new WordCounterTweetHandler(tweetWordsCount, new StopFilterWordSplitter(keywordFilters)));
        new WordCountPublisher(this.eventManager, tweetWordsCount, "tweet.words", 12000l).start();
        
        WordCountRepository userCount = new ConcurrentMapWordCountRepository();
        this.eventManager.subscribe(new UserCounterTweetHandler(userCount));
        new WordCountPublisher(this.eventManager, userCount, "tweet.users", 16000l).start();
        
        WordCountRepository mentionedCount = new ConcurrentMapWordCountRepository();
        this.eventManager.subscribe(new MentionedUserCounterTweetHandler(mentionedCount));
        new WordCountPublisher(this.eventManager, mentionedCount, "tweet.mentioned", 18000l).start();
        
        this.eventManager.subscribe(new TopRetweetHandler(this.eventManager));
        
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
	
	public static void main(String[]  args){
		
		String connectionString =  "amqp://guest:guest@localhost:5672/";
		String[] keywords = "China,Xilai,Lashkar-e-Tayyibba,Lashkar-e-Taibba,Lashkar,Tayyibba,Taibba,LeT,Kashmir,Bhartiya,Janata,Iran,Pakistan,ISS,Taliban".split(",");
		
		if(args.length > 0 && args[0].startsWith("amqp://")){
			
			connectionString = args[0];
		}
		
		if(args.length > 2){
			
			keywords = args[1].split(",");
		}
		
		//Manually configure the EventManager
    	AmqpConfiguration config = AmqpConfiguration.getDefault("tweetstream", new AmqpConnectionParameters(connectionString));
    	
    	//Initialize the EventManager
    	EventManager eventManager = new AmqpEventManager(config);
    	
    	//Start the EventManager
    	eventManager.start();
    	
    	new AnalyticServices(eventManager, keywords).initialize();
	}
}
