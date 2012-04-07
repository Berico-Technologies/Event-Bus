package com.berico.tweetstream.geo;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.berico.tweetstream.Location;

public interface LocationRepository {

	public void addLocation(LocationMention mention);
	
	public List<LocationMention> getAllLocations();
	
	public Map<Location, Long> getTopNUserLocations(int N);
	public Map<Location, Long> getTopNMentionedLocations(int N);
	public Map<String, List<Location>> getUserLocationsByCountry();
	public Map<String, List<Location>> getMentionedLocationsByCountry();

	Collection<CountryCount> getTopNUserCountryCodes(int N);
	Collection<CountryCount> getTopNMentionedCountryCodes(int N);
	
	//topN Messages
	//topN User
	//all userLocations by country
	//all messageLocations by country	

}
