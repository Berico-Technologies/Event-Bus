package com.berico.tweetstream.handlers;

public class TopicMatchAggregate {

	private String[] keywords = null;
	
	private int matchThreshold = -1;
	
	private String description = null;
	
	private long numberOfItemsSeen = 0;
	private long numberOfTopicMatches = 0;
	
	public TopicMatchAggregate(String description, String[] keywords, int matchThreshold){
	
		assert matchThreshold <= keywords.length;
	
		this.description = description;
		this.keywords = keywords;
		this.matchThreshold = matchThreshold;
	}
	
	public String getDescription(){
		return this.description;
	}
	
	public long getNumberOfItemsSeen(){
		return numberOfItemsSeen;
	}
	
	public long getNumberOfTopicMatches(){
		return this.getNumberOfTopicMatches();
	}
	
	public double getMatchRatio(){
		return ((double)this.numberOfTopicMatches) / ((double)this.numberOfItemsSeen);
	}
	
	public void setKeywords(String[] keywords) {
		assert this.keywords == null;
		this.keywords = keywords;
	}

	public void setMatchThreshold(int matchThreshold) {
		assert this.matchThreshold == -1;
		this.matchThreshold = matchThreshold;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setNumberOfItemsSeen(long numberOfItemsSeen) {
		assert this.numberOfItemsSeen == 0;
		this.numberOfItemsSeen = numberOfItemsSeen;
	}

	public void setNumberOfTopicMatches(long numberOfTopicMatches) {
		assert this.numberOfTopicMatches == 0;
		this.numberOfTopicMatches = numberOfTopicMatches;
	}

	public boolean isTopicMatch(String[] words){
		
		numberOfItemsSeen++;
		
		int numMatches = 0;
		
		for(String word : words){
			for(String keyword : keywords){
				
				if(word.equals(keyword)){
					
					numMatches++;
				}
			}
		}
		
		boolean isMatch = numMatches >= matchThreshold;
		
		if(isMatch){
			
			numberOfTopicMatches++;
		}
		
		return isMatch;
	}
	
	
	
}
