package com.berico.tweetstream;

import java.util.ArrayList;

import twitter4j.GeoLocation;
import twitter4j.Status;
import twitter4j.UserMentionEntity;

public class ModelAdaptors {

	public static User fromUser(twitter4j.User tuser){
		
		User user = new User();
		
		user.setUserId(tuser.getId());
		user.setFollowers(tuser.getFollowersCount());
		user.setLocation(tuser.getLocation());
		
		return user;
	}
	
	public static User fromUserComplete(twitter4j.User tuser){
		User user = fromUser(tuser);
		
		user.setUser(tuser.getName());
		user.setAccountName(tuser.getScreenName());
		user.setUserImageUrl(tuser.getProfileImageURL().toExternalForm());
		
		if(tuser.getURL() != null){
			
			user.setUserUrl(tuser.getURL().toExternalForm());
		}
		
		return user;
	}
	
	public static User fromUserMentionEntity(UserMentionEntity tuser){
		
		User user = new User();
		
		user.setUserId(tuser.getId());

		return user;
	}
	
	public static User fromUserMentionEntityComplete(UserMentionEntity tuser){
		
		User user = fromUserMentionEntity(tuser);
		
		user.setUser(tuser.getName());
		user.setAccountName(tuser.getScreenName());
		
		return user;
	}
	

	public static Location fromPlace(twitter4j.Place place){
		
		Location location = new Location();
		
		if(place.getCountry() != null){
			location.setCountry(place.getCountry());
		}
		
		if(place.getCountryCode() != null){
			location.setCountryCode(place.getCountryCode());
		}
		
		location.setFullname(place.getFullName());
		
		location.setLocationType(place.getPlaceType());
		
		location.setUrl(place.getURL());
		
		if(place.getGeometryCoordinates() != null){
			if(place.getGeometryCoordinates().length > 0){
				if(place.getGeometryCoordinates()[0].length > 0){
					GeoLocation loc = place.getGeometryCoordinates()[0][0];
					
					if(loc != null){
						location.setLatitude(loc.getLatitude());
						location.setLongitude(loc.getLongitude());
					}
				}
			}
		}
		
		return location;
	}
	
	public static Tweet fromStatus(Status status){
		
		Tweet tweet = new Tweet();
		
		tweet.setUser(fromUser(status.getUser()));
		
		if(status.getPlace() != null){
		
			tweet.setLocation(fromPlace(status.getPlace()));
		}
		
		tweet.setTimeOfTweet(status.getCreatedAt().toString());
		tweet.setRetweetCount(status.getRetweetCount());
		
		ArrayList<User> lusers = new ArrayList<User>();
		
		String statusText = status.getText();
		
		for(UserMentionEntity entity : status.getUserMentionEntities()){
			
			lusers.add(fromUserMentionEntity(entity));
			
			statusText = stripMentionedUserFromStatus(statusText, entity);
		}
		
		tweet.setMessage(statusText);
		
		tweet.setMentioned(lusers.toArray(new User[]{}));
		
		return tweet;
	}
	
	
	public static String stripMentionedUserFromStatus(String status, UserMentionEntity mention){
		String newStatus = status.replace(mention.getScreenName(), Long.toString(mention.getId())); 
		
		System.out.println(newStatus);
		
		return newStatus;
	}

	public static Tweet fromStatusComplete(Status status){
		
		Tweet tweet = new Tweet();
		tweet.setMessage(status.getText());
		tweet.setUser(fromUserComplete(status.getUser()));
		tweet.setTimeOfTweet(status.getCreatedAt().toString());
		tweet.setRetweetCount(status.getRetweetCount());
		
		if(status.getPlace() != null){
		
			tweet.setLocation(fromPlace(status.getPlace()));
		}
		
		ArrayList<User> lusers = new ArrayList<User>();
		
		for(UserMentionEntity entity : status.getUserMentionEntities()){
			
			lusers.add(fromUserMentionEntityComplete(entity));
		}
		
		tweet.setMentioned(lusers.toArray(new User[]{}));
		
		return tweet;
	}
	
	public static User[] extractMentions(Tweet tweet){
		ArrayList<User> users = new ArrayList<User>();
		
		String text = tweet.getMessage();
		
		int mentionStart = -1;
		
		int count = -1;
		
		StringBuilder sb = null;
		
		for(char c : text.toCharArray()){
			
			count++;
			
			if(c == '@'){
				
				mentionStart = count;
				sb = new StringBuilder();
			}
			
			if(!Character.isLetterOrDigit(c) && c != '@'){
				
				mentionStart = -1;
				
				if(sb != null){
					
					User user = new User();
					user.setAccountName(sb.toString());
					users.add(user);
					
					sb = null;
				}
			}
			
			if(mentionStart != -1){
				
				sb.append(c);
			}
			
		}
		
		return users.toArray(new User[]{});
	}
	
	public static void rectifyAbsentMentions(Tweet tweet){
		
		tweet.setMentioned(extractMentions(tweet));
	}

	public static void obfuscateUserNames(Tweet tweet){
		
		String message = tweet.getMessage();
		
		for(User mention : tweet.getMentioned()){
			
			mention.setUserId(mention.getAccountName().hashCode());
			
			message = message.replace(mention.getAccountName(), String.format("@%s", mention.getUserId()));
			
			mention.setAccountName("omitted");
		}
		
		tweet.setMessage(message);
	}
}
