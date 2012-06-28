using System;


namespace pegasus.eventbus.client
{
	public interface IEventBusFactory
	{
		IEventManager GetEventManager(
			string clientName,
			EventBusConnectionParameters connectionParameters);
	}
}

