package com.berico.tweetstream.geo;

import java.util.Collection;

import pegasus.eventbus.client.EventManager;

import com.berico.tweetstream.publishers.IntervalPublisher;


public class UserCountryCountPublisher extends IntervalPublisher<TopNCountries> {
	
	public int countryCount = 25;
	public String source = "user.locations";
	
	private final LocationRepository locationRepository;
	
	
	public UserCountryCountPublisher(EventManager em, LocationRepository countryCountRepo) {
		
		super(em);
		
		this.locationRepository = countryCountRepo;
	}
	
	public UserCountryCountPublisher(EventManager em, LocationRepository countryCountRepo, long sleepInterval) {

		super(em, sleepInterval);
		
		this.locationRepository = countryCountRepo;
	}
	
	public UserCountryCountPublisher(EventManager em, LocationRepository countryCountRepo, int countryCount, long sleepInterval) {

		super(em, sleepInterval);
		
		this.locationRepository = countryCountRepo;
		this.countryCount = countryCount;
	}
	
	public UserCountryCountPublisher(EventManager em, LocationRepository countryCountRepo, int countryCount, long sleepInterval, String source) {

		super(em, sleepInterval);
		
		this.locationRepository = countryCountRepo;
		this.countryCount = countryCount;
		this.source = source;
	}
	
	public void setWordCount(int wordCount) {
		this.countryCount = wordCount;
	}

	@Override
	protected TopNCountries nextEvent() {
		
		Collection<CountryCount> topN = this.locationRepository.getTopNUserCountryCodes(countryCount);

		System.out.println(String.format("Top %d User Countries\n%s", this.countryCount, topN));
		
		return new TopNCountries(topN, source);
	}
	
}