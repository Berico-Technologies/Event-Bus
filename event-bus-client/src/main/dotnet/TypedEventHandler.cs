using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;


namespace pegasus.eventbus.client
{
    public class TypedEventHandler<TEvent> : IEventHandler where TEvent : class
    {
        private Func<TEvent, EventResult> _handler;
        private IEnumerable<Type> _handledEventTypes;


        public IEnumerable<Type> HandledEventTypes
        {
            get { return _handledEventTypes; }
            set { _handledEventTypes = value; }
        }


        public TypedEventHandler(Func<TEvent, EventResult> handler)
        {
            if (null == handler) throw new ArgumentNullException("handler");

            _handler = handler;
            _handledEventTypes = new Type[] { typeof(TEvent) };
        }


        public EventResult HandleEvent(object ev)
        {
            EventResult result = EventResult.Failed;

            try
            {
                result = _handler(ev as TEvent);
            }
            catch { }

            return result;
        }
    }
}
