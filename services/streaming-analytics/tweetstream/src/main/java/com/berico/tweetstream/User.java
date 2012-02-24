package com.berico.tweetstream;

import twitter4j.UserMentionEntity;

/**
 * A user, either making the Tweet or mentioned
 * within a Tweet.
 * @author Richard Clayton (Berico Technologies)
 */
public class User {

	private String user = null;
	private String accountName = null;
	private String userImageUrl = null;
	private String userUrl = null;

	public String getUser() {
		return user;
	}
	
	public void setUser(String user) {
		this.user = user;
	}
	
	public String getAccountName() {
		return accountName;
	}
	
	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}
	
	public String getUserImageUrl() {
		return userImageUrl;
	}
	
	public void setUserImageUrl(String userImageUrl) {
		this.userImageUrl = userImageUrl;
	}
	
	public String getUserUrl() {
		return userUrl;
	}
	
	public void setUserUrl(String userUrl) {
		this.userUrl = userUrl;
	}
	
	/**
	 * Convert a Twitter4j user into our representation
	 * of a User.
	 * @param tuser Twitter4j user
	 * @return Our representation of a user.
	 */
	public static User fromUser(twitter4j.User tuser){
		
		User user = new User();
		user.setUser(tuser.getName());
		user.setAccountName(tuser.getScreenName());
		user.setUserImageUrl(tuser.getProfileImageURL().toExternalForm());
		
		if(tuser.getURL() != null){
		
			user.setUserUrl(tuser.getURL().toExternalForm());
		}
		
		return user;
	}
	
	/**
	 * Convert a Twitter4j UserMentionEntity (this is someone
	 * mentioned in a Tweet [prefixed with an '@' sign) into
	 * a User in our model.
	 * @param tuser Twitter4j UserMentionEntity
	 * @return Our representation of a User.
	 */
	public static User fromUserMentionEntity(UserMentionEntity tuser){
		
		User user = new User();
		user.setUser(tuser.getName());
		user.setAccountName(tuser.getScreenName());
		
		return user;
	}
	
}
