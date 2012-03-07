package pegasus.eventbus.services.rabbit.status.monitors.volume;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import dashboard.server.metric.VolumeMetric;

import pegasus.eventbus.services.rabbit.status.PublisherService;
import pegasus.eventbus.services.rabbit.status.RabbitManagementApiHelper;

public class VolumeMetricMonitorTest {

	@Mock
	private RabbitManagementApiHelper apiHelper;

	private VolumeMetric metric;

	@Before
	public void beforeEachTest() {

		MockitoAnnotations.initMocks(this);

		PublisherService.apiHelper = apiHelper;


	}

	@Test
	public void PublishedMessagesPerSecondMonitorForAnActiveServerShouldReturn_publish_details_rate() {
		when(apiHelper.getOverviewJson()).thenReturn(jsonForActiveServer);
		metric = (VolumeMetric) new PublishedMessagesPerSecondMonitor().getMetric();
		assertEquals(15, metric.getValue());
	}
	
	@Test
	public void PublishedMessagesPerSecondMonitorForAnIdleServerShouldReturnZero() {
		when(apiHelper.getOverviewJson()).thenReturn(jsonForIdleServer);
		metric = (VolumeMetric) new PublishedMessagesPerSecondMonitor().getMetric();
		assertEquals(0, metric.getValue());
	}

	@Test
	public void DeliveredMessagesPerSecondMonitorForAnActiveServerShouldReturn_deliver_get_details_rate() {
		when(apiHelper.getOverviewJson()).thenReturn(jsonForActiveServer);
		metric = (VolumeMetric) new DeliveredMessagesPerSecondMonitor().getMetric();
		assertEquals(12, metric.getValue());
	}

	@Test
	public void DeliveredMessagesPerSecondMonitorForAnIdleServerShouldReturnZero() {
		when(apiHelper.getOverviewJson()).thenReturn(jsonForIdleServer);
		metric = (VolumeMetric) new DeliveredMessagesPerSecondMonitor().getMetric();
		assertEquals(0, metric.getValue());
	}

	@Test
	public void TotalMessagesMonitorForAnActiveServerShouldReturn_queue_totals_messages() {
		when(apiHelper.getOverviewJson()).thenReturn(jsonForActiveServer);
		metric = (VolumeMetric) new TotalMessagesMonitor().getMetric();
		assertEquals(45, metric.getValue());
	}

	@Test
	public void TotalMessagesMonitorForAnIdleServerShouldReturnZero() {
		when(apiHelper.getOverviewJson()).thenReturn(jsonForIdleServer);
		metric = (VolumeMetric) new TotalMessagesMonitor().getMetric();
		assertEquals(0, metric.getValue());
	}

	@Test
	public void QueuedMessagesMonitorForAnActiveServerShouldReturn_queue_totals_messages_ready() {
		when(apiHelper.getOverviewJson()).thenReturn(jsonForActiveServer);
		metric = (VolumeMetric) new QueuedMessagesMonitor().getMetric();
		assertEquals(34, metric.getValue());
	}

	@Test
	public void QueuedMessagesMonitorForAnIdleServerShouldReturnZero() {
		when(apiHelper.getOverviewJson()).thenReturn(jsonForIdleServer);
		metric = (VolumeMetric) new QueuedMessagesMonitor().getMetric();
		assertEquals(0, metric.getValue());
	}

	@Test
	public void UnacknowledgedMessagesMonitorForAnActiveServerShouldReturn_queue_totals_messages_unacknowledged() {
		when(apiHelper.getOverviewJson()).thenReturn(jsonForActiveServer);
		metric = (VolumeMetric) new UnacknowledgedMessagesMonitor().getMetric();
		assertEquals(23, metric.getValue());
	}

	@Test
	public void UnacknowledgedMessagesMonitorForAnIdleServerShouldReturnZero() {
		when(apiHelper.getOverviewJson()).thenReturn(jsonForIdleServer);
		metric = (VolumeMetric) new UnacknowledgedMessagesMonitor().getMetric();
		assertEquals(0, metric.getValue());
	}

