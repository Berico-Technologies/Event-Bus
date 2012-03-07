package pegasus.eventbus.services.rabbit.status;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pegasus.eventbus.client.EventManager;
import pegasus.eventbus.services.rabbit.status.monitors.ChannelCountMonitor;
import pegasus.eventbus.services.rabbit.status.monitors.ConnectionCountMonitor;
import pegasus.eventbus.services.rabbit.status.monitors.DeliveredMessagesPerSecondMonitor;
import pegasus.eventbus.services.rabbit.status.monitors.PublishedMessagesPerSecondMonitor;
import pegasus.eventbus.services.rabbit.status.monitors.QueueCountMonitor;
import pegasus.eventbus.services.rabbit.status.monitors.QueuedMessagesMonitor;
import pegasus.eventbus.services.rabbit.status.monitors.TotalMessagesMonitor;
import pegasus.eventbus.services.rabbit.status.monitors.UnacknowledgedMessagesMonitor;

public class PublisherService {

	private static final Logger                    LOG                                = LoggerFactory.getLogger(PublisherService.class);
	public static EventManager eventManager;
	public static RabbitManagementApiHelper apiHelper;
	
	private ScheduledExecutorService scheduler ;

	public PublisherService(EventManager eventManager, RabbitManagementApiHelper apiHelper) {
		PublisherService.eventManager = eventManager;
		PublisherService.apiHelper = apiHelper;
	}

	public void start(){
		LOG.info("Rabbit Status Publisher Service starting...");
		
		List<Runnable> monitors = getMonitorPublishers();
		
		scheduler = Executors.newScheduledThreadPool(monitors.size());
		int intervalBetweenMonitors = 1000 / monitors.size();
		int delay = 0;
		for(Runnable monitor : monitors){
			scheduler.scheduleAtFixedRate(monitor, delay, 1000, TimeUnit.MILLISECONDS);
			delay += intervalBetweenMonitors;
		}
		
		LOG.info("Rabbit Status Publisher Service started.");
	}
	
	private List<Runnable> getMonitorPublishers() {
		ArrayList<Runnable> publishers = new ArrayList<Runnable>();
		
		publishers.add( new Publisher( new PublishedMessagesPerSecondMonitor()));
		publishers.add( new Publisher( new DeliveredMessagesPerSecondMonitor()));
		publishers.add( new Publisher( new UnacknowledgedMessagesMonitor()));
		publishers.add( new Publisher( new QueuedMessagesMonitor()));
		publishers.add( new Publisher( new TotalMessagesMonitor()));
		publishers.add( new Publisher( new ConnectionCountMonitor()));
		publishers.add( new Publisher( new ChannelCountMonitor()));
		publishers.add( new Publisher( new QueueCountMonitor()));
		
		return publishers;
	}

	public void stop(){
		LOG.info("Rabbit Status Publisher Service started.");
		if(scheduler != null && !scheduler.isShutdown()){
			scheduler.shutdown();
		}
	}
}