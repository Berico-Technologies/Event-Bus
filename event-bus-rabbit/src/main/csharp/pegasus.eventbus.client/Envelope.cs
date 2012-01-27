using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Text;

using log4net;
using Newtonsoft.Json;


namespace pegasus.eventbus.client
{
    public delegate void EnvelopeHandler(Envelope env);



    public class Envelope
    {
        private static ILog LOG = LogManager.GetLogger(typeof(Envelope));


        public static Envelope Deserialize(byte[] body)
        {
            Envelope env = null;

            MemoryStream baseStream = new MemoryStream(body);
            JsonSerializer ser = new JsonSerializer();

            using (StreamReader reader = new StreamReader(baseStream, new UTF8Encoding()))
            {
                using (JsonTextReader jsonReader = new JsonTextReader(reader))
                {
                    env = ser.Deserialize<Envelope>(jsonReader);
                    jsonReader.Close();
                }
                reader.Close();
            }

            return env;
        }


        public IDictionary<string, string> Headers
        {
            get;
            protected set;
        }

        public byte[] Body
        {
            get;
            set;
        }

        [JsonIgnore] public Guid Id
        {
            get
            {
                string guid = this.GetHeader(EventHeaders.ID);

                return string.IsNullOrEmpty(guid) ? Guid.Empty : Guid.Parse(guid);
            }
            set { this.SetHeader(EventHeaders.ID, value.ToString()); }
        }

        [JsonIgnore] public Guid CorrelationId
        {
            get
            {
                string guid = this.GetHeader(EventHeaders.CORRELATION_ID);

                return string.IsNullOrEmpty(guid) ? Guid.Empty : Guid.Parse(guid);
            }
            set { this.SetHeader(EventHeaders.CORRELATION_ID, value.ToString()); }
        }

        [JsonIgnore] public string Topic
        {
            get { return this.GetHeader(EventHeaders.TOPIC); }
            set { this.SetHeader(EventHeaders.TOPIC, value); }
        }

        [JsonIgnore] public string EventType
        {
            get { return this.GetHeader(EventHeaders.EVENT_TYPE); }
            set { this.SetHeader(EventHeaders.EVENT_TYPE, value); }
        }


        public Envelope(IEvent message)
        {
            this.Headers = message.Headers;
            this.Body = message.Serialize();
            this.EventType = message.GetType().FullName;

            if (string.IsNullOrEmpty(this.Topic)) { this.Topic = message.GetType().FullName; }
        }

        public Envelope()
        {
            this.Headers = new Dictionary<string, string>();
        }

        protected Envelope(byte[] body)
        {
            Envelope temp = Envelope.Deserialize(body);
            this.Headers = temp.Headers;
            this.Body = temp.Body;
        } 


        public virtual byte[] Serialize()
        {
            JsonSerializer ser = new JsonSerializer();
            StringBuilder buffer = new StringBuilder();

            using (StringWriter writer = new StringWriter(buffer))
            {
                using (JsonTextWriter jsonWriter = new JsonTextWriter(writer))
                {
                    // cast this to envelope because we always want to SEND and RECEIVE
                    // basic envelopes, even though others may inherit from Envelope
                    ser.Serialize(jsonWriter, (Envelope)this);
                    jsonWriter.Flush();
                    jsonWriter.Close();
                }

                writer.Close();
            }

            LOG.DebugFormat("Serialized envelope {0}: {1}", this, buffer.ToString());

            return new UTF8Encoding().GetBytes(buffer.ToString());
        }

        public virtual IEvent Open()
        {
            IEvent typedEvent = null;

            MemoryStream baseStream = new MemoryStream(this.Body);
            JsonSerializer ser = new JsonSerializer();

            using (StreamReader reader = new StreamReader(baseStream, new UTF8Encoding()))
            {
                Type eventType = this.GetEventType(this.EventType);
                LOG.DebugFormat("Event Type {0} found?: {1}", this.EventType, eventType != null);

                typedEvent = JsonConvert.DeserializeObject(reader.ReadToEnd(), eventType) as IEvent;

                reader.Close();
            }

            // give the message the headers that were on the envelope
            typedEvent.Headers = this.Headers;

            return typedEvent;
        }

        public virtual Type GetEventType(string type)
        {
            Type eventType = null;

            foreach (Assembly ass in AppDomain.CurrentDomain.GetAssemblies())
            {
                eventType = ass.GetType(type, false, true);
                if (null != eventType) { break; }
            }

            return eventType;
        }

        public override string ToString()
        {
            return string.Format("{0} --> id:{1} cid:{2}",
                this.Topic, this.Id, this.CorrelationId);
        }


        public virtual string GetHeader(string key)
        {
            string header = null;

            if (this.Headers.ContainsKey(key))
            {
                header = this.Headers[key];
            }

            return header;
        }

        public virtual void SetHeader(string key, string value)
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
