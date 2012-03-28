package com.berico.tweetstream.handlers;

import com.berico.tweetstream.Tweet;
import com.berico.tweetstream.wordcount.WordCountRepository;

public class UserCounterTweetHandler extends BaseTweetCounterHandler {

	public UserCounterTweetHandler(WordCountRepository userCounter) {
		super(userCounter);
	}

	@Override
	public String[] provideWordsToCount(Tweet tweet) {
		// Yes, using the long would be more ideal, but I don't want to redesign the word counter at the moment.
		return new String[]{ Long.toString(tweet.getUser().getUserId()) };
	}

}
