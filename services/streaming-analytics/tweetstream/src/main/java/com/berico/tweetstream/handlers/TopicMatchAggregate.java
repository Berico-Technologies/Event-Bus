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
	
	public void observe(String[] words){
		
		numberOfItemsSeen++;
		
		int numMatches = 0;
		
		for(String word : words){
			for(String keyword : keywords){
				
				if(word.equals(keyword)){
					
					numMatches++;
				}
			}
		}
		
		if(numMatches >= matchThreshold){
			
			numberOfTopicMatches++;
		}
	}
	
	
	
}
