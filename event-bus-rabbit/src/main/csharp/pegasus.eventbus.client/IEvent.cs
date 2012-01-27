using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace pegasus.eventbus.client
{
    public interface IEvent
    {
        Guid Id { get; set; }
        Guid CorrelationId { get; set; }
        string Topic { get; set; }
        IDictionary<string, string> Headers { get; set; }

        byte[] Serialize();
    }
}
