using System;
using System.Collections.Generic;
using System.Linq;
using System.Text.RegularExpressions;

using pegasus.eventbus.client;


namespace pegasus.eventbus.amqp
{
	public class AmqpConnectionParameters : EventBusConnectionParameters
	{
		private static readonly Regex AMQP_URI = new Regex(
			"amqp://(?<username>[^:]+):(?<password>[^@]+)@(?<host>[^:/]+):?(?<port>[^/]*)/?(?<vhost>.*)");


		public AmqpConnectionParameters()
		{
			this.SetDefaultValues();
		}

		public AmqpConnectionParameters(IDictionary<string, string> parameters)
		{
			this.SetDefaultValues();

			parameters
				.ToList<KeyValuePair<string, string>>()
				.ForEach(param => this.Set(param.Key, param.Value));
		}

		public AmqpConnectionParameters(string parameters)
		{
			this.SetDefaultValues();

			Match uri = AMQP_URI.Match(parameters);

			if (uri.Success)
			{
				this.Username = uri.Groups ["username"].Value;
				this.Password = uri.Groups ["password"].Value;
				this.Host = uri.Groups ["host"].Value;
				this.Port = uri.Groups ["port"].Value;
				this.VHost = uri.Groups ["vhost"].Value;
				
			} else
			{
				parameters.Split(';').ToList().ForEach(param =>
				{
					string[] tokens = param.Split('=');
					this.Set(tokens [0], tokens [1]);
				});
			}
		}


		private void SetDefaultValues()
		{
			this.Username = "guest";
			this.Password = "guest";
			this.Host = "rabbit";
			this.Port = "5672";
			this.VHost = string.Empty;

			this [CONNECTION_RETRY_TIMEOUT_PROPERTY] = "30000";
		}

		private void Set(string key, string value)
		{
			switch (key.ToLower())
			{
				case "host":
					this.Host = value;
					break;
				case "username":
					this.Username = value;
					break;
				case "password":
					this.Password = value;
					break;
				case "port":
					this.Port = value;
					break;
				case "vhost":
					this.VHost = value;
					break;
				default:
					throw new ArgumentException("Unknown connection parameter: " + key);
			}
		}
	}
}

