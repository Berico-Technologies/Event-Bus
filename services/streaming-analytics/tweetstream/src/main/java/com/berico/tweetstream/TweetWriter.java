package com.berico.tweetstream;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import twitter4j.FilterQuery;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;

public class TweetWriter implements StatusListener {

	public static final String DELIMITER = "\t";
	
	public static final String HEADER = 
			  "id" + DELIMITER 
			//+ "name" + DELIMITER 
			+ "screen name" + DELIMITER 
			+ "language" + DELIMITER
			+ "timezone" + DELIMITER
			+ "UTC offset" + DELIMITER
			+ "Location" + DELIMITER
			//+ "Source" + DELIMITER
			+ "Text" + DELIMITER
			+ "Created At" + DELIMITER
			+ "is Retweet" + DELIMITER
			+ "Retweet Count" + DELIMITER
			+ "Lat,Long" + DELIMITER
			+ "Place Name" + DELIMITER
			+ "Place Country" + DELIMITER;	
	
	BufferedWriter bufferedWriter = null;
	
	public TweetWriter(String location){
		
		try {
			
			/*
			File outputFile = new File(
					String.format(
							"/Users/rclayton/Data/TWEETS/sample_%s.utf8.txt", 
							System.currentTimeMillis()));
			*/
			
			File outputFile = new File(location);
			
			FileOutputStream fos = new FileOutputStream(outputFile);
			
			bufferedWriter = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
			
			bufferedWriter.write(HEADER);
			bufferedWriter.newLine();
			
		} catch (IOException e) {

			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
	}
	
	public static void main(String[] args) {

		if(args.length < 2){
			print("Signature:  [location to save output] [method: sample, keyword, location]" + 
				  "[if keyword: comma seperated list of keywords] [if location: lat long pairs seperated by semicolons]");
			
			for(String arg : args){
				System.out.print(arg + " ");
			}
			
			return;
		}
		
		TweetWriter tweetWriter = new TweetWriter(args[0]);
		
		TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
		
		twitterStream.addListener(tweetWriter);
		
		String method = args[1];
		
		if(method.equals("sample")){
		
			print("Sampling.");
			
			twitterStream.sample();
		}
		else {
			
			if(args.length != 3){
				
				print("Please specify either keywords (as comma seperated values) or location (as lat long pairs).");
				return;
			}
			
			if(method.equals("keyword")){
				
				String[] keywords = args[2].replaceAll("\"", "").split(",");
				
				FilterQuery fq = new FilterQuery();
				fq.track(keywords);
				
				print("Filtering by keywords: %s", keywords);
				
				twitterStream.filter(fq);
			}
			else if (method.equals("location")) {
				
				System.out.println("Filtering on Location");
				
				String[] pairs = args[2].split(";");
				
				double[][] latlongs = new double[pairs.length][2];
				
				int index = 0;
				
				for(String pair : pairs){
					
					String[] latlonStr = pair.split(",");
					double lat = Double.parseDouble(latlonStr[0]);
					double lon = Double.parseDouble(latlonStr[1]);
					
					latlongs[index] = new double[]{ lat, lon };
					
					index++;
				}
				
				System.out.println(latlongs);
				
				FilterQuery fq = new FilterQuery();
				fq.locations(latlongs);
				
				print("Filtering by locations: %s", latlongs);
				twitterStream.filter(fq);
			}
			else {
				
				System.out.println("Filtering on Defaults");
				
				FilterQuery fq = new FilterQuery();
				fq.track("China,Xilai,Lashkar-e-Tayyibba,Lashkar-e-Taibba,Lashkar,Tayyibba,Taibba,LeT,Kashmir,Bhartiya,Janata,Iran,Pakistan,ISS,Taliban".split(","));
				//fq.locations(new double[][]{ new double[]{ -122.75,36.8 }, new double[]{-121.75,37.8}});
				fq.locations(new double[][]{ new double[]{67.236328,7.71099}, new double[]{92.548828,32.990236}});
				twitterStream.filter(fq);
			}
		}
		
		
		print("Hit End.");
	}

	
	public void onException(Exception ex) {
		
	}

	public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
		
	}

	public void onScrubGeo(long arg0, long arg1) {
		
	}

	int count = 0;
	
	public void onStatus(Status status) {

		String delimitedString = createDelimitedLine(status);
		
		print(delimitedString);
		
		try {
		
			bufferedWriter.write(delimitedString);
			bufferedWriter.newLine();
			
			count++;
			
			if(count == 25){
			
				bufferedWriter.flush();
			
				count = 0;
			}
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		
	}

	public void onTrackLimitationNotice(int arg0) {
		
	}
	
	protected String normalize(String in){
		if(in == null){ return ""; }
		return in.replaceAll("\r", " ").replaceAll("\n", " ").replaceAll("\t", " ");
	}
	
	protected String createDelimitedLine(Status status){
		
		StringBuilder sb = new StringBuilder();
		
		Object[] params = null;
		
		try {
			
		params = new Object[]{
			status.getUser().getId(),
			//status.getUser().getName(),
			status.getUser().getScreenName(),
			status.getUser().getLang(),
			status.getUser().getTimeZone(),
			status.getUser().getUtcOffset(),
			normalize(status.getUser().getLocation()),
			//status.getSource(),
			normalize(status.getText()),
			status.getCreatedAt(),
			status.isRetweet(),
			status.getRetweetCount(),
			(status.getGeoLocation() != null)? String.format("%s,%s", status.getGeoLocation().getLatitude(), status.getGeoLocation().getLongitude()) : "",
			(status.getPlace() != null)? status.getPlace().getFullName() : "",
			(status.getPlace() != null)? status.getPlace().getCountry() : "",
			};
		} catch (Exception e){
			e.printStackTrace();
		}
		
		for(Object param : params){
			
			sb.append(param).append(DELIMITER);
		}
		
		String value = sb.substring(0, sb.length() - 1);
				
		return value;
	}
	
	public void close(){
		
		try {
			
			bufferedWriter.close();

		} catch (IOException e) {

			e.printStackTrace();
		}
		
	}

	public static void print(String message){
		System.out.println(message);
	}
	
	public static void print(String template, Object... objects){
		System.out.println(String.format(template, objects));
	}
}
