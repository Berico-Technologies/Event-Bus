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

import java.util.ArrayList;

public class StopFilterWordSplitter implements WordSplitter {

	public static String[] defaultStopWords = new String[]{ 
		"rt", "a", "about", "above", "above", "across", "after", "afterwards", "again", "against", "all", 
		"almost", "alone", "along", "already", "also","although","always","am","among", "amongst", "amoungst", 
		"amount",  "an", "and", "another", "any","anyhow","anyone","anything","anyway", "anywhere", "are", 
		"around", "as",  "at", "back","be","became", "because","become","becomes", "becoming", "been", "before", 
		"beforehand", "behind", "being", "below", "beside", "besides", "between", "beyond", "bill", "both", "bottom",
		"but", "by", "call", "can", "cannot", "cant", "co", "con", "could", "couldnt", "cry", "de", "describe", "detail", 
		"do", "done", "down", "due", "during", "each", "eg", "eight", "either", "eleven","else", "elsewhere", "empty", 
		"enough", "etc", "even", "ever", "every", "everyone", "everything", "everywhere", "except", "few", "fifteen", 
		"fify", "fill", "find", "fire", "first", "five", "for", "former", "formerly", "forty", "found", "four", "from", 
		"front", "full", "further", "get", "give", "go", "had", "has", "hasnt", "have", "he", "hence", "her", "here", 
		"hereafter", "hereby", "herein", "hereupon", "hers", "herself", "him", "himself", "his", "how", "however", 
		"hundred", "i", "ie", "if", "in", "inc", "indeed", "interest", "into", "is", "it", "its", "itself", "keep", 
		"last", "latter", "latterly", "least", "less", "ltd", "made", "many", "may", "me", "meanwhile", "might", "mill", 
		"mine", "more", "moreover", "most", "mostly", "move", "much", "must", "my", "myself", "name", "namely", "neither", 
		"never", "nevertheless", "next", "nine", "no", "nobody", "none", "noone", "nor", "not", "nothing", "now", "nowhere", 
		"of", "off", "often", "on", "once", "one", "only", "onto", "or", "other", "others", "otherwise", "our", "ours", 
		"ourselves", "out", "over", "own","part", "per", "perhaps", "please", "put", "rather", "re", "same", "see", 
		"seem", "seemed", "seeming", "seems", "serious", "several", "she", "should", "show", "side", "since", "sincere", 
		"six", "sixty", "so", "some", "somehow", "someone", "something", "sometime", "sometimes", "somewhere", "still", 
		"such", "system", "take", "ten", "than", "that", "the", "their", "them", "themselves", "then", "thence", "there", 
		"thereafter", "thereby", "therefore", "therein", "thereupon", "these", "they", "thickv", "thin", "third", "this", 
		"those", "though", "three", "through", "throughout", "thru", "thus", "to", "together", "too", "top", "toward", 
		"towards", "twelve", "twenty", "two", "un", "under", "until", "up", "upon", "us", "very", "via", "was", "we", 
		"well", "were", "what", "whatever", "when", "whence", "whenever", "where", "whereafter", "whereas", "whereby", 
		"wherein", "whereupon", "wherever", "whether", "which", "while", "whither", "who", "whoever", "whole", "whom", 
		"whose", "why", "will", "with", "within", "without", "would", "yet", "you", "your", "yours", "yourself", 
		"yourselves", "the", "let", "let's", "lets", "know", "dont", "don't", "know", "just", "lol", "hate", "love", 
		"want", "need", "way", "wont", "won't", "ill", "i'll", "today", "good", "like", "make", "day", "u", "people", 
		"im", "i'm", "just", "dont", "don't", "know", "que","la",
		"fuck", "shit", "damn", "bitch", "ass", "cunt", "dick", "cock", "penis", "vagina","nigga","nigger",
		"come","yg","life","los","como","vou","got","man","mas","life","haha","mais","um","meu","yo","le","mi","di",
		"new","las","una","em","vai","really","wanna", "el","eu","te","se","en","da","é","não","es","lo","ya","si",
		"faz","voc","1d","apa","ela","ada","nada","oh","ta","vc","youre","ver","ok","ah","del","thats","aint","follow",
		"na","tu","por","com","think","pra","para" };
	
	private String[] stopWords = null;

	public StopFilterWordSplitter(){
		stopWords = defaultStopWords;
	}
	
	public StopFilterWordSplitter(String[] additionalStopWords){
		
		stopWords = join(defaultStopWords, additionalStopWords);
	}
	
	public String[] split(String sentence) {
		
		ArrayList<String> normalizedWords = new ArrayList<String>();
		
		String[] words = sentence.split("\\s+");
		
		for(String word : words){
			
			String tempWord = cleanse(word.toLowerCase());
			
			if(!isStopWord(tempWord) && tempWord.length() > 2){
				normalizedWords.add(tempWord);
			}
		}
		
		return normalizedWords.toArray(new String[]{});
	}
	
	private String cleanse(String word){
		
		StringBuilder sb = new StringBuilder();
		
		for(char c : word.toCharArray()){
			
			if(Character.isLetterOrDigit(c)){
				sb.append(c);
			}
		}
		
		return sb.toString();
	}
	
	private boolean isStopWord(String word){
		
		for(String stopWord : stopWords){
			if(word.equalsIgnoreCase(stopWord)){
				return true;
			}
		}
		return false;
	}

	
	public static String[] join(String[] arrayOne, String[] arrayTwo){
		
		   String[] C= new String[arrayOne.length + arrayTwo.length];
		   
		   System.arraycopy(arrayOne, 0, C, 0, arrayOne.length);
		   
		   System.arraycopy(arrayTwo, 0, C, arrayOne.length, arrayTwo.length);
		   
		   return C;
	}
}
