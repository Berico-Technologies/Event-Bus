using System;
using System.Collections.Generic;


namespace pegasus.eventbus.client
{
	public class EventBusConnectionParameters
	{
		public static readonly string USERNAME_PROPERTY                 = "event.bus.username";
	    public static readonly string PASSWORD_PROPERTY                 = "event.bus.password";
	    public static readonly string HOST_PROPERTY                     = "event.bus.host";
	    public static readonly string PORT_PROPERTY                     = "event.bus.port";
	    public static readonly string VHOST_PROPERTY                    = "event.bus.vhost";
	    public static readonly string CONNECTION_RETRY_TIMEOUT_PROPERTY = "event.bus.connectionRetryTimeout";


		private IDictionary<string, string> _parameters;


		public string Username
		{
			get { return this[USERNAME_PROPERTY]; }
			set { this[USERNAME_PROPERTY] = value; }
		}

		public string Password
		{
			get { return this[PASSWORD_PROPERTY]; }
			set { this[PASSWORD_PROPERTY] = value; }
		}

		public string Host
		{
			get { return this[HOST_PROPERTY]; }
			set { this[HOST_PROPERTY] = value; }
		}

		public string Port
		{
			get { return this [PORT_PROPERTY]; }
			set { this [PORT_PROPERTY] = value; }
		}

		public string VHost
		{
			get { return this[VHOST_PROPERTY]; }
			set { this[VHOST_PROPERTY] = value; }
		}


		public string this [string key]
		{
			get
			{
				if (_parameters.ContainsKey(key))
				{
					return _parameters [key];
				} else
				{
					return null;
				}
			}
			set
			{
				if (false == _parameters.ContainsKey(key))
				{
					_parameters.Add(key, null);
				}

				_parameters [key] =value;
			}
		}


		public EventBusConnectionParameters()
		{
			_parameters = new Dictionary<string, string>();
		}
	}
}

