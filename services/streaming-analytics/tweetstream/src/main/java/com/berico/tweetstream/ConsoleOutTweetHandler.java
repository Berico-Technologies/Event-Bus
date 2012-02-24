package com.berico.tweetstream;

import pegasus.eventbus.client.EventHandler;
import pegasus.eventbus.client.EventResult;

/**
 * Prints Tweets to the Console when they arrive off the bus.
 * @author Richard Clayton (Berico Technologies)
 */
public class ConsoleOutTweetHandler implements EventHandler<Tweet> {
	
	@SuppressWarnings("unchecked")
	public Class<? extends Tweet>[] getHandledEventTypes() {
		
		return new Class[]{ Tweet.class };
	}

	/**
	 * Print the Event to the console.
	 */
	public EventResult handleEvent(Tweet event) {
		
		System.out.println(
			String.format(
				"[%s] => %s", 
					event.getUser().getAccountName(), 
					event.getMessage()));
		
		return EventResult.Handled;
	}
}
