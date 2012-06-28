using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using pegasus.eventbus.client;


namespace pegasus.eventbus.amqp
{
    public interface ITopologyService
    {
        void Start(IEventManager eventManager);

        void Close();

        RoutingInfo GetRoutingInfoForEventOfType(Type evType);

        IEnumerable<RoutingInfo> GetRoutingInfoForNamedEventSet(string eventSetName);
    }
}
