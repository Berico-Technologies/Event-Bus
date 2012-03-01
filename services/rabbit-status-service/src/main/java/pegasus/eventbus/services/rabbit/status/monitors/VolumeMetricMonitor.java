package pegasus.eventbus.services.rabbit.status.monitors;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pegasus.eventbus.services.rabbit.status.PublisherService;
import dashboard.server.metric.Metric;
import dashboard.server.metric.VolumeMetric;

public abstract class VolumeMetricMonitor implements Monitor {

	final protected Logger LOG = LoggerFactory.getLogger(this.getClass());

	public Metric getMetric() {
		int rate = GetRate();
		VolumeMetric metric = new VolumeMetric();
		metric.setLabel(getLabel());
		metric.setTime(Calendar.getInstance().getTimeInMillis());
		metric.setValue(rate);
		LOG.debug("Sending metric '{}', value: {}." , metric.getLabel(), metric.getValue());
		return metric;
	
	}

	protected int GetRate() {
		try{
			String overview = PublisherService.apiHelper.getOverviewJson();
			Matcher matcher = getRateFinder().matcher(overview);
			if (matcher.find())
				return Integer.parseInt(matcher.group(1));
			else 
				return 0;
		} catch (Exception e) {
			LOG.error("Error getting rate." , e);
			return 0;
		}
	}

	protected abstract String getLabel();
	
	protected Pattern getRateFinder() {
		throw new NotImplementedException("getRateFinder must be implemented if getRate is not overridden.");
	}
}