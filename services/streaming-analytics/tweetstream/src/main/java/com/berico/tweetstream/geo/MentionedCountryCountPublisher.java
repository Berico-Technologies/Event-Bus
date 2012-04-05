package com.berico.tweetstream.geo;

import java.util.Map;

import pegasus.eventbus.client.EventManager;

import com.berico.tweetstream.publishers.IntervalPublisher;


public class MentionedCountryCountPublisher extends IntervalPublisher<TopNCountries> {
	
	public int countryCount = 25;
	
	private final LocationRepository locationRepository;
	
	
	public MentionedCountryCountPublisher(EventManager em, LocationRepository countryCountRepo) {
		
		super(em);
		
		this.locationRepository = countryCountRepo;
	}
	
	public MentionedCountryCountPublisher(EventManager em, LocationRepository countryCountRepo, int countryCount, long sleepInterval) {

		super(em, sleepInterval);
		
		this.locationRepository = countryCountRepo;
		this.countryCount = countryCount;
	}
	
	public void setWordCount(int wordCount) {
		this.countryCount = wordCount;
	}

	@Override
	protected TopNCountries nextEvent() {
		
		Map<String, Long> topN = this.locationRepository.getTopNMentionedCountryCodes(countryCount);

		System.out.println(String.format("Top %d Mentioned Countries\n%s", this.countryCount, topN));
		
		return new TopNCountries(topN, "All");
	}
	
}