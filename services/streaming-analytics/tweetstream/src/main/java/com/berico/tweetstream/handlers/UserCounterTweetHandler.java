package com.berico.tweetstream.handlers;

import com.berico.tweetstream.Tweet;
import com.berico.tweetstream.wordcount.WordCountRepository;

public class UserCounterTweetHandler extends BaseTweetCounterHandler {

	public UserCounterTweetHandler(WordCountRepository userCounter) {
		super(userCounter);
	}

	@Override
	public String[] provideWordsToCount(Tweet tweet) {
		
		return new String[]{ tweet.getUser().getAccountName() };
	}

}
