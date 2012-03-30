package com.berico.tweetstream.geo;

import java.util.Collections;
import java.util.Map;

import pegasus.eventbus.client.EventManager;

import com.berico.tweetstream.Location;
import com.berico.tweetstream.publishers.IntervalPublisher;


public class CountryCountPublisher extends IntervalPublisher<TopNCountries> {
	
	public int countryCount = 25;
	
	private final CountryCountRepository countryCountRepo;
	
	
	public CountryCountPublisher(EventManager em, CountryCountRepository countryCountRepo) {
		
		super(em);
		
		this.countryCountRepo = countryCountRepo;
	}
	
	public CountryCountPublisher(EventManager em, CountryCountRepository countryCountRepo, int countryCount, long sleepInterval) {

		super(em, sleepInterval);
		
		this.countryCountRepo = countryCountRepo;
		this.countryCount = countryCount;
	}
	
	public void setWordCount(int wordCount) {
		this.countryCount = wordCount;
	}

	@Override
	protected TopNCountries nextEvent() {
		
		Map<String, Long> topN = this.countryCountRepo.getTopNCountries(countryCount);

		System.out.println(String.format("TOP %d COUNTRIES:\n%s", countryCount, topN));
		return new TopNCountries(topN, "All");
	}
	
}