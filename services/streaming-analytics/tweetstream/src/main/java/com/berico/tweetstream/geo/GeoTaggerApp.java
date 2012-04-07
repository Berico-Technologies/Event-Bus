package com.berico.tweetstream.geo;

import pegasus.eventbus.amqp.AmqpConfiguration;
import pegasus.eventbus.amqp.AmqpConnectionParameters;
import pegasus.eventbus.amqp.AmqpEventManager;
import pegasus.eventbus.client.EventManager;

public class GeoTaggerApp {

	public static void main(String... args){
		
		String connection = "amqp://guest:guest@localhost:5672/";
		
		boolean queueMode = false;
		
		if(args.length > 0 && args[0].startsWith("amqp://")){
			connection = args[0];
		}
		
		if(args.length > 0 && !args[0].startsWith("amqp://")){
			queueMode = true;
		}
		
		if(args.length > 1){
			queueMode = true;
		}
		
		//Manually configure the EventManager
    	AmqpConfiguration config = AmqpConfiguration.getDefault(
				"geotagger", 
				new AmqpConnectionParameters(
					connection));
    	
    	//Initialize the EventManager
    	EventManager em = new AmqpEventManager(config);
    	
    	//Start the EventManager
    	em.start();
    	
    	LocationRepository repo = new LocationRepositoryImpl();
    	
    	//If we are in queue mode (wanting multiple handlers)
    	if(queueMode){
    		em.subscribe(new GeoTaggingHandler(repo), "twitter_geo_requests");
    	//Otherwise, skip the overhead of persisting messages
    	} else {
    		em.subscribe(new GeoTaggingHandler(repo));
    	}
    	
    	new UserCountryCountPublisher(em, repo, 15000l).start();
    	new UserCountryCountPublisher(em, repo, 200, 8000, "all.user.locations").start();
    	new MentionedCountryCountPublisher(em, repo, 19000).start();
    	new MentionedCountryCountPublisher(em, repo, 200, 8000, "all.mention.locations").start();
    	//new UserLocationsByCountryPublisher(em, repo).start();
    	//new MentionedLocationsByCountryPublisher(em, repo).start();
	}
}
