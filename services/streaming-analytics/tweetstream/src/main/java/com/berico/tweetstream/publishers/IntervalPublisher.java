package com.berico.tweetstream.publishers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pegasus.eventbus.client.EventManager;

public abstract class IntervalPublisher<T> implements Runnable {
	
	protected abstract T nextEvent();
	
	private final Logger LOG;
	
	public long sleepInterval = 10000;

	private boolean running = false;
	
	private final EventManager eventManager;
	
	public IntervalPublisher(EventManager em) {
		
		LOG = LoggerFactory.getLogger(this.getClass());
		
		this.eventManager = em;
	}
	
	public IntervalPublisher(EventManager em, long sleepInterval) {

		LOG = LoggerFactory.getLogger(this.getClass());
		
		this.eventManager = em;
		this.sleepInterval = sleepInterval;
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
				
				T event = nextEvent();
				
				LOG.trace("Publishing event onto bus: {}", event);
				
				eventManager.publish(event);
				
			} catch (InterruptedException e) {
				
				e.printStackTrace();
			}
		}
	}
	
}
