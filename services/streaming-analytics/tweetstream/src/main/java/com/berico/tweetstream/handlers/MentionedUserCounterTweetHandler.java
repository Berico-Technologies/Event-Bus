package com.berico.tweetstream.handlers;

import com.berico.tweetstream.Tweet;
import com.berico.tweetstream.User;
import com.berico.tweetstream.wordcount.WordCountRepository;

public class MentionedUserCounterTweetHandler extends BaseTweetCounterHandler {

	public MentionedUserCounterTweetHandler(WordCountRepository userCounter) {
		super(userCounter);

	}

	@Override
	public String[] provideWordsToCount(Tweet tweet) {
		
		if(tweet.getMentioned().length > 0){
		
			String[] mentioned = new String[tweet.getMentioned().length];
		
			int count = 0;
			
			for(User mention : tweet.getMentioned()){
				
				mentioned[count] = mention.getAccountName();
				count++;
			}
			
			return mentioned;
		}
		
		return new String[]{};
	}	
}