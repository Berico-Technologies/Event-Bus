using System;

namespace pegasus.eventbus.client
{
	public interface IEventHandler<TEvent>
	{
		TEvent[] GetHandledEventTypes();
		
		EventResult HandleEvent(TEvent ev);
	}
}