	private static final String jsonForActiveServer = "{\"management_version\":\"2.7.1\",\"statistics_level\":\"fine\",\"message_stats\":{\"publish\":143,\"publish_details\":{\"rate\":15.998225589036561,\"interval\":181572709,\"last_event\":1330775162496},\"ack\":143,\"ack_details\":{\"rate\":9.19633985673702,\"interval\":182169647,\"last_event\":1330775162608},\"deliver\":143,\"deliver_details\":{\"rate\":9.19633985673702,\"interval\":182169647,\"last_event\":1330775162608},\"deliver_get\":143,\"deliver_get_details\":{\"rate\":12.19633985673702,\"interval\":182169647,\"last_event\":1330775162608}},\"queue_totals\":{\"messages\":45,\"messages_ready\":34,\"messages_unacknowledged\":23,\"messages_details\":{\"rate\":0.0,\"interval\":181525218,\"last_event\":1330775162608},\"messages_ready_details\":{\"rate\":0.0,\"interval\":181525218,\"last_event\":1330775162608},\"messages_unacknowledged_details\":{\"rate\":0.0,\"interval\":181525218,\"last_event\":1330775162608}},\"node\":\"rabbit@Kenneths-MacBook-Pro\",\"statistics_db_node\":\"rabbit@Kenneths-MacBook-Pro\",\"listeners\":[{\"node\":\"rabbit@Kenneths-MacBook-Pro\",\"protocol\":\"amqp\",\"ip_address\":\"0.0.0.0\",\"port\":5672},{\"node\":\"rabbit@Kenneths-MacBook-Pro\",\"protocol\":\"amqp\",\"ip_address\":\"::\",\"port\":5672},{\"node\":\"rabbit@Kenneths-MacBook-Pro\",\"protocol\":\"amqp/ssl\",\"ip_address\":\"0.0.0.0\",\"port\":5671},{\"node\":\"rabbit@Kenneths-MacBook-Pro\",\"protocol\":\"amqp/ssl\",\"ip_address\":\"::\",\"port\":5671}],\"contexts\":[{\"node\":\"rabbit@Kenneths-MacBook-Pro\",\"description\":\"RabbitMQ Management\",\"path\":\"/\",\"port\":55672}]}";
	private static final String jsonForIdleServer = "{\"management_version\":\"2.7.1\",\"statistics_level\":\"fine\",\"message_stats\":[],\"queue_totals\":{\"messages\":0,\"messages_ready\":0,\"messages_unacknowledged\":0,\"messages_details\":{\"rate\":0,\"interval\":0,\"last_event\":0},\"messages_ready_details\":{\"rate\":0,\"interval\":0,\"last_event\":0},\"messages_unacknowledged_details\":{\"rate\":0,\"interval\":0,\"last_event\":0}},\"node\":\"rabbit@Kenneths-MacBook-Pro\",\"statistics_db_node\":\"rabbit@Kenneths-MacBook-Pro\",\"listeners\":[{\"node\":\"rabbit@Kenneths-MacBook-Pro\",\"protocol\":\"amqp\",\"ip_address\":\"0.0.0.0\",\"port\":5672},{\"node\":\"rabbit@Kenneths-MacBook-Pro\",\"protocol\":\"amqp\",\"ip_address\":\"::\",\"port\":5672},{\"node\":\"rabbit@Kenneths-MacBook-Pro\",\"protocol\":\"amqp/ssl\",\"ip_address\":\"0.0.0.0\",\"port\":5671},{\"node\":\"rabbit@Kenneths-MacBook-Pro\",\"protocol\":\"amqp/ssl\",\"ip_address\":\"::\",\"port\":5671}],\"contexts\":[{\"node\":\"rabbit@Kenneths-MacBook-Pro\",\"description\":\"RabbitMQ Management\",\"path\":\"/\",\"port\":55672}]}";
}
