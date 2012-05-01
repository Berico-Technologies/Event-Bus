using System;
using System.Collections.Generic;


namespace pegasus.eventbus.client
{
	public interface IEventHandler
	{
		IEnumerable<Type> HandledEventTypes { get; }
		
		EventResult HandleEvent(object ev);
	}
}

