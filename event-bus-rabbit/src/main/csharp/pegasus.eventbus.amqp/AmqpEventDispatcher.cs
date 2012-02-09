using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;

using log4net;

using pegasus.eventbus.client;


namespace pegasus.eventbus.amqp
{
    public class AmqpEventDispatcher : IEventDispatcher, IDisposable
    {
        private static ILog LOG = LogManager.GetLogger(typeof(AmqpEventDispatcher));

        private Queue<AmqpEnvelope> _eventQueue;
        private object _queueLock = new object();
        private IDictionary<string, IList<IEventSubscription>> _subscriptions;
        private IDictionary<string, IList<IEventInterceptor>> _interceptors;
        private IList<IEventSubscription> _wiretaps;
        private bool _isDisposing;


        public AmqpEventDispatcher()
        {
            _eventQueue = new Queue<AmqpEnvelope>();
            _subscriptions = new Dictionary<string, IList<IEventSubscription>>();
            _interceptors = new Dictionary<string, IList<IEventInterceptor>>();
            _wiretaps = new List<IEventSubscription>();

            _isDisposing = false;

            Thread dispatchThread = new Thread(this.DispatchMessages);
            dispatchThread.Name = "Dispatcher Thread";
            dispatchThread.Start();
        }


        public void AddSubscription(IEventSubscription sub)
        {
            // if the subscription is a wiretap, add it to a different list
            if (sub.IsWiretap()) { _wiretaps.Add(sub); return; }

            lock (_subscriptions)
            {
                if (false == _subscriptions.ContainsKey(sub.Topic))
                {
                    _subscriptions.Add(sub.Topic, new List<IEventSubscription>());
                }

                _subscriptions[sub.Topic].Add(sub);
            }
        }

        public void RemoveSubscription(IEventSubscription sub)
        {
            // if the subscription is a wiretap, remove it from a different list
            if (sub.IsWiretap()) { _wiretaps.Remove(sub); return; }

            lock (_subscriptions)
            {
                if (_subscriptions.ContainsKey(sub.Topic))
                {
                    if (_subscriptions[sub.Topic].Contains(sub))
                    {
                        _subscriptions[sub.Topic].Remove(sub);
                    }
                }
            }
        }

        public void AddInterceptor(IEventInterceptor intercept)
        {
            lock (_interceptors)
            {
                if (false == _interceptors.ContainsKey(intercept.Topic))
                {
                    _interceptors.Add(intercept.Topic, new List<IEventInterceptor>());
                }

                _interceptors[intercept.Topic].Add(intercept);
            }
        }

        public void RemoveInterceptor(IEventInterceptor intercept)
        {
            lock (_interceptors)
            {
                if (_interceptors.ContainsKey(intercept.Topic))
                {
                    if (_interceptors[intercept.Topic].Contains(intercept))
                    {
                        _interceptors[intercept.Topic].Remove(intercept);
                    }
                }
            }
        }

        public void Dispatch(AmqpEnvelope env)
        {
            LOG.DebugFormat("Was given an envelope for dispatch. {0}", env);

            lock (_queueLock)
            {
                _eventQueue.Enqueue(env);
            }
        }

        public void Dispose()
        {
            _isDisposing = true;
        }


        protected void DispatchMessages()
        {
            LOG.Debug("The dispatcher will now begin dispatching events");

            int count = 0;
            AmqpEnvelope nextEnv = null;

            while (!_isDisposing)
            {
                lock (_queueLock)
                {
                    count = _eventQueue.Count;

                    if (count > 0)
                    {
                        try { nextEnv = _eventQueue.Dequeue(); }
                        catch (Exception ex) { LOG.Error(ex); continue; }
                    }
                }

                if (0 == count)
                {
                    // avoid a hard-loop
                    Thread.Sleep(100);
                }
                else
                {
                    this.Deliver(nextEnv);
                }
            }

            LOG.Debug("The dispatcher is disposing -- will continue delivering events already queued");

            // even though our program is ending, we want to make sure 
            // that any queued messages get processed.
            while (_eventQueue.Count > 0)
            {
                this.Deliver(_eventQueue.Dequeue());
            }

            LOG.Debug("The dispatcher has delivered all events.  Shutting down.");
        }

