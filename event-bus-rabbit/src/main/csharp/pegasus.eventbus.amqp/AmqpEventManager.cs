using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

using pegasus.eventbus.client;


namespace pegasus.eventbus.amqp
{
    public class AmqpEventManager : IEventManager
    {
        private IQueueManager _queueManager;
        private IEventDispatcher _eventDispatcher;
        private IDictionary<SubscriptionToken, IEventSubscription> _subscriptions;
        private object _subLock = new object();


        public AmqpEventManager(IQueueManager queueManager, IEventDispatcher eventDispatcher)
        {
            _queueManager = queueManager;
            _eventDispatcher = eventDispatcher;
            _subscriptions = new Dictionary<SubscriptionToken, IEventSubscription>();

            _queueManager.EnvelopeReceived += _eventDispatcher.Dispatch;
        }


        public SubscriptionToken Subscribe(string topic, Action<IEvent> eventHandler)
        {
            return this.Subscribe(new EventSubscription(topic, eventHandler));
        }

        public SubscriptionToken Subscribe<TEvent>(Action<TEvent> eventHandler) where TEvent : class, IEvent
        {
            return this.Subscribe(new TypedEventSubscription<TEvent>(eventHandler));
        }

        public SubscriptionToken Subscribe<TEvent>(string topic, Action<TEvent> eventHandler) where TEvent : class, IEvent
        {
            return this.Subscribe(new TypedEventSubscription<TEvent>(topic, eventHandler));
        }


        public void Unsubscribe(SubscriptionToken token)
        {
            lock (_subLock)
            {
                if (_subscriptions.ContainsKey(token))
                {
                    IEventSubscription sub = _subscriptions[token];
                    _subscriptions.Remove(token);

                    _queueManager.RemoveSubscription(sub);
                    _eventDispatcher.RemoveSubscription(sub);
                }
            }
        }


        public void GetAllMessages(Action<IEvent> wiretapHandler)
        {
            WiretapSubscription sub = new WiretapSubscription(wiretapHandler);

            this.Subscribe(sub);
        }

        public void Publish(IEvent message)
        {
            if (null == message) { throw new ArgumentNullException("message"); }

            if (Guid.Empty.Equals(message.Id)) { message.Id = Guid.NewGuid(); }

            _queueManager.Send(message);
        }


        public IEvent GetResponseTo(IEvent request, TimeSpan timeout, string responseTopic)
        {
            return this.GetResponse(request, timeout, responseTopic);
        }

        public TResponse GetResponseTo<TResponse>(IEvent request, TimeSpan timeout) where TResponse : class, IEvent
        {
            return this.GetResponse(request, timeout, typeof(TResponse).FullName) as TResponse;
        }

        public TResponse GetResponseTo<TResponse>(IEvent request, TimeSpan timeout, string responseTopic) where TResponse : class, IEvent
        {
            return this.GetResponse(request, timeout, responseTopic) as TResponse;
        }

        public bool TryGetResponseTo(IEvent request, TimeSpan timeout, string responseTopic, out IEvent response)
        {
            return this.TryGetResponse(request, timeout, responseTopic, out response);
        }

        public bool TryGetResponseTo<TResponse>(IEvent request, TimeSpan timeout, out TResponse response) where TResponse : class, IEvent
        {
            return this.TryGetResponseTo<TResponse>(request, timeout, typeof(TResponse).FullName, out response);
        }

        public bool TryGetResponseTo<TResponse>(IEvent request, TimeSpan timeout, string responseTopic, out TResponse response) where TResponse : class, IEvent
        {
            IEvent temp = null;
            response = null;
            bool gotResponse = this.TryGetResponse(request, timeout, responseTopic, out temp);

            if (gotResponse) { response = temp as TResponse; }
            return gotResponse;
        }


        public SubscriptionToken GetResponsesTo(IEvent request, IEnumerable<string> responseTopics, Action<IEvent> responseHandler)
        {
            throw new NotImplementedException();
        }
        
        public void RespondTo(IEvent request, IEvent response)
        {
            if (null == request) { throw new ArgumentNullException("request"); }
            if (null == response) { throw new ArgumentNullException("response"); }

            if (Guid.Empty.Equals(response.Id)) { response.Id = Guid.NewGuid(); }

            response.CorrelationId = request.Id;

            _queueManager.Send(response);
        }


        public void Close()
        {
            _queueManager.Dispose();
            _eventDispatcher.Dispose();
        }

        public void Dispose()
        {
            this.Close();
        }


        protected virtual SubscriptionToken Subscribe(IEventSubscription sub)
        {
            SubscriptionToken token = new SubscriptionToken()
            {
                Value = sub.GetHashCode(),
                Topic = sub.Topic
            };

            lock (_subLock)
            {
                _subscriptions.Add(token, sub);
                _queueManager.AddSubscription(sub);
                _eventDispatcher.AddSubscription(sub);
            }

            return token;
        }

        protected virtual IEvent GetResponse(IEvent request, TimeSpan timeout, string responseTopic)
        {
            // create an interceptor for the request
            IEventInterceptor interceptor = new EventInterceptor(request, timeout, responseTopic);
            
            // tell the dispatcher to watch for a response to this request
            _eventDispatcher.AddInterceptor(interceptor);

            // subscribe to the response (the interceptor is a subscription)
            _queueManager.AddSubscription(interceptor);

            // send the request
            _queueManager.Send(request);

            // block this thread while we wait for a response.  If the timeout elapses,
            // this will throw a TimeoutException.  
            // No matter what, remove the subscription and the interceptor
            try { interceptor.WaitForResponse(); }
            finally
            {
                _queueManager.RemoveSubscription(interceptor);
                _eventDispatcher.RemoveInterceptor(interceptor);
            }

            // return the intercepted response
            return interceptor.Response;
        }

        protected virtual bool TryGetResponse(IEvent request, TimeSpan timeout, string responseTopic, out IEvent response)
        {
            bool gotResponse = false;

            try
            {
                response = this.GetResponse(request, timeout, responseTopic);
                gotResponse = true;
            }
            catch (TimeoutException)
            {
                response = null;
            }

            return gotResponse;
        }
    }
}
