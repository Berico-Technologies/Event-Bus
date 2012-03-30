package com.berico.tweetstream.geo;

import java.util.List;

import pegasus.eventbus.client.EventHandler;
import pegasus.eventbus.client.EventResult;

import com.berico.clavin.dodiis.TwitterResolver;
import com.berico.tweetstream.Tweet;

public class GeoTaggingHandler implements EventHandler<Tweet> {

	TwitterResolver resolver;
	CountryCountRepository countryCountRepository;
	

	
	public GeoTaggingHandler(CountryCountRepository counter){
		resolver = new TwitterResolver();
		this.countryCountRepository = counter;
	}
	

	
	@SuppressWarnings("unchecked")
	public Class<? extends Tweet>[] getHandledEventTypes() {
		return new Class[]{ Tweet.class };
	}

	public EventResult handleEvent(Tweet tweet) {
		
		List<String> countryCodes = resolver.resolveTweetCountryCodes(tweet.getMessage());
		String userCountry = resolver.resolveUserCountryCode(tweet.getUser().getLocation());
		if(userCountry != null){
			countryCodes.add(userCountry);
		}
		for(String country : countryCodes){
			
			this.countryCountRepository.increment(country);
		}

		return EventResult.Handled;
	}
}
