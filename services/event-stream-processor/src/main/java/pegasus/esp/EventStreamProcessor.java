package pegasus.esp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pegasus.eventbus.client.Envelope;
import pegasus.eventbus.client.EnvelopeHandler;
import pegasus.eventbus.client.EventManager;
import pegasus.eventbus.client.EventResult;
import pegasus.eventbus.client.Subscription;
import pegasus.eventbus.client.SubscriptionToken;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;
import com.google.common.annotations.VisibleForTesting;

class EventStreamProcessor {

    private EventManager eventbus;

    private static final Log logger = LogFactory.getLog(EventStreamProcessor.class);

    class EnvelopeListener implements UpdateListener {

        public EnvelopeListener(EventMonitor monitor) {
            super();
            this.monitor = monitor;
        }

        EventMonitor monitor;

        @Override
        public void update(EventBean[] newEvents, EventBean[] oldEvents) {
            try {
                for (EventBean eventBean : newEvents) {
                    logger.debug(Utils.beanString(eventBean) + "==> " + monitor + ".receive(" + ")");
                    processResult(monitor.receive(eventBean));
                }
//                resultingEvent = monitor.handle(newEvents, oldEvents);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

        }

        private void processResult(InferredEvent resultingEvent) {
            if (resultingEvent == null) {
                return;
            }

            EPRuntime epRuntime = epService.getEPRuntime();
            if (resultingEvent instanceof InferredEventList) {
                for (InferredEvent ev : ((InferredEventList) resultingEvent).getList()) {
                    logger.debug(monitor + " SENDS* " + ev);
                    epRuntime.sendEvent(ev);
                }
            } else {
                logger.debug(monitor + " SENDS " + resultingEvent);
                epRuntime.sendEvent(resultingEvent);
            }
        }
    }

    class EventbusListener implements EnvelopeHandler {

        private EventStreamProcessor eventStreamProcessor;

        public EventbusListener(EventStreamProcessor eventStreamProcessor) {
            this.eventStreamProcessor = eventStreamProcessor;
        }

        @Override
        public EventResult handleEnvelope(Envelope envelope) {
            sendEvent(envelope);
            return EventResult.Handled;
        }

		@Override
		public String getEventSetName() {
			return "ALL";
		}

    }

    // TODO: consider making this a constructor parameter to allow for multiple instances
    public static final String engineURI = "EventStreamProcessor";

    private EPServiceProvider epService;

    private SubscriptionToken token;

    private EventMonitorRepository repository;

    public EventStreamProcessor(EventMonitorRepository rep) {
        this.repository = rep;
        epService = createEventProcessor();
        rep.registerWith(this);
    }

    private EPServiceProvider createEventProcessor() {
        Configuration configuration = new Configuration();
        configuration.addEventType("Envelope", Envelope.class);
        configuration.addEventType("InferredEvent", InferredEvent.class);
        EPServiceProvider epService = EPServiceProviderManager.getProvider(engineURI , configuration);
        epService.initialize();
        return epService;
    }

    public void attachToEventBus() {
        EnvelopeHandler envelopeHandler = new EventbusListener(this);
        Subscription subscription = new Subscription(envelopeHandler);
        token = eventbus.subscribe(subscription);
    }

    public void detachFromEventBus() {
        eventbus.unsubscribe(token);
        token = null;
    }

    public void watchFor(EventMonitor monitor) {
        monitor.registerPatterns(this);
    }

    @VisibleForTesting
    public void sendEvent(Envelope envelope) {
        logger.debug(" --> Event: " + envelope);
        epService.getEPRuntime().sendEvent(envelope);

    }

    public void monitor(boolean isEPL, String pattern, EventMonitor monitor) {
        EPAdministrator administrator = epService.getEPAdministrator();
        EPStatement stmt;
        logger.debug("Creating " + (isEPL ? "EPL" : "pattern") + ": " + pattern);
        if (isEPL) {
            stmt = administrator.createEPL(pattern);
        } else {
            stmt = administrator.createPattern(pattern);
        }
        if (monitor != null) {
            stmt.addListener(new EnvelopeListener(monitor));
        }
        if (monitor == null) {
            UpdateListener intermediatePrinter = createIntermediatePrinter(pattern);
            if (intermediatePrinter != null) {
                stmt.addListener(intermediatePrinter);
            }
        }
    }

    private UpdateListener createIntermediatePrinter(final String pattern) {
        boolean watchInt = true;
        if (! watchInt) { return null; }

        String displ = pattern;
        String insert = "insert into ";
        if (pattern.startsWith(insert)) {
            displ = pattern.substring(insert.length()).split("\\W")[0];
        }
        final String fdispl = displ;
        UpdateListener ip = new UpdateListener() {
            @Override
            public void update(EventBean[] newEvents, EventBean[] oldEvents) {
                for (EventBean eventBean : newEvents) {
                    logger.debug("!!!! " + fdispl + ": " + Utils.beanString(eventBean));
                }
            }
        };

        return ip;
    }
}
