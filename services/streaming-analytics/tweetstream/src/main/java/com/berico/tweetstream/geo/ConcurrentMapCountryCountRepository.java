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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.berico.tweetstream.Location;


public class ConcurrentMapCountryCountRepository implements CountryCountRepository {

	ConcurrentHashMap<String, LocationCount> countryMap = new ConcurrentHashMap<String, LocationCount>();

	public void increment(LocationMention mention) {
		countryMap.putIfAbsent(mention.getLoc().getCountryCode(), new LocationCount(mention.getLoc().getCountryCode()));
		countryMap.get(mention.getLoc().getCountryCode()).incrementCount(mention.getSource());
	}

	public Map<String, Long> getTopNCountries(int N) {
		System.out.println("Getting Top "+N);
		Map<String, Long> topNCountries = new HashMap<String, Long>();
		
		for(Entry<String, LocationCount> locationCountEntry : countryMap.entrySet()){
			
			LocationCount current = locationCountEntry.getValue();
			
			if(topNCountries.size() < N){
				
				topNCountries.put(current.countryCode, current.getCount());
			}
			else {
				
				String countryToReplace = null;
				long countryToReplaceCount = Long.MAX_VALUE;
				
				for(Entry<String, Long> topNCountriesEntry : topNCountries.entrySet()){
					
					if(current.getCount() > topNCountriesEntry.getValue()){
					
						if(countryToReplaceCount > topNCountriesEntry.getValue()){
							
							countryToReplace = topNCountriesEntry.getKey();
							countryToReplaceCount = topNCountriesEntry.getValue();
						}
					}
				}
				
				if(countryToReplace != null){
					
					topNCountries.remove(countryToReplace);
					
					topNCountries.put(current.countryCode, current.getCount());
				}
			}  
		}
		
		return topNCountries;
	}
	
	public long getCount(String countryCode) {
		
		LocationCount wc = countryMap.get(countryCode);
		
		return (wc == null)? 0 : wc.getCount();
	}
	
	@Override
	public long getCountWithSourceField(String countryCode, String sourceField) {
		LocationCount wc = countryMap.get(countryCode);
		
		return (wc == null)? 0 : wc.getCountForSource(sourceField);
	}

	@Override
	public Map<String, Long> getTopNCountriesForSource(int N, String source) {
		Map<String, Long> topNCountries = new HashMap<String, Long>();
		System.out.println("Getting Top "+N+" for " + source);
		for(Entry<String, LocationCount> locationCountEntry : countryMap.entrySet()){
			
			LocationCount current = locationCountEntry.getValue();
			
			if(topNCountries.size() < N){
				
				topNCountries.put(current.countryCode, current.getCountForSource(source));
			}
			else {
				
				String countryToReplace = null;
				long countryToReplaceCount = Long.MAX_VALUE;
				
				for(Entry<String, Long> topNCountriesEntry : topNCountries.entrySet()){
					
					if(current.getCountForSource(source) > topNCountriesEntry.getValue()){
					
						if(countryToReplaceCount > topNCountriesEntry.getValue()){
							
							countryToReplace = topNCountriesEntry.getKey();
							countryToReplaceCount = topNCountriesEntry.getValue();
						}
					}
				}
				
				if(countryToReplace != null){
					
					topNCountries.remove(countryToReplace);
					
					topNCountries.put(current.countryCode, current.getCountForSource(source));
				}
			}  
		}
		
		return topNCountries;
	}
	
	protected class LocationCount {
		
		String countryCode = null;
		ConcurrentHashMap<String, Long> countBySource;
		
		
		LocationCount(String countryCode){
			this.countryCode = countryCode;
			this.countBySource = new ConcurrentHashMap<String, Long>();
		}
		
		
		
		void incrementCount(String source){
			this.countBySource.putIfAbsent(source, 0L);
			long value = this.countBySource.get(source);
			this.countBySource.put(source, ++value);
		}
		
		public long getCount(){
			long returnVal = 0;
			for(Long l : this.countBySource.values()){
				returnVal += l;
			}
			return returnVal;
		}
		
		public long getCountForSource(String source){
			this.countBySource.putIfAbsent(source, 0L);
			return this.countBySource.get(source);
		}
	}



}
