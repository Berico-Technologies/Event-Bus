package pegasus.eventbus.amqp;

import pegasus.eventbus.client.Envelope;

public interface AmqpMessageBus {
	
	void createExchange(RoutingInfo.Exchange exchange);
	void createQueue(String name, RoutingInfo[] bindings, boolean durable);
	void deleteQueue(String queueName);
	
	void publish(RoutingInfo route, Envelope message);
	
	UnacceptedMessage getNextMessageFrom(String queueName);
	
	void rejectMessage(UnacceptedMessage message, boolean redeliverMessageLater);
	void acceptMessage(UnacceptedMessage message);

	void close();
	
	public static class UnacceptedMessage{
		private final Envelope envelope;
		private final long acknowledgementToken;

		public UnacceptedMessage(Envelope envelope, long acknowledgementToken) {
			super();
			this.envelope = envelope;
			this.acknowledgementToken = acknowledgementToken;
		}

		public Envelope getEnvelope() {
			return envelope;
		}

		public long getAcceptanceToken() {
			return acknowledgementToken;
		}
	}
}
