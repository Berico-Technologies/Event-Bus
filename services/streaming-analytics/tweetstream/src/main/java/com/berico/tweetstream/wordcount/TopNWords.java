package com.berico.tweetstream.wordcount;

import java.util.Map;

public class TopNWords {

	private Map<String, Long> topWords;
	
	private String source = null;
	
	private long timestamp = System.currentTimeMillis();

	public TopNWords(Map<String, Long> topWords, String source) {

		this.topWords = topWords;
		this.source = source;
	}

	public TopNWords(Map<String, Long> topWords, String source, long timestamp) {
		
		this.topWords = topWords;
		this.source = source;
		this.timestamp = timestamp;
	}

	public Map<String, Long> getTopWords() {
		return topWords;
	}

	public void setTopWords(Map<String, Long> topWords) {
		this.topWords = topWords;
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
