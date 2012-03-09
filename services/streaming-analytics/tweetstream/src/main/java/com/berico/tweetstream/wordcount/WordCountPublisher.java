/**
 *   ______     ______     ______     __     ______     ______    
 *  /\  == \   /\  ___\   /\  == \   /\ \   /\  ___\   /\  __ \   
 *  \ \  __<   \ \  __\   \ \  __<   \ \ \  \ \ \____  \ \ \/\ \  
 *   \ \_____\  \ \_____\  \ \_\ \_\  \ \_\  \ \_____\  \ \_____\ 
 *    \/_____/   \/_____/   \/_/ /_/   \/_/   \/_____/   \/_____/ 
 *   ______   ______     ______     __  __     __   __     ______     __         ______     ______     __     ______     ______    
 *  /\__  _\ /\  ___\   /\  ___\   /\ \_\ \   /\ "-.\ \   /\  __ \   /\ \       /\  __ \   /\  ___\   /\ \   /\  ___\   /\  ___\   
 *  \/_/\ \/ \ \  __\   \ \ \____  \ \  __ \  \ \ \-.  \  \ \ \/\ \  \ \ \____  \ \ \/\ \  \ \ \__ \  \ \ \  \ \  __\   \ \___  \  
 *     \ \_\  \ \_____\  \ \_____\  \ \_\ \_\  \ \_\\"\_\  \ \_____\  \ \_____\  \ \_____\  \ \_____\  \ \_\  \ \_____\  \/\_____\ 
 *      \/_/   \/_____/   \/_____/   \/_/\/_/   \/_/ \/_/   \/_____/   \/_____/   \/_____/   \/_____/   \/_/   \/_____/   \/_____/ 
 *                                                                                                                              
 *  All rights reserved - Feb. 2012
 *  Richard Clayton (rclayton@bericotechnologies.com)                                                                                                                
 */
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
	
	private final String wordCountTopic;
	
	public WordCountPublisher(EventManager em, WordCountRepository wordCountRepo, String wordCountTopic) {
		
		this.em = em;
		this.wordCountRepo = wordCountRepo;
		this.wordCountTopic = wordCountTopic;
	}
	
	public WordCountPublisher(EventManager em, WordCountRepository wordCountRepo, String wordCountTopic, int wordCount, long sleepInterval) {

		this.em = em;
		this.wordCountRepo = wordCountRepo;
		this.wordCountTopic = wordCountTopic;
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
				
				em.publish(new TopNWords(topN, this.wordCountTopic));
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
}
