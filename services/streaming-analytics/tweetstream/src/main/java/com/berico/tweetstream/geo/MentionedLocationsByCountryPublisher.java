package com.berico.tweetstream.geo;

import java.util.List;
import java.util.Map;

import pegasus.eventbus.client.EventManager;

import com.berico.tweetstream.Location;
import com.berico.tweetstream.publishers.IntervalPublisher;


public class MentionedLocationsByCountryPublisher extends IntervalPublisher<LocationsByCountry> {
	
	public int countryCount = 25;
	
	private final LocationRepository locationRepository;
	
	
	public MentionedLocationsByCountryPublisher(EventManager em, LocationRepository countryCountRepo) {
		
		super(em);
		
		this.locationRepository = countryCountRepo;
	}
	
	public MentionedLocationsByCountryPublisher(EventManager em, LocationRepository countryCountRepo, int countryCount, long sleepInterval) {

		super(em, sleepInterval);
		
		this.locationRepository = countryCountRepo;
		this.countryCount = countryCount;
	}
	
	public void setWordCount(int wordCount) {
		this.countryCount = wordCount;
	}

	@Override
	protected LocationsByCountry nextEvent() {
		
		Map<String, List<Location>> topN = this.locationRepository.getMentionedLocationsByCountry();

		System.out.println(String.format("Mentioned Locations By\n%s", topN));
		
		return new LocationsByCountry(topN, "message");
	}
	
}