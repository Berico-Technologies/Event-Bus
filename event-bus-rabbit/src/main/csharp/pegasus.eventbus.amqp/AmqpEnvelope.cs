using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;
using pegasus.eventbus.client;


namespace pegasus.eventbus.amqp
{
    public delegate void AmqpEnvelopeHandler(AmqpEnvelope env);



    public class AmqpEnvelope : Envelope
    {
        private AutoResetEvent _waitEvent = new AutoResetEvent(false);


        public AmqpEnvelope(byte[] body)
            : base(body)
        {
        }

        public AmqpEnvelope(IEvent message)
            : base(message)
        {
        }


        public void WaitToBeProcessed()
        {
            _waitEvent.WaitOne();
        }

        public void MarkAsProcessed()
        {
            _waitEvent.Set();
        }
    }
}
