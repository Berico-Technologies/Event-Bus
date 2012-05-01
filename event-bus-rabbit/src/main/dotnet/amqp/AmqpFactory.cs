using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using log4net;

using pegasus.eventbus.client;


namespace pegasus.eventbus.amqp
{
    public class AmqpFactory : IEventBusFactory
    {
        private static ILog LOG = LogManager.GetLogger(typeof(AmqpFactory));


        public IEventManager GetEventManager(string clientName, EventBusConnectionParameters connectionParameters)
        {
            LOG.DebugFormat("Getting new instance of IEventManager for {0}", clientName);

            // please, do not think that this doesn't make me want to puke.
            // this is how the java client is implemented, and until we get
            // a chance to refactor, I am just following their lead.
            return new AmqpEventManager(AmqpConfiguration.GetDefault(clientName, (AmqpConnectionParameters)connectionParameters));
        }
    }
}
