package com.berico.tweetstream.wordcount;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pegasus.eventbus.client.EventManager;

public class WordCountPublisher implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(WordCountPublisher.class);
	
	public int wordCount = 25;
	
	public long sleepInterval = 10000;

	private boolean running = false;
	
	private final EventManager em;
	
	private final WordCountRepository wordCountRepo;
	
	public WordCountPublisher(EventManager em, WordCountRepository wordCountRepo) {

		this.em = em;
		this.wordCountRepo = wordCountRepo;
	}
	
	public WordCountPublisher(EventManager em, WordCountRepository wordCountRepo, int wordCount, long sleepInterval) {

		this.em = em;
		this.wordCountRepo = wordCountRepo;
		this.wordCount = wordCount;
		this.sleepInterval = sleepInterval;
	}
	
	public void setWordCount(int wordCount) {
		this.wordCount = wordCount;
	}

	public void setSleepInterval(long sleepInterval) {
		this.sleepInterval = sleepInterval;
	}

	public void start(){
		new Thread(this).start();
	}
	
	public void stop(){
		this.running = false;
	}
	
	public void run() {
		
		this.running = true;
		
		while(this.running){
		
			try {
				
				Thread.sleep(sleepInterval);
				
				Map<String, Long> topN = this.wordCountRepo.getTopNWords(wordCount);
				
				LOG.trace("Publishing top {} words onto bus.", wordCount);
				
				em.publish(new TopNWords(topN, "Twitter Stream"));
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
}
