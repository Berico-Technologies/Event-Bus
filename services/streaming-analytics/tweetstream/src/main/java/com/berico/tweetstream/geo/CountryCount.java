package com.berico.tweetstream.geo;

import com.berico.tweetstream.Location;


public class CountryCount {

	private String country = null;
	private String countryCode = null;
	private long count = 0;
	
	public CountryCount(){}

	public CountryCount(Location location){
		
		this.country = location.getFullname();
		this.countryCode = location.getCountryCode();
	}
	
	public CountryCount(String country, String countryCode, long count) {
	
		this.country = country;
		this.countryCode = countryCode;
		this.count = count;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}
	
	public void increment(){
		this.count++;
	}

	@Override
	public String toString() {
		return countryCode + "=" + count;
	}
}
