package pegasus.eventbus.services.rabbit.status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pegasus.eventbus.client.EventManager;

public class PublisherService {

	private static final Logger                    LOG                                = LoggerFactory.getLogger(PublisherService.class);

	private final EventManager eventManager;
	
	
	public PublisherService(EventManager eventManager) {
		super();
		this.eventManager = eventManager;
	}

	public void start(){
		LOG.info("Rabbit Status Publisher Service started.");
	}
	
	public void stop(){
		LOG.info("Rabbit Status Publisher Service started.");
	}
	
}
