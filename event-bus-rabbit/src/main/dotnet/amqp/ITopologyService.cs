using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace pegasus.eventbus.amqp
{
    public interface ITopologyService
    {
        void Start();

        void Close();

        RoutingInfo GetRoutingInfoForEventOfType(Type evType);
    }
}
