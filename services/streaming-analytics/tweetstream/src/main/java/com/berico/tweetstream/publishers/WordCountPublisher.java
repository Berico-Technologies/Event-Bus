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
package com.berico.tweetstream.publishers;

import java.util.Map;

import com.berico.tweetstream.wordcount.TopNWords;
import com.berico.tweetstream.wordcount.WordCountRepository;

import pegasus.eventbus.client.EventManager;

public class WordCountPublisher extends IntervalPublisher<TopNWords> {
	
	public int wordCount = 25;
	
	private final WordCountRepository wordCountRepo;
	
	private final String wordCountTopic;
	
	public WordCountPublisher(EventManager em, WordCountRepository wordCountRepo, String wordCountTopic) {
		
		super(em);
		
		this.wordCountRepo = wordCountRepo;
		this.wordCountTopic = wordCountTopic;
	}
	
	public WordCountPublisher(EventManager em, WordCountRepository wordCountRepo, String wordCountTopic, long sleepInterval) {

		super(em, sleepInterval);
		
		this.wordCountRepo = wordCountRepo;
		this.wordCountTopic = wordCountTopic;
	}
	
	public WordCountPublisher(EventManager em, WordCountRepository wordCountRepo, String wordCountTopic, int wordCount, long sleepInterval) {

		super(em, sleepInterval);
		
		this.wordCountRepo = wordCountRepo;
		this.wordCountTopic = wordCountTopic;
		this.wordCount = wordCount;
	}
	
	public void setWordCount(int wordCount) {
		this.wordCount = wordCount;
	}

	@Override
	protected TopNWords nextEvent() {
		
		Map<String, Long> topN = this.wordCountRepo.getTopNWords(wordCount);
		
		return new TopNWords(topN, this.wordCountTopic);
	}
	
}
