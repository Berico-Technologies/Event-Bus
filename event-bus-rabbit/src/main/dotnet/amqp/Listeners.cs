using System;

using pegasus.eventbus.client;


namespace pegasus.eventbus.amqp
{
	public interface StartListener
	{
		void OnStart();
	}
	
	
	public interface CloseListener
	{
		void OnClose(bool unexpected);
	}
	
	
	public interface SubscribeListener
	{
		void OnSubscribe(SubscriptionToken subscriptionToken);
	}
	
	
	public interface UnsubscribeListener
	{
		void OnUnsubscribe(SubscriptionToken subscriptionToken);
	}
}

