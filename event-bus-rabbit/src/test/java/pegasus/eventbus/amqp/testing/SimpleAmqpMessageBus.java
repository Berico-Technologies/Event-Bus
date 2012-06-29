package pegasus.eventbus.amqp.testing;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import pegasus.eventbus.amqp.AmqpMessageBus;
import pegasus.eventbus.amqp.RoutingInfo;
import pegasus.eventbus.amqp.RoutingInfo.Exchange;
import pegasus.eventbus.client.Envelope;
import pegasus.eventbus.client.EnvelopeHandler;
import pegasus.eventbus.client.EventResult;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class SimpleAmqpMessageBus implements AmqpMessageBus {

    /** How long to wait before seeing if there are any more messages to be processed? */
    private static final int CONSUMER_MESSAGE_CHECK_INTERVAL_MILLIS = 1000;

    public class MessageQueue {

        private String name;
        private RoutingInfo[] bindings;
        private ArrayList<Envelope> queue;
        boolean enabled = true;

        public MessageQueue(String name, RoutingInfo[] bindings) {
            this.name = name;
            this.bindings = bindings;
            this.queue = Lists.newArrayList();
        }

        public void addMessageCopy(Envelope message) {
            queue.add(deepCopy(message));
        }

        public Envelope deepCopy(Envelope message) {
            Envelope nmessage = new Envelope();
            nmessage.setBody(message.getBody().clone());
            nmessage.setCorrelationId(message.getCorrelationId());
            nmessage.setEventType(message.getEventType());
            nmessage.getHeaders().putAll(message.getHeaders());
            nmessage.setId(message.getId());
            nmessage.setReplyTo(message.getReplyTo());
            nmessage.setTimestamp(new Date(message.getTimestamp().getTime()));
            nmessage.setTopic(message.getTopic());
            return nmessage;
        }

        public boolean handles(RoutingInfo route) {
            for (RoutingInfo binding : bindings) {
                if (route.equals(binding)) { return true; }
            }
            return false;
        }

        private synchronized Envelope getNextEnvelope() {
            if (! queue.isEmpty()) {
                return queue.remove(0);
            }
            return null;
        }

        private synchronized void addEnvelope(Envelope e) {
            queue.add(e);
        }

        public void disable() {
            queue.clear();
            enabled = false;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public boolean hasMessages() {
            return !queue.isEmpty();
        }
    }

    private boolean busIsActive = false;
    private ArrayList<UnexpectedConnectionCloseListener> uccls = Lists.newArrayList();
    private Map<String, Exchange> allExchanges = Maps.newHashMap();
    private Map<String, MessageQueue> allQueues = Maps.newHashMap();
    private Map<String, Thread> activeThreads = Maps.newHashMap();

    @Override
    public void start() {
        if (busIsActive) {
            throw new IllegalStateException("Starting an already active message bus.");
        }
        busIsActive = true;
    }

    @Override
    public void close() {
        checkStatus("close");
        for (String consumerTag : activeThreads.keySet()) {
            stopConsumingMessages(consumerTag);
        }
        for (String queueName : allQueues.keySet()) {
            allQueues.remove(queueName);
        }
        busIsActive = false;
    }

    private void checkStatus(String action) {
        if (!busIsActive) {
            throw new IllegalStateException("Attempting to " + action + " on an inactive message bus.");
        }
    }

    @Override
    public void attachUnexpectedConnectionCloseListener(UnexpectedConnectionCloseListener listener) {
        checkStatus("attachUnexpectedConnectionCloseListener");
        if (uccls.contains(listener)) {
            return; //ignored per comments in definition
        }
        uccls.add(listener);
    }

    @Override
    public void detachUnexpectedConnectionCloseListener(UnexpectedConnectionCloseListener listener) {
        checkStatus("detachUnexpectedConnectionCloseListener");
        if (!uccls.contains(listener)) {
            return; //ignored per comments in definition
        }
        uccls.remove(listener);
    }

    private <T> void addToMap(Map<String, T> map, String name, T item) {
        if (map.get(name) != null) {
            throw new IllegalStateException("There is already a " +
        item.getClass().getSimpleName() + " named " + name);
        }
        map.put(name, item);
    }


    private <T> void removeFromMap(Map<String, T> map, String name) {
        if (map.get(name) == null) {
            throw new IllegalStateException("There is no item named " + name);
        }
        map.remove(name);
    }


    @Override
    public void createExchange(Exchange exchange) {
        checkStatus("createExchange");
        addToMap(allExchanges, exchange.getName(), exchange);
    }


    //+ The createQueue method should setup some manner of in memory queue
    //+ and record the routine info for which message should be routed to
    //+ it. Delete queue should delete such queues and any messages
    //+ therein. Calling createQueue multiple times for the same queue name
    //+ (without an interim call to deleteQueue) is acceptable. In the case
    //+ where the queue already exists the subsequent call is ignored.
    @Override
    public void createQueue(String name, RoutingInfo[] bindings, boolean durable) {
        checkStatus("createQueue");
        if (!allQueues.containsKey(name)) {
            allQueues.put(name, new MessageQueue(name, bindings));
        }
    }

    @Override
    public void deleteQueue(String name) {
        checkStatus("deleteQueue");
        if (allQueues.containsKey(name)) {
            MessageQueue messageQueue = allQueues.get(name);
            messageQueue.disable();
            removeFromMap(allQueues, name);
        }
    }

    @Override
    public void publish(RoutingInfo route, Envelope message) {
        checkStatus("publish");
        for (MessageQueue queue : findMatchingQueues(route)) {
            queue.addMessageCopy(message);
        }
    }

    private List<MessageQueue> findMatchingQueues(RoutingInfo route) {
        List<MessageQueue> res = Lists.newArrayList();
        for (MessageQueue queue : allQueues.values()) {
            if (queue.handles(route)) {
                res.add(queue);
            }
        }
        return res;
    }

    @Override
    public String beginConsumingMessages(String queueName, final EnvelopeHandler consumer) {
        checkStatus("beginConsumingMessages");
        final MessageQueue messageQueue = allQueues.get(queueName);
        if (messageQueue == null) {
            throw new IllegalStateException("There is no queue named " + queueName);
        }
        final String consumerTag = UUID.randomUUID().toString();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (messageQueue.isEnabled()) {
                    HashSet<Envelope> retried = Sets.newHashSet();
                    while (messageQueue.hasMessages()) {
                        if (! activeThreads.containsKey(consumerTag)) return;
                        Envelope e = messageQueue.getNextEnvelope();
                        if (e != null) {
                            // If we've already retried this message during this cycle,
                            // then add it back to the queue for some other consumer
                            // Furthermore, we went through all the messages available at the
                            // start, so we should end this cycle
                            if (retried.contains(e)) {
                                messageQueue.addEnvelope(e);
                                continue;
                            } else if (consumer.handleEnvelope(e) == EventResult.Retry) {
                                // any envelope that couldn't be handled by the consumer should
                                // be added back the the queue and recorded so we don't try it
                                // again with this consumer at this time.
                                retried.add(e);
                                messageQueue.addEnvelope(e);
                            }
                        }
                    }
                    try { 
                        Thread.sleep(CONSUMER_MESSAGE_CHECK_INTERVAL_MILLIS);
                    } catch (InterruptedException e1) {}
                }
            }
        });
        activeThreads.put(consumerTag, thread);
        thread.start();
        return consumerTag;
    }

    @Override
    public void stopConsumingMessages(String consumerTag) {
        checkStatus("stopConsumingMessages");
        // Remove this tag from the active threads so that the next time the thread checks,
        // it will gracefully exit
        activeThreads.remove(consumerTag);
    }

    //+ Moreover the implementation should include a new method that permits
    //+ simulating closed connections. This test method should accept the
    //+ same boolean argument that is passed to the listener. Invoking this
    //+ method should have the "side effect" of calling
    //+ stopConsumingMessages on all active consumers, before any registered
    //+ listeners are invoked. If the boolean argument is "false" (the
    //+ connection was not successfully reopened), then the further side
    //+ effect of setting the state of the impel to closed should occur,
    //+ such that invoking other methods will now cause an
    //+ IllegalStateException.
    // Question: Does a lost connection that is reopened really lose all consumers unless that created a UCCL? 
    public void connectionWasLost(boolean reopened) {
        for (String tag : activeThreads.keySet()) {
            stopConsumingMessages(tag);
        }
        
        for (UnexpectedConnectionCloseListener listener : uccls) {
            listener.onUnexpectedConnectionClose(reopened);
        }

        if (! reopened) {
            busIsActive = false;
        }
    }

    //+ A suite of automated test that assert the behaviors described herein
    //+ should be created.

    //+ All code, both implementations and tests should be created in the
    //+ test code branch of the project (not the main branch).

    //+ I believe that once this implementation is complete the following
    //+ tests should be executable against it with success:
    //+ pegasus.eventbus.integration_tests.RpcTest and
    //+ pegasus.eventbus.integration_tests.SubscriptionTest

    //+ However these tests inherit from a base class that wires up an event
    //+ manager to the real event bus and does other things that require
    //+ actual connectivity. Something will need to be done to either copy
    //+ the tests to new classes that use a new base class that wires up the
    //+ test harness impl. This should be done and a new context.xml file
    //+ demonstrating how to wire up the test harness version should be
    //+ created and used by it.
}
