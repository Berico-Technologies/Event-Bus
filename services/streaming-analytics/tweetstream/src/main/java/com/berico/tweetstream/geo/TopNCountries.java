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

import java.util.Collection;

public class TopNCountries {

	private Collection<CountryCount> topCountries;
	
	private String source = null;
	
	private long timestamp = System.currentTimeMillis();

	public TopNCountries(Collection<CountryCount> topCountries, String source) {

		this.topCountries = topCountries;
		this.source = source;
	}

	public TopNCountries(Collection<CountryCount> topWords, String source, long timestamp) {
		
		this.topCountries = topWords;
		this.source = source;
		this.timestamp = timestamp;
	}

	public Collection<CountryCount> getTopCountriess() {
		return topCountries;
	}

	public void setTopCountries(Collection<CountryCount> topCountries) {
		this.topCountries = topCountries;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
}
