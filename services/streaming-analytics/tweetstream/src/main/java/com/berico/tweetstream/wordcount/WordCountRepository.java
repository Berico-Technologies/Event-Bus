package com.berico.tweetstream.wordcount;

import java.util.Map;

public interface WordCountRepository {

	void increment(String word);
	
	long getCount(String word);
	
	Map<String, Long> getTopNWords(int N);
	
}
