package com.berico.tweetstream.geo;

import java.util.Map;

import com.berico.tweetstream.Location;

public interface CountryCountRepository {

	public void increment(LocationMention geoLoc);

	public long getCount(String countryCode);
	
	public long getCountWithSourceField(String countryCode, String sourceField);

	public Map<String, Long> getTopNCountries(int N);
	
	public Map<String, Long> getTopNCountriesForSource(int N, String source);

}
