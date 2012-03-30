package com.berico.tweetstream.retweet;

import java.util.ArrayList;
import java.util.List;

import com.berico.tweetstream.Tweet;

public class TopRetweets {

	public static int TOP_N = 25;
	
	private List<Tweet> topRetweets = new ArrayList<Tweet>();
	
	public TopRetweets() {}

	public List<Tweet> getTopRetweets() {
		return topRetweets;
	}

	public void setTopRetweets(List<Tweet> topRetweets) {
		if(this.topRetweets.size() == 0){
			this.topRetweets = topRetweets;
		}
	}

	public boolean isTopRetweet(Tweet newTweet){
		
		if(newTweet.getRetweetCount() > 0){
			
			if(topRetweets.size() < TOP_N){
				
				topRetweets.add(newTweet);
				
				return true;
			}
			
			Tweet leastRetweeted = null;
			
			for(Tweet retweet : this.topRetweets){
				
				if(newTweet.getRetweetCount() > retweet.getRetweetCount()){
					
					if(leastRetweeted == null){
						
						leastRetweeted = retweet;
					}
					else if (leastRetweeted.getRetweetCount() > retweet.getRetweetCount()) {
						
						leastRetweeted = retweet;
					}
				}
			}
			
			if(leastRetweeted != null){
				
				this.topRetweets.remove(leastRetweeted);
				
				this.topRetweets.add(newTweet);
				
				return true;
			}
		}
		
		return false;
	}
	
	
}
