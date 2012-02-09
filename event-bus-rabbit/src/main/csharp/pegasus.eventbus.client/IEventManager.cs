using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;


namespace pegasus.eventbus.client
{
    public interface IEventManager : IDisposable
    {
        SubscriptionToken Subscribe(string topic, Action<IEvent> eventHandler);

        SubscriptionToken Subscribe<TEvent>(Action<TEvent> eventHandler) where TEvent : class, IEvent;

        SubscriptionToken Subscribe<TEvent>(string topic, Action<TEvent> eventHandler) where TEvent : class, IEvent;


        void Unsubscribe(SubscriptionToken token);


        void GetAllMessages(Action<IEvent> wiretapHandler);


        void Publish(IEvent message);


        IEvent GetResponseTo(IEvent request, TimeSpan timeout, string responseTopic);

        TResponse GetResponseTo<TResponse>(IEvent request, TimeSpan timeout) where TResponse : class, IEvent;

        TResponse GetResponseTo<TResponse>(IEvent request, TimeSpan timeout, string responseTopic) where TResponse : class, IEvent;

        bool TryGetResponseTo(IEvent request, TimeSpan timeout, string responseTopic, out IEvent response);

        bool TryGetResponseTo<TResponse>(IEvent request, TimeSpan timeout, out TResponse response) where TResponse : class, IEvent;

        bool TryGetResponseTo<TResponse>(IEvent request, TimeSpan timeout, string responseTopic, out TResponse response) where TResponse : class, IEvent;


        SubscriptionToken GetResponsesTo(IEvent request, IEnumerable<string> responseTopics, Action<IEvent> handler);


        void RespondTo(IEvent request, IEvent response);


        void Close();
    }
}
