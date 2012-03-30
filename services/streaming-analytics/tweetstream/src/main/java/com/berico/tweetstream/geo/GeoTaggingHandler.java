package com.berico.tweetstream.geo;

import java.util.ArrayList;
import java.util.List;

import pegasus.eventbus.client.EventHandler;
import pegasus.eventbus.client.EventResult;

import com.berico.clavin.dodiis.TwitterResolver;
import com.berico.clavin.geonameresolver.data.ResolvedGeoLocation;
import com.berico.tweetstream.Location;
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

		//List<String> countryCodesFromMeassge = resolver.resolveTweetCountryCodes(tweet.getMessage());
		String userCountry = resolver.resolveUserCountryCode(tweet.getUser().getLocation());
		
		
		List<LocationMention> geoLocs = new ArrayList<LocationMention>();
		
		for(ResolvedGeoLocation location : resolver.resolveTweetLocations(tweet.getMessage())){
			Location loc = buildLocation(location);
			this.countryCountRepository.increment(new LocationMention(loc, "message"));
		}
		
		ResolvedGeoLocation location = resolver.resolveUserLocation(tweet.getMessage());
		if(location != null){
			Location loc = buildLocation(location);
			this.countryCountRepository.increment(new LocationMention(loc, "user"));
		}
		
		
		return EventResult.Handled;
	}
	
	private Location buildLocation(ResolvedGeoLocation location){
		String country = location.feature.primaryCountryCode;
		String countryCode = location.feature.primaryCountryCode;
		String fullname = location.feature.displayName;
		double latitude = location.feature.latLon.lat;
		double longitude = location.feature.latLon.lat;
		
		
		Location loc = new Location();
		loc.setCountry(country);
		loc.setCountryCode(countryCode);
		loc.setFullname(fullname);
		loc.setLatitude(latitude);
		loc.setLongitude(longitude);
		
		return loc;
	}
}
