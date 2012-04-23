package com.berico.tweetstream;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;

public class TweetMapper implements FieldSetMapper<Tweet> {
	public Tweet mapFieldSet(FieldSet fs) {

		if(fs == null){
			return null;
		}

		Tweet tweet = new Tweet();

		/*
		 * Map User
		 */
		User u = new User();
		u.setUserId(fs.readLong(0));
		u.setUser(fs.readString(1));
		u.setAccountName(fs.readString(1));
		u.setLocation(fs.readString(5));
		tweet.setUser(u);

		

		/*
		 * Map Location
		 */
		Location loc = new Location();
		loc.setCountry(fs.readString(12));

		String latLonString = fs.readString(10);
		if(!latLonString.isEmpty()){
			String[] latLon = latLonString.split(",");
			loc.setLatitude(Double.parseDouble(latLon[0]));
			loc.setLongitude(Double.parseDouble(latLon[1]));
		}

		loc.setFullname(fs.readString(11));
		tweet.setLocation(loc);

		/*
		 * Map Other Tweet Info
		 */
		tweet.setMessage(fs.readString(6));
		tweet.setTimeOfTweet(fs.readString(7));
		tweet.setRetweetCount(fs.readLong(9));
		tweet.setMentioned(new User[]{});

		return tweet;
	}
}
