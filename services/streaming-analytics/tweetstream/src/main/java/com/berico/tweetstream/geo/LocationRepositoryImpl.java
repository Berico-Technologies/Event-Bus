/**
 *   ______     ______     ______     __     ______     ______    
 *  /\  == \   /\  ___\   /\  == \   /\ \   /\  ___\   /\  __ \   
 *  \ \  __<   \ \  __\   \ \  __<   \ \ \  \ \ \____  \ \ \/\ \  
 *   \ \_____\  \ \_____\  \ \_\ \_\  \ \_\  \ \_____\  \ \_____\ 
 *    \/_____/   \/_____/   \/_/ /_/   \/_/   \/_____/   \/_____/ 
 *   ______   ______     ______     __  __     __   __     ______     __         ______     ______     __     ______     ______    
 *  /\__  _\ /\  ___\   /\  ___\   /\ \_\ \   /\ "-.\ \   /\  __ \   /\ \       /\  __ \   /\  ___\   /\ \   /\  ___\   /\  ___\   
 *  \/_/\ \/ \ \  __\   \ \ \____  \ \  __ \  \ \ \-.  \  \ \ \/\ \  \ \ \____  \ \ \/\ \  \ \ \__ \  \ \ \  \ \  __\   \ \___  \  
 *     \ \_\  \ \_____\  \ \_____\  \ \_\ \_\  \ \_\\"\_\  \ \_____\  \ \_____\  \ \_____\  \ \_____\  \ \_\  \ \_____\  \/\_____\ 
 *      \/_/   \/_____/   \/_____/   \/_/\/_/   \/_/ \/_/   \/_____/   \/_____/   \/_____/   \/_____/   \/_/   \/_____/   \/_____/ 
 *                                                                                                                              
 *  All rights reserved - Feb. 2012
 *  Richard Clayton (rclayton@bericotechnologies.com)                                                                                                                
 */
package com.berico.tweetstream.geo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import com.berico.tweetstream.Location;


public class LocationRepositoryImpl implements LocationRepository {
	
	private Vector<LocationMention> allLocations;
	private ConcurrentHashMap<String, List<LocationMention>> locationsByCountry;
	private ConcurrentHashMap<String, List<LocationMention>> locationsBySource;
	
	/*
	 * Count indices
	 */
	private ConcurrentHashMap<String, CountryCount> userLocationByCountryCount;
	private ConcurrentHashMap<String, CountryCount> menitionedLocationsByCountryCount;
	private ConcurrentHashMap<Location, Long> userLocationCount;
	private ConcurrentHashMap<Location, Long> mentionLocationCount;

	
	public LocationRepositoryImpl(){
		this.allLocations = new Vector<LocationMention>();
		this.locationsByCountry = new ConcurrentHashMap<String, List<LocationMention>>();
		this.locationsBySource = new ConcurrentHashMap<String, List<LocationMention>>();
		this.userLocationByCountryCount = new ConcurrentHashMap<String, CountryCount>();
		this.menitionedLocationsByCountryCount = new ConcurrentHashMap<String, CountryCount>();
		this.userLocationCount = new ConcurrentHashMap<Location, Long>();
		this.mentionLocationCount = new ConcurrentHashMap<Location, Long>();
	}
	
	@Override
	public void addLocation(LocationMention mention) {
		indexByCountry(mention);
		indexBySource(mention);
		
		this.allLocations.add(mention);

	}
	
	private void indexBySource(LocationMention mention) {
		
		String source = mention.getSource();
		
		this.locationsBySource.putIfAbsent(source, new ArrayList<LocationMention>());
		this.locationsBySource.get(source).add(mention);
		
		if(source.equals("user")){
			this.userLocationCount.putIfAbsent(mention.getLoc(), 0L);
			long count = this.userLocationCount.get(mention.getLoc());
			this.userLocationCount.put(mention.getLoc(), ++count);
		}else if(source.equals("message")){
			this.mentionLocationCount.putIfAbsent(mention.getLoc(), 0L);
			long count = this.mentionLocationCount.get(mention.getLoc());
			this.mentionLocationCount.put(mention.getLoc(), ++count);
		}

	}

	private void indexByCountry(LocationMention mention) {
		
		String countryCode = mention.getLoc().getCountryCode();
		
		this.locationsByCountry.putIfAbsent(countryCode, new ArrayList<LocationMention>());		
		this.locationsByCountry.get(countryCode).add(mention);
		
		if(mention.getSource().equals("user")){
			this.userLocationByCountryCount.putIfAbsent(countryCode, new CountryCount(mention.getLoc()));
			this.userLocationByCountryCount.get(countryCode).increment();
			
		}else if(mention.getSource().equals("message")){
			this.menitionedLocationsByCountryCount.putIfAbsent(countryCode, new CountryCount(mention.getLoc()));
			this.menitionedLocationsByCountryCount.get(countryCode).increment();
		}
		
	}

