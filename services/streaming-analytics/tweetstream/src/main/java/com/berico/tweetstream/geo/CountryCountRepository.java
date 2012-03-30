package com.berico.tweetstream.geo;

import java.util.Map;

public interface CountryCountRepository {

	public void increment(String countryCode);

	public long getCount(String countryCode);

	public Map<String, Long> getTopNCountries(int N);

}
