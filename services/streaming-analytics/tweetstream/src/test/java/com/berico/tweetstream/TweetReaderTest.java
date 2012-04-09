package com.berico.tweetstream;

import static org.junit.Assert.*;

import org.junit.Test;

public class TweetReaderTest {

	@Test
	public void rectify_absent_mentions_properly_extracts_mentions_from_text_body() {
		
		String text = "@feelingsMonique @juliaturra: sim sim, tenho a quarta e a sexta da semana que vem livre, eu acho KKK";
		
		User user1 = new User();
		user1.setAccountName("@feelingsMonique");
		
		User user2 = new User();
		user2.setAccountName("@juliaturra");
		
		User[] expectedMentions = new User[]{ user1, user2  };
		
		Tweet tweet = new Tweet();
		tweet.setMessage(text);
		
		ModelAdaptors.rectifyAbsentMentions(tweet);

		assertEquals(expectedMentions, tweet.getMentioned());
	}

}
