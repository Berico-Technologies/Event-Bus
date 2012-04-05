using System;
using System.Net;
using System.Net.NetworkInformation;
using System.Reflection;
using System.Text.RegularExpressions;

using log4net;

using pegasus.eventbus.client;


namespace pegasus.eventbus.amqp
{
	/// <summary>
	/// Container for all the nasty settings and providers necessary to make the AmqpEventManager work. We 
	/// recommend using the default configuration, accessed by the static functions "getDefault", which simplifies
	/// </summary>
	public class AmqpConfiguration : EventBusConfiguration
	{
		private static readonly ILog LOG = LogManager.GetLogger(typeof(AmqpConfiguration));
		
		// Must start with Alpha, Digit or _ and be no more than 255 chars. Special
		// chars, spaces, etc. are allowed.
		// We are limiting name to 215 chars to allow us to append UUID.
		private static readonly Regex VALID_AMQP_NAME = new Regex("^\\w{1}.{0,214}+$");
		
		// AMQP name may not start with amq. as this is reserved
		private static readonly Regex FIRST_CHARS_INVALID_FOR_AMQP = new Regex("^(\\W|(amq\\.))");
    	
		// Assumes command is anything prior to the first whitespace and then
		// extracts the final ., / or \ delimited segment thereof
		// however . appearing within the final 8 characters of command are included
		// in command as a presumed extension.
		private static readonly Regex NAME_FROM_COMMAND = new Regex(
	    	"((?:^([^\\s./\\\\]+?(?:\\.[^\\s./\\\\]{0,7})*?))|((?:(?:^\\S*?[./\\\\])|^)([^\\s./\\\\]+?(?:\\.[^\\s./\\\\]{0,7})*?)))(?:\\s|$)");
    	
    	
    	public static AmqpConfiguration GetDefault(string clientName)
    	{
    	}
    	
    	public static AmqpConfiguration GetDefault(string clientName, AmqpConnectionParameters connectionParameters)
		{
			RabbitConnection rabbitConnection = new RabbitConnection(connectionParameters);
			AmqpMessageBus amqpMessageBus = new RabbitMessageBus(rabbitConnection);
			CompositeTopologyManager compositeTopologyManager = new CompositeTopologyManager();
			TopologyManager fixedTopologyManager = new StaticTopologyManager();
			compositeTopologyManager.addManager(fixedTopologyManager);
			//TODO: Make the heartbeat interval configurable?
			TopologyManager globalTopologyService = new GlobalTopologyServiceManager(clientName, 300);
			compositeTopologyManager.addManager(globalTopologyService);
			TopologyManager fallbackToplogyService = new FallbackTopologyManager();
			compositeTopologyManager.addManager(fallbackToplogyService);
			Serializer serializer = new GsonSerializer();

			AmqpConfiguration defaultConfiguration = new AmqpConfiguration();
			defaultConfiguration.setClientName(clientName);
			defaultConfiguration.setConnectionParameters(connectionParameters);
			defaultConfiguration.setAmqpMessageBus(amqpMessageBus);
			defaultConfiguration.setTopologyManager(compositeTopologyManager);
			defaultConfiguration.setSerializer(serializer);

			return defaultConfiguration;
    	}
    		
    		
    		
		private string _clientName;
		private AmqpConnectionParameters _connectionParameters;
		private IAmqpMessageBus _amqpMessageBus;
		private ITopologyService _topologyManager;
		private IEventSerializer _serializer;
	    
	    
		public string ClientName
		{
			get { return _clientName; }
			set
			{
				_clientName = this.RectifyClientName(value);
			}
		}
		
		public AmqpConnectionParameters ConnectionParameters
		{
			get { return _connectionParameters; }
			set { _connectionParameters = value; }
		}
		
		public IAmqpMessageBus MessageBus
		{
			get { return _amqpMessageBus; }
			set { _amqpMessageBus = value; }
		}
		
		public ITopologyService TopologyService
		{
			get { return _topologyManager; }
			set { _topologyManager = value; }
		}
		
		public IEventSerializer EventSerializer
		{
			get { return _serializer; }
			set { _serializer = value; }
		}
		
		
		private string RectifyClientName(string potentialName)
		{
			string clientName = this.GetFallBackClientNameIfNeeded(potentialName);
			clientName = this.FixNameIfInvalidForAmqp(clientName);

			// Because fixNameIfInvalidForAmqp should fixing any invalid names, the
			// validation method
			// is not really needed any more but we are keeping it in place just in
			// case something
			// gets past it.
			this.ValidateClientName(clientName);
		}
		
		/// <summary>
		/// If client name is null, attempt to pull the host name from the environment or fall back to "UNKNOWN"
		/// </summary>
		/// <returns>
		/// Best client name available
		/// </returns>
		/// <param name='clientName'>
		/// Name of this instance of the AmqpEventManager
		/// </param>
		private string GetFallBackClientNameIfNeeded(string potentialName)
		{
			LOG.DebugFormat("Attempting to grab the correct client name; one provided = [{0}]", potentialName);
			
			string clientName = potentialName;

			if (string.IsNullOrWhiteSpace(clientName))
			{
				LOG.DebugFormat("Invalid client name. Attempting to pull from Environment.");

				// Try to get name from what command was run to start this process.
				clientName = Assembly.GetEntryAssembly().GetName().Name;
			}
	
			if (string.IsNullOrWhiteSpace(clientName))
			{
				LOG.DebugFormat("Could not find client name in environment, pulling hostname of computer instead.");
	
				// Try to use computer name as client name.
				try
				{
					clientName = this.GetLocalhostFqdn();
	
				} catch (Exception ex)
				{
	
					LOG.Error("Could not find the hostname. Resorting to 'UNKNOWN'.", ex);
	
					clientName = "UNKNOWN";
				}
			}
	
			LOG.DebugFormat("Final client name is [{0}]", clientName);
	
			return clientName;
		}
		
		private string GetLocalhostFqdn()
		{
			var ipProperties = IPGlobalProperties.GetIPGlobalProperties();
			string fqdn = string.Format("{0}.{1}", ipProperties.HostName, ipProperties.DomainName);
			fqdn = fqdn.Replace(".(none)", string.Empty);
			
			return fqdn;
		}
		
		private string FixNameIfInvalidForAmqp(string potentialName)
		{
			LOG.Debug("Normalizing Client Name.");

			string clientName = potentialName.Trim();

			int length = clientName.Length;
			if (length > 214)
			{
				clientName = clientName.Substring(length - 215, length - 1);
			}
			
			if (AmqpConfiguration.FIRST_CHARS_INVALID_FOR_AMQP.IsMatch(clientName))
			{
				clientName = clientName.Insert(0, "_");
			}

			LOG.DebugFormat("Normalized Client Name is [{0}]", clientName);
			return clientName;
		}
		
		private void ValidateClientName(string potentialName)
		{
			LOG.Debug("Validating Client Name.");

			if (false == AmqpConfiguration.VALID_AMQP_NAME.IsMatch(potentialName))
			{
				string msg = "The client name must begin with a letter number or underscore and be no more than " +
						"215 characters long.";
				LOG.Error(msg);
				throw new ArgumentException(msg);
			} else if (potentialName.StartsWith("amq."))
			{
				LOG.Error("The client name may not begin with 'amq.' as this is a reserved namespace.");

				throw new ArgumentException("The clientName may not begin with 'amq.' as this is a reserved namespace.");
			}
		}
	}
}