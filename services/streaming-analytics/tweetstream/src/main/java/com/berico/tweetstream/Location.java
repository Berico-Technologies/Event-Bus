package com.berico.tweetstream;

import twitter4j.GeoLocation;

/**
 * Location that a Twitter user has sent a Tweet from.
 * @author Richard Clayton (Berico Technologies)
 */
public class Location {

	private String fullname = null;
	private String country = null;
	private String countryCode = null;
	private String locationType = null;
	private String url = null;
	private double latitude = -91d;
	private double longitude = -181d;

	public String getFullname() {
		return fullname;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
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

	public String getLocationType() {
		return locationType;
	}

	public void setLocationType(String locationType) {
		this.locationType = locationType;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public double getLatitude() {
		return latitude;
	}
	
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	
	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	/**
	 * Adapt a Twitter4j "Place" to our model's Location
	 * entity.
	 * @param place A place in which someone has tweeted
	 * @return our representation of a Location.
	 */
	public static Location fromPlace(twitter4j.Place place){
		
		Location location = new Location();
		
		if(place.getCountry() != null){
			location.setCountry(place.getCountry());
		}
		
		if(place.getCountryCode() != null){
			location.setCountryCode(place.getCountryCode());
		}
		
		
		location.setFullname(place.getFullName());
		
		location.setLocationType(place.getPlaceType());
		
		location.setUrl(place.getURL());
		
		if(place.getGeometryCoordinates() != null){
			if(place.getGeometryCoordinates().length > 0){
				if(place.getGeometryCoordinates()[0].length > 0){
					GeoLocation loc = place.getGeometryCoordinates()[0][0];
					
					if(loc != null){
						location.setLatitude(loc.getLatitude());
						location.setLongitude(loc.getLongitude());
					}
				}
			}
		}
		
		return location;
	}
	
}
