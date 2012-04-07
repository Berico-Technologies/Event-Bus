package com.berico.tweetstream.retweet;

import java.util.ArrayList;
import java.util.List;

import com.berico.tweetstream.Tweet;
import com.berico.tweetstream.User;

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
			
			swapTweeter(newTweet);
			
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
	
	/**
	 * Take the first mention, and declare that as the user who
	 * made the tweet since it is a "retweet".
	 * @param tweet
	 */
	protected void swapTweeter(Tweet tweet){
		
		if(tweet.getMentioned() != null && tweet.getMentioned().length > 0){
			User firstMention = tweet.getMentioned()[0];
			
			tweet.setUser(firstMention);
		}
	}
}
