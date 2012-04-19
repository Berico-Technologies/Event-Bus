using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using log4net;

using pegasus.eventbus.client;
using pegasus.eventbus.amqp;


namespace pegasus.eventbus.topology
{
    public class StaticTopologyManager : ITopologyService
    {
        private static readonly ILog LOG = LogManager.GetLogger(typeof(StaticTopologyManager));
        private static readonly string DEFAULT_TOPOLOGY_EXCHANGE = "topology";

        private string _topologyExchange;
        private IDictionary<string, RoutingInfo> _topologyEventRegistry;
        private IDictionary<string, IEnumerable<RoutingInfo>> _topologyEventSetRegistry;


        public StaticTopologyManager()
        {
            _topologyExchange = DEFAULT_TOPOLOGY_EXCHANGE;
            _topologyEventRegistry = new Dictionary<string, RoutingInfo>();
            _topologyEventSetRegistry = new Dictionary<string, IEnumerable<RoutingInfo>>();

            this.RegisterForEvents();
        }


        public void Start(IEventManager eventManager)
        {
        }

        public void Close()
        {
        }

        public RoutingInfo GetRoutingInfoForEventOfType(Type evType)
        {
            RoutingInfo route = null;

            if (_topologyEventRegistry.ContainsKey(evType.FullName))
            {
                route = _topologyEventRegistry[evType.FullName];
            }

            return route;
        }

        public IEnumerable<RoutingInfo> GetRoutingInfoForNamedEventSet(string eventSetName)
        {
            // the static topo service never adds to the event set dictionary,
            // so instead of looking it up and having it always return null,
            // I'm just going to always return null
            return null;
        }


        private void RegisterForEvents()
        {
            this.RegisterForEvent(typeof(RegisterClient));
            this.RegisterForEvent(typeof(UnregisterClient));
    	    this.RegisterForEvent(typeof(HeartBeat));
      	    this.RegisterForEvent(typeof(TopologyUpdate));
     	    this.RegisterForEvent(typeof(GetEventTypeRoute));
            this.RegisterForEvent(typeof(EventTypeRoutingInfo));
        }

        private void RegisterForEvent(Type eventType)
        {
            _topologyEventRegistry.Add(
                eventType.FullName, 
                new RoutingInfo(_topologyExchange, ExchangeType.Topic, true, eventType.FullName));
        }
    }
}
