using System;
using System.Collections.Generic;
using System.Linq;

using pegasus.eventbus.client;


namespace pegasus.eventbus.amqp
{
	public class AmqpConnectionParameters : EventBusConnectionParameters
	{
		public AmqpConnectionParameters()
		{
		}

		public AmqpConnectionParameters(IDictionary<string, string> parameters)
		{
			parameters
				.ToList<KeyValuePair<string, string>>()
				.ForEach(param => this [param.Key] = param.Value);
		}

		public AmqpConnectionParameters(string parameters)
		{
		}
	}
}