        protected void Deliver(AmqpEnvelope env)
        {
            try
            {
                LOG.DebugFormat("Now delivering to client(s): {0}", env);

                IEvent message = env.Open();

                // always check for interceptors first.  They're on a schedule, man.
                if (this.DoesNotIntercept(message))
                {
                    IEnumerable<IEventSubscription> subs = _subscriptions.For(message);

                    foreach (IEventSubscription sub in subs)
                    {
                        try
                        {
                            sub.Handler(message);
                        }
                        catch (Exception ex)
                        {
                            LOG.Error(string.Format("{0}{1}{2}",
                                "The event dispatcher caught an unhandled exception thrown by a client's event handler.  ",
                                "The event dispatcher is fine and message processing will continue, ",
                                "but the client that wanted to handle the event may not have done so."
                                ), ex);
                        }
                    }
                }

                // now, deliver the event to any wiretappers - even though it may have been
                // intercepted as a response for a request
                foreach (IEventSubscription sub in _wiretaps)
                {
                    try
                    {
                        sub.Handler(message);
                    }
                    catch (Exception ex)
                    {
                        LOG.Error(string.Format("{0}{1}{2}",
                            "The event dispatcher caught an unhandled exception thrown by a client's event handler.  ",
                            "The event dispatcher is fine and message processing will continue, ",
                            "but the client that wanted to handle the event may not have done so."
                            ), ex);
                    }
                }
            }
            catch (Exception ex)
            {
                LOG.Error(string.Format("{0}{1}{2}",
                    "The event dispatcher encountered an exception while trying to open an envelope and ",
                    "deliver it to subscribed clients.  The event dispatcher is fine and message processing ",
                    "will continue, but any clients wanting to handle the event will not get it.")
                    , ex);
            }
            finally
            {
                env.MarkAsProcessed();
            }
        }

        protected bool DoesNotIntercept(IEvent ev)
        {
            bool wasNotIntercepted = true;
            IEventInterceptor interceptor = _interceptors.For(ev);

            if (null != interceptor)
            {
                // we're intercepting it
                wasNotIntercepted = false;

                try
                {
                    interceptor.Handler(ev);

                    LOG.DebugFormat("Event {0}#{1} was intercepted for request {2}:{3}",
                        ev.Topic, ev.Id, interceptor.Request.Topic, interceptor.Request.Id);
                }
                catch (Exception ex)
                {
                    LOG.Error(string.Format("{0}{1}",
                        "The event dispatcher caught an unhandled exception thrown by an interceptor.  ",
                        "The event dispatcher is fine and message processing will continue, but the client ",
                        "that was expecting a response to its request is not going to receive it.")
                        , ex);
                }
            }

            return wasNotIntercepted;
        }
    }




    public static class EventDispatcherExtensions
    {
        public static IEnumerable<IEventSubscription> For(this IDictionary<string, IList<IEventSubscription>> subs, IEvent ev)
        {
            IEnumerable<IEventSubscription> applicable = null;

            if (subs.ContainsKey(ev.Topic))
            {
                applicable = subs[ev.Topic];
            }
            else
            {
                applicable = new List<IEventSubscription>();
            }

            return subs[ev.Topic];
        }

        public static IEventInterceptor For(this IDictionary<string, IList<IEventInterceptor>> interceptors, IEvent ev)
        {
            IEventInterceptor applicable = null;

            if (interceptors.ContainsKey(ev.Topic))
            {
                applicable = interceptors[ev.Topic].SingleOrDefault(i => Guid.Equals(ev.CorrelationId, i.Request.Id));
            }

            return applicable;
        }
    }
}
