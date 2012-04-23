package com.berico.tweetstream.geo;

import com.berico.tweetstream.Location;

public class LocationMention {
	
	private Location loc;
	private String source;
	
	public LocationMention(Location loc, String source){
		this.setLoc(loc);
		this.setSource(source);
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public Location getLoc() {
		return loc;
	}

	public void setLoc(Location loc) {
		this.loc = loc;
	}
}
