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


public class ConcurrentMapCountryCountRepository implements CountryCountRepository {

	ConcurrentHashMap<String, WordCount> countryMap = new ConcurrentHashMap<String, WordCount>();

	public void increment(String word) {
		
		countryMap.putIfAbsent(word, new WordCount(word));
		countryMap.get(word).count++;
	}

	public Map<String, Long> getTopNCountries(int N) {
		
		Map<String, Long> topNCountries = new HashMap<String, Long>();
		
		for(Entry<String, WordCount> wordCountEntry : countryMap.entrySet()){
			
			WordCount current = wordCountEntry.getValue();
			
			if(topNCountries.size() < N){
				
				topNCountries.put(current.word, current.count);
			}
			else {
				
				String countryToReplace = null;
				long countryToReplaceCount = Long.MAX_VALUE;
				
				for(Entry<String, Long> topNCountriesEntry : topNCountries.entrySet()){
					
					if(current.count > topNCountriesEntry.getValue()){
					
						if(countryToReplaceCount > topNCountriesEntry.getValue()){
							
							countryToReplace = topNCountriesEntry.getKey();
							countryToReplaceCount = topNCountriesEntry.getValue();
						}
					}
				}
				
				if(countryToReplace != null){
					
					topNCountries.remove(countryToReplace);
					
					topNCountries.put(current.word, current.count);
				}
			}  
		}
		
		return topNCountries;
	}
	
	public long getCount(String word) {
		
		WordCount wc = countryMap.get(word);
		
		return (wc == null)? 0 : wc.count;
	}
	
	protected class WordCount {
		WordCount(String word){
			this.word = word;
		}
		String word = null;
		long count = 0;
	}
}
