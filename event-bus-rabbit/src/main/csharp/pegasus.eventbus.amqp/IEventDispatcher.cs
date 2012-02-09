using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using pegasus.eventbus.client;


namespace pegasus.eventbus.amqp
{
    public interface IEventDispatcher : IDisposable
    {
        void AddSubscription(IEventSubscription sub);

        void RemoveSubscription(IEventSubscription sub);


        void AddInterceptor(IEventInterceptor intercept);

        void RemoveInterceptor(IEventInterceptor intercept);


        void Dispatch(AmqpEnvelope env);
    }
}
