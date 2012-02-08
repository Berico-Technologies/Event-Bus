using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using pegasus.eventbus.client;

namespace pegasus.eventbus.amqp
{
    public interface IEventInterceptor : IEventSubscription
    {
        IEvent Request { get; }

        TimeSpan Timeout { get; }

        IEvent Response { get; set; }


        bool Intercepts(IEvent possibleResponse);

        void WaitForResponse();

        void StopWaiting();
    }
}
