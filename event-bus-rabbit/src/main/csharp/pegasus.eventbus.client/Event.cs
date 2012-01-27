using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;

using log4net;
using Newtonsoft.Json;


namespace pegasus.eventbus.client
{
    public class Event : IEvent
    {
        private static readonly ILog LOG = LogManager.GetLogger(typeof(Event));

        [JsonIgnore] public virtual Guid Id
        {
            get
            {
                string guid = this.GetHeader(EventHeaders.ID);

                return string.IsNullOrEmpty(guid) ? Guid.Empty : Guid.Parse(guid);
            }
            set { this.SetHeader(EventHeaders.ID, value.ToString()); }
        }

        [JsonIgnore] public virtual Guid CorrelationId
        {
            get
            {
                string guid = this.GetHeader(EventHeaders.CORRELATION_ID);

                return string.IsNullOrEmpty(guid) ? Guid.Empty : Guid.Parse(guid);
            }
            set { this.SetHeader(EventHeaders.CORRELATION_ID, value.ToString()); }
        }

        [JsonIgnore] public virtual string Topic
        {
            get { return this.GetHeader(EventHeaders.TOPIC); }
            set { this.SetHeader(EventHeaders.TOPIC, value); }
        }

        public virtual IDictionary<string, string> Headers
        {
            get;
            set;
        }


        public Event()
        {
            this.Headers = new Dictionary<string, string>();
        }


        public virtual byte[] Serialize()
        {
            StringBuilder buffer = new StringBuilder();

            using (StringWriter writer = new StringWriter(buffer))
            {
                using (JsonTextWriter jsonWriter = new JsonTextWriter(writer))
                {
                    JsonSerializer ser = new JsonSerializer();
                    ser.Serialize(jsonWriter, this);

                    jsonWriter.Flush();
                    jsonWriter.Close();
                }

                writer.Close();
            }

            LOG.DebugFormat("Serialized event {0}: {1}", this, buffer.ToString());

            return new UTF8Encoding().GetBytes(buffer.ToString());
        }


        protected virtual string GetHeader(string key)
        {
            string header = null;

            if (this.Headers.ContainsKey(key))
            {
                header = this.Headers[key];
            }

            return header;
        }

        protected virtual void SetHeader(string key, string value)
        {
            if (this.Headers.ContainsKey(key))
            {
                this.Headers[key] = value;
            }
            else
            {
                this.Headers.Add(key, value);
            }
        }
    }
}
