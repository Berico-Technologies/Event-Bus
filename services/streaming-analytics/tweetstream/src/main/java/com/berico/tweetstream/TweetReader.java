package com.berico.tweetstream;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.FileSystemResource;

import pegasus.eventbus.amqp.AmqpConfiguration;
import pegasus.eventbus.amqp.AmqpConnectionParameters;
import pegasus.eventbus.amqp.AmqpEventManager;
import pegasus.eventbus.client.EventManager;

import com.berico.tweetstream.TwitterStreamMode.StreamState;
import com.berico.tweetstream.publishers.TwitterStreamModePublisher;

public class TweetReader {

	private static final int batchSize = 10;
	private static final long timeBetweenBatches = 1200;
	private static final long timeBetweenEach = 100;
	private static final long delay = 50;
	
	public static void main(String[] args) throws UnexpectedInputException, ParseException, Exception{
		
		String filePath = args[0];
		
		AmqpConfiguration config = AmqpConfiguration.getDefault(
				"archive_stream", 
				new AmqpConnectionParameters(
					"amqp://guest:guest@localhost:5672/"));

    	//Initialize the EventManager
    	EventManager em = new AmqpEventManager(config);
    	
    	//Start the EventManager
    	em.start();
    	
    	new TwitterStreamModePublisher(new TwitterStreamMode(StreamState.Historical), em).start();
    	
		ApplicationContext ctxt = new ClassPathXmlApplicationContext("tweetReader.xml");
	
		FlatFileItemReader<Tweet> tweetReader = (FlatFileItemReader<Tweet>) ctxt.getBean("tweetItemReader");

		tweetReader.setResource(new FileSystemResource(filePath));
		tweetReader.setLinesToSkip(1);
		tweetReader.open(new ExecutionContext());
		
		Tweet tweet = null;
		
		do{
			 tweet = tweetReader.read();
			 
			 ModelAdaptors.rectifyAbsentMentions(tweet);
			 ModelAdaptors.obfuscateUserNames(tweet);
			 
			 Thread.sleep(delay);
			 
			 em.publish(tweet);
			
		}while(tweet != null);
		
	}
}
