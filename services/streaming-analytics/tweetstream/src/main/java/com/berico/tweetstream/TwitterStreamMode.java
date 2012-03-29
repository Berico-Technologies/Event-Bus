package com.berico.tweetstream;

public class TwitterStreamMode {

	public enum Mode {
		Filter,
		Sample
	}
	
	public enum StreamState {
		Live,
		Historical
	}
	
	private Mode mode = Mode.Sample;
	
	private StreamState state = StreamState.Live;
	
	private String[] keywords = new String[]{};
	
	private double[][] locations = new double[][]{};

	public TwitterStreamMode(){}
	
	public TwitterStreamMode(StreamState state, String[] keywords) {

		this.mode = Mode.Filter;
		this.state = state;
		this.keywords = keywords;
	}
	
	public TwitterStreamMode(StreamState state, double[][] locations) {

		this.mode = Mode.Filter;
		this.state = state;
		this.locations = locations;
	}
	
	public TwitterStreamMode(StreamState state, String[] keywords, double[][] locations) {

		this.mode = Mode.Filter;
		this.state = state;
		this.keywords = keywords;
		this.locations = locations;
	}

	public Mode getMode() {
		return mode;
	}

	public void setMode(Mode mode) {
		this.mode = mode;
	}

	public StreamState getState() {
		return state;
	}

	public void setState(StreamState state) {
		this.state = state;
	}

	public String[] getKeywords() {
		return keywords;
	}

	public void setKeywords(String[] keywords) {
		this.keywords = keywords;
	}

	public double[][] getLocations() {
		return locations;
	}

	public void setLocations(double[][] locations) {
		this.locations = locations;
	}
}
