package com.berico.tweetstream;

import java.util.ArrayList;

import twitter4j.Status;
import twitter4j.UserMentionEntity;

/**
 * This is a wrapper around the twitter4j Status interface
 * so it can be published on the Bus.
 * @author rclayton
 */
public class Tweet {

	private User user = null;
	private String message = null;
	private Location location = null;
	private User[] mentioned = null;
	
	public Tweet(){}
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	
	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public User[] getMentioned() {
		return mentioned;
	}

	public void setMentioned(User[] mentioned) {
		this.mentioned = mentioned;
	}

	/**
	 * Convert a Twitter4j Status update into our
	 * own model for Tweets.
	 * @param status Incoming Status object
	 * @return our representation of a Tweet.
	 */
	public static Tweet fromStatus(Status status){
		
		Tweet tweet = new Tweet();
		tweet.setMessage(status.getText());
		tweet.setUser(User.fromUser(status.getUser()));
		
		if(status.getPlace() != null){
		
			tweet.setLocation(Location.fromPlace(status.getPlace()));
		}
		
		ArrayList<User> lusers = new ArrayList<User>();
		
		for(UserMentionEntity entity : status.getUserMentionEntities()){
			
			lusers.add(User.fromUserMentionEntity(entity));
		}
		
		tweet.setMentioned(lusers.toArray(new User[]{}));
		
		return tweet;
	}
	
	
}
