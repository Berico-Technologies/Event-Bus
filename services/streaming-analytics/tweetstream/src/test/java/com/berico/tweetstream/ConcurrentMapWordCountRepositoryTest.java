package com.berico.tweetstream;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

import com.berico.tweetstream.wordcount.ConcurrentMapWordCountRepository;

public class ConcurrentMapWordCountRepositoryTest {
	
	@Test
	public void ensure_item_is_added_to_the_repo() {

		ConcurrentMapWordCountRepository repo = new ConcurrentMapWordCountRepository();
		
		repo.increment("hello");
		repo.increment("world");
		repo.increment("here");
		repo.increment("is");
		repo.increment("another");
		repo.increment("word");
		
		assertEquals(1, repo.getCount("hello"));
		assertEquals(1, repo.getCount("world"));
		assertEquals(1, repo.getCount("here"));
		assertEquals(1, repo.getCount("is"));
		assertEquals(1, repo.getCount("another"));
		assertEquals(1, repo.getCount("word"));
		
		assertEquals(0, repo.getCount("not"));
		assertEquals(0, repo.getCount("in"));
		assertEquals(0, repo.getCount("repo"));
	}
	

	@Test
	public void item_count_returns_correctly_from_the_repository(){
		
		ConcurrentMapWordCountRepository repo = new ConcurrentMapWordCountRepository();
		
		repo.increment("one");
		
		repo.increment("two");
		repo.increment("two");
		
		
		repo.increment("three");
		repo.increment("three");
		repo.increment("three");
		
		assertEquals(1, repo.getCount("one"));
		assertEquals(2, repo.getCount("two"));
		assertEquals(3, repo.getCount("three"));
	}
	
	
	@Test
	public void repo_correctly_returns_the_top_10_items(){
		
		ConcurrentMapWordCountRepository repo = new ConcurrentMapWordCountRepository();
		
		Map<String, Integer> wordsToCountMap = new HashMap<String, Integer>();
		
		wordsToCountMap.put("sixteen", 16);
		wordsToCountMap.put("three", 3);
		wordsToCountMap.put("two", 2);
		wordsToCountMap.put("five", 5);
		wordsToCountMap.put("four", 4);
		wordsToCountMap.put("twenty", 20);
		wordsToCountMap.put("six", 6);
		wordsToCountMap.put("eighteen", 18);
		wordsToCountMap.put("eight", 8);
		wordsToCountMap.put("fourteen", 14);
		wordsToCountMap.put("nine", 9);
		wordsToCountMap.put("three", 3);
		wordsToCountMap.put("eleven", 11);
		wordsToCountMap.put("seven", 7);
		wordsToCountMap.put("thirteen", 13);
		wordsToCountMap.put("three", 3);
		wordsToCountMap.put("fifteen", 15);
		wordsToCountMap.put("twelve", 12);
		wordsToCountMap.put("seventeen", 17);
		wordsToCountMap.put("three", 3);
		wordsToCountMap.put("nineteen", 19);
		wordsToCountMap.put("three", 3);
		
		//Build up the Word Counts
		for(Entry<String, Integer> e : wordsToCountMap.entrySet()){
			
			for(int i = 0; i < e.getValue(); i++){
				
				repo.increment(e.getKey());
			}
		}
		
		Map<String, Long> top5 = repo.getTopNWords(5);
		
		assertEquals(5, top5.size());
		
		assertEquals(16l, (long)top5.get("sixteen"));
		assertEquals(17l, (long)top5.get("seventeen"));
		assertEquals(18l, (long)top5.get("eighteen"));
		assertEquals(19l, (long)top5.get("nineteen"));
		assertEquals(20l, (long)top5.get("twenty"));
		
		Map<String, Long> top10 = repo.getTopNWords(10);
		
		assertEquals(10, top10.size());
		
		assertEquals(11l, (long)top10.get("eleven"));
		assertEquals(12l, (long)top10.get("twelve"));
		assertEquals(13l, (long)top10.get("thirteen"));
		assertEquals(14l, (long)top10.get("fourteen"));
		assertEquals(15l, (long)top10.get("fifteen"));
		assertEquals(16l, (long)top10.get("sixteen"));
		assertEquals(17l, (long)top10.get("seventeen"));
		assertEquals(18l, (long)top10.get("eighteen"));
		assertEquals(19l, (long)top10.get("nineteen"));
		assertEquals(20l, (long)top10.get("twenty"));
	}
	
	
}
