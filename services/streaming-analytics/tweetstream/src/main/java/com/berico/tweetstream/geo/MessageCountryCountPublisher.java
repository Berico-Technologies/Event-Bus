package com.berico.tweetstream.geo;

import java.util.Collections;
import java.util.Map;

import pegasus.eventbus.client.EventManager;

import com.berico.tweetstream.Location;
import com.berico.tweetstream.publishers.IntervalPublisher;


public class MessageCountryCountPublisher extends IntervalPublisher<TopNCountries> {
	
	public int countryCount = 25;
	
	private final CountryCountRepository countryCountRepo;
	
	
	public MessageCountryCountPublisher(EventManager em, CountryCountRepository countryCountRepo) {
		
		super(em);
		
		this.countryCountRepo = countryCountRepo;
	}
	
	public MessageCountryCountPublisher(EventManager em, CountryCountRepository countryCountRepo, String countryCountTopic, int countryCount, long sleepInterval) {

		super(em, sleepInterval);
		
		this.countryCountRepo = countryCountRepo;
		this.countryCount = countryCount;
	}
	
	public void setWordCount(int wordCount) {
		this.countryCount = wordCount;
	}

	@Override
	protected TopNCountries nextEvent() {
		
		Map<String, Long> topN = this.countryCountRepo.getTopNCountriesForSource(countryCount, "message");
		System.out.println(String.format("TOP %d Mesaage USER COUNTRIES:\n%s", countryCount, topN));
		return new TopNCountries(topN, "message");
	}
	
}