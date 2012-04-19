using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;

using log4net;
using Newtonsoft.Json;

using pegasus.eventbus.amqp;


namespace pegasus.eventbus.gson
{
    public class GsonSerializer : IEventSerializer
    {
        private static readonly ILog LOG = LogManager.GetLogger(typeof(GsonSerializer));


        public byte[] Serialize(object ev)
        {
            if (null == ev) throw new ArgumentNullException("ev");

            byte[] buffer;

            try
            {
                buffer = new UTF8Encoding().GetBytes(JsonConvert.SerializeObject(ev));
            }
            catch(Exception ex)
            {
                LOG.Error("Failed to serialize an event of type " + ev.GetType().FullName, ex);
                throw;
            }

            return buffer;
        }

        public TEvent Deserialize<TEvent>(byte[] rawEvent)
        {
            TEvent ev = default(TEvent);

            try
            {
                ev = JsonConvert.DeserializeObject<TEvent>(new UTF8Encoding().GetString(rawEvent));
            }
            catch (Exception ex)
            {
                LOG.Error("Failed to deserialize an event of type " + typeof(TEvent).FullName, ex);
                throw;
            }

            return ev;
        }
    }
}