	/*
	 * Finds the keys for the N largest values
	 */
	private List<String> findNLargestKeyCount(Map<String, CountryCount> countMap, int n){
		
		List<String> keys = new ArrayList<String>();
		for(String key : countMap.keySet()){
			keys.add(key);
		}
		
		Collections.sort(keys, new CountryCountComparator<String>(countMap));
		
		return keys.subList(0, Math.min(keys.size(), n));
	}
	
	
	@Override
	public Collection<CountryCount> getTopNUserCountryCodes(int N) {
		List<String> countryCodes = findNLargestKeyCount(this.userLocationByCountryCount, N);
		Map<String, CountryCount> result = new HashMap<String, CountryCount>();

		for(String countryCode : countryCodes){
			result.put(countryCode, this.userLocationByCountryCount.get(countryCode));
		}
		return result.values();
	}

	@Override
	public Collection<CountryCount> getTopNMentionedCountryCodes(int N) {
		List<String> countryCodes = findNLargestKeyCount(this.menitionedLocationsByCountryCount, N);
		Map<String, CountryCount> result = new HashMap<String, CountryCount>();
		
		for(String countryCode : countryCodes){
			result.put(countryCode, this.menitionedLocationsByCountryCount.get(countryCode));
		}
		
		return result.values();
	}
	
	/*
	 * Finds the keys for the N largest values
	 */
	private List<Location> findNLargestLocationCount(Map<Location, Long> countMap, int n){
		
		List<Location> keys = new ArrayList<Location>();
		for(Location key : countMap.keySet()){
			keys.add(key);
		}

		Collections.sort(keys, new CountComparator<Location>(countMap));
		
		return keys.subList(0, Math.min(keys.size(), n));
	}
	
	@Override
	public Map<Location, Long> getTopNUserLocations(int N) {
		List<Location> locations = findNLargestLocationCount(this.userLocationCount,N);
		Map<Location, Long> result = new HashMap<Location, Long>();
		for(Location key : locations){
			result.put(key, this.userLocationCount.get(key));
		}
		return result;
	}
	
	@Override
	public Map<Location, Long> getTopNMentionedLocations(int N) {
		List<Location> locations = findNLargestLocationCount(this.mentionLocationCount,N);
		Map<Location, Long> result = new HashMap<Location, Long>();
		for(Location key : locations){
			result.put(key, this.mentionLocationCount.get(key));
		}
		return result;
	}

	@Override
	public Map<String, List<Location>> getUserLocationsByCountry() {
		Map<String, List<Location>> result = new HashMap<String, List<Location>>();
		
		for(String countryCode : this.locationsByCountry.keySet()){
			List<Location> locations = new ArrayList<Location>();
			for(LocationMention location : this.locationsByCountry.get(countryCode)){
				if(location.getSource().equals("user")){
					locations.add(location.getLoc());
				}
			}
			
			result.put(countryCode, locations);
		}
		
		return result;
	}

	@Override
	public Map<String, List<Location>> getMentionedLocationsByCountry() {
		Map<String, List<Location>> result = new HashMap<String, List<Location>>();
		
		for(String countryCode : this.locationsByCountry.keySet()){
			List<Location> locations = new ArrayList<Location>();
			for(LocationMention location : this.locationsByCountry.get(countryCode)){
				if(location.getSource().equals("message")){
					locations.add(location.getLoc());
				}
			}
			
			result.put(countryCode, locations);
		}
		
		return result;
	}


	@Override
	public List<LocationMention> getAllLocations() {
		List<LocationMention> locations = new ArrayList<LocationMention>();
		locations.addAll(this.allLocations);
		return locations;
	}

	
	private static class CountryCountComparator<T> implements Comparator<T>{
		
		Map<T, CountryCount> weights;
		
		CountryCountComparator(Map<T, CountryCount> weights){
			this.weights = weights;
		}

		@Override
		public int compare(T o1, T o2) {

			return Long.valueOf(
					weights.get(o2).getCount())
						.compareTo(
							Long.valueOf(
								weights.get(o1).getCount()));
		}
	}
	
	private static class CountComparator<T> implements Comparator<T>{
		Map<T, Long> weights;
		
		CountComparator(Map<T, Long> weights){
			this.weights = weights;
		}

		@Override
		public int compare(T o1, T o2) {

			return weights.get(o2).compareTo(weights.get(o1));
		}
	}
	

}
