/**
 * 
 */
package pegasus.esp.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import pegasus.esp.EnvelopeUtils;
import pegasus.esp.EventStreamProcessor;
import pegasus.eventbus.client.Envelope;

/**
 * @author israel
 *
 */
public class osgiActivator implements BundleActivator {

	private EventStreamProcessor eventStreamProcessor;

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		System.out.println("@@@@ " + this.getClass().getCanonicalName() + " starting.");
		Envelope envelope = EnvelopeUtils.makeEnvelope("StartBundle", null, null, "BundleTopic", "OSGI");
		System.out.println("@@@@ " + this.getClass().getCanonicalName() + " built envelope.");
		System.out.println("@@@@ Envelope is " + envelope);
		
		this.eventStreamProcessor = new EventStreamProcessor();
		// TODO: add handler to look for an EventManager and attach it
		// TODO: add handler to look for an EventMonitorRepository and register it
		System.out.println(this.getClass().getCanonicalName() + " started.");
	}

	public EventStreamProcessor getEventStreamProcessor() {
		return eventStreamProcessor;
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		System.out.println(this.getClass().getCanonicalName() + " stopping.");
		eventStreamProcessor.detachFromEventBus();
		eventStreamProcessor = null;
		System.out.println(this.getClass().getCanonicalName() + " stopped.");
	}
}
