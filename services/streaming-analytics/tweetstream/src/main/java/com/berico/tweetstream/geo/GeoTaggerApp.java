package com.berico.tweetstream.geo;

import pegasus.eventbus.amqp.AmqpConfiguration;
import pegasus.eventbus.amqp.AmqpConnectionParameters;
import pegasus.eventbus.amqp.AmqpEventManager;
import pegasus.eventbus.client.EventManager;

public class GeoTaggerApp {

	public static void main(String... args){
		//Manually configure the EventManager
    	AmqpConfiguration config = AmqpConfiguration.getDefault(
				"tweetstream", 
				new AmqpConnectionParameters(
					"amqp://guest:guest@localhost:5672/"));
    	
    	//Initialize the EventManager
    	EventManager em = new AmqpEventManager(config);
    	
    	//Start the EventManager
    	em.start();
    	
    	CountryCountRepository repo = new ConcurrentMapCountryCountRepository();
    	em.subscribe(new GeoTaggingHandler(repo));
    	new CountryCountPublisher(em, repo).start();
    	new UserCountryCountPublisher(em, repo).start();
    	new MessageCountryCountPublisher(em, repo).start();
	}
}
