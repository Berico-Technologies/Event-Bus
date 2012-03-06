using System;

namespace pegasus.eventbus.client
{
	public interface IEventHandler
	{
		Type[] GetHandledEventTypes();
		
		EventResult HandleEvent(object ev);
	}
}

