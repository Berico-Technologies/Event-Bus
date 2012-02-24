package com.berico.tweetstream.wordcount;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentMapWordCountRepository implements WordCountRepository {

	ConcurrentHashMap<String, WordCount> wordMap = new ConcurrentHashMap<String, WordCount>();

	public void increment(String word) {
		
		wordMap.putIfAbsent(word, new WordCount(word));
		wordMap.get(word).count++;
	}

	public Map<String, Long> getTopNWords(int N) {
		
		Map<String, Long> topNWords = new HashMap<String, Long>();
		
		for(Entry<String, WordCount> wordCountEntry : wordMap.entrySet()){
			
			WordCount current = wordCountEntry.getValue();
			
			if(topNWords.size() < N){
				
				topNWords.put(current.word, current.count);
			}
			else {
				
				String wordToReplace = null;
				long wordToReplaceCount = Long.MAX_VALUE;
				
				for(Entry<String, Long> topNWordsEntry : topNWords.entrySet()){
					
					if(current.count > topNWordsEntry.getValue()){
					
						if(wordToReplaceCount > topNWordsEntry.getValue()){
							
							wordToReplace = topNWordsEntry.getKey();
							wordToReplaceCount = topNWordsEntry.getValue();
						}
					}
				}
				
				if(wordToReplace != null){
					
					topNWords.remove(wordToReplace);
					
					topNWords.put(current.word, current.count);
				}
			}  
		}
		
		return topNWords;
	}
	
	public long getCount(String word) {
		
		WordCount wc = wordMap.get(word);
		
		return (wc == null)? 0 : wc.count;
	}
	
	protected class WordCount {
		WordCount(String word){
			this.word = word;
		}
		String word = null;
		long count = 0;
	}
}
