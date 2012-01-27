using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;

using log4net;

using pegasus.eventbus.client;


namespace pegasus.eventbus.amqp
{
    public class EventInterceptor : IEventInterceptor
    {
        private static ILog LOG = LogManager.GetLogger(typeof(EventInterceptor));

        private AutoResetEvent _waitHandle;
        

        public IEvent Request { get; set; }

        public TimeSpan Timeout { get; set; }

        public IEvent Response { get; set; }

        public string Topic
        {
            get;
            protected set;
        }

        public Action<IEvent> Handler
        {
            get;
            protected set;
        }


        public EventInterceptor(IEvent request, TimeSpan timeout, string responseTopic)
        {
            this.Request = request;
            this.Timeout = timeout;
            this.Topic = responseTopic;
            this.Handler = this.Intercept;

            _waitHandle = new AutoResetEvent(false);
        }


        public virtual void WaitForResponse()
        {
            if (false == _waitHandle.WaitOne(this.Timeout))
            {
                string message = string.Format(
                    "A response of type {0} for request {1} did not arrive within the specified timeout period."
                    , this.Topic, this.Request.Id);

                LOG.Warn(message);
                throw new TimeoutException(message);
            }
        }

        public virtual void Intercept(IEvent possibleResponse)
        {
            if (false == this.Intercepts(possibleResponse))
            {
                LOG.InfoFormat("Event interceptor was DISPATCHED a possible response.  The event interceptor should not have events dispatched to it.");
            }
        }

        public virtual bool Intercepts(IEvent possibleResponse)
        {
            bool intercepts = false;

            if ((null != possibleResponse) &&
                (Guid.Equals(this.Request.Id, possibleResponse.CorrelationId)))
            {
                intercepts = true;
                this.Response = possibleResponse;

                this.StopWaiting();
            }

            return intercepts;
        }

        public virtual void StopWaiting()
        {
            _waitHandle.Set();
        }
    }
}
