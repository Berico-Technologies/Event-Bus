package com.berico.tweetstream.geo;


import pegasus.eventbus.client.EventHandler;
import pegasus.eventbus.client.EventResult;

import com.berico.clavin.dodiis.TwitterResolver;
import com.berico.clavin.geonameresolver.data.ResolvedGeoLocation;
import com.berico.tweetstream.Location;
import com.berico.tweetstream.Tweet;

public class GeoTaggingHandler implements EventHandler<Tweet> {

	TwitterResolver resolver;
	LocationRepository locationRepository;
	

	
	public GeoTaggingHandler(LocationRepository locationRepository){
		resolver = new TwitterResolver();
		this.locationRepository = locationRepository;
	}
	

	
	@SuppressWarnings("unchecked")
	public Class<? extends Tweet>[] getHandledEventTypes() {
		return new Class[]{ Tweet.class };
	}

	public EventResult handleEvent(Tweet tweet) {

		
		for(ResolvedGeoLocation location : resolver.resolveTweetLocations(tweet.getMessage())){
			Location loc = buildLocation(location);
			this.locationRepository.addLocation(new LocationMention(loc, "message"));
		}
		
		ResolvedGeoLocation location = resolver.resolveUserLocation(tweet.getUser().getLocation());
		if(location != null){
			Location loc = buildLocation(location);
			this.locationRepository.addLocation(new LocationMention(loc, "user"));
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
