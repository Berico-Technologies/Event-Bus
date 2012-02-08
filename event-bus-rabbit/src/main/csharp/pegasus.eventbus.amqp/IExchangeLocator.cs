using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace pegasus.eventbus.amqp
{
    public interface IExchangeLocator
    {
        IExchange GetExchange(string eventTopic);

        IExchange GetExchangeFromUri(string uri);

        IEnumerable<IExchange> GetAllExchanges();
    }
}
