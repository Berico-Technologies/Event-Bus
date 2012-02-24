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
