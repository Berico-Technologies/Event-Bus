using System;
using System.Collections.Generic;
using System.Text;

namespace pegasus.eventbus.client
{
	public class Envelope
	{
		public byte[] Body { get; set; }
		public IDictionary<string, string> Headers { get; set; }
		
		
		public Envelope ()
		{
			this.Headers = new Dictionary<string, string>();
		}
		
		
		public virtual string GetHeader(string name)
		{
			string val = null;
			
			if (this.Headers.ContainsKey(name))
			{
				val = this.Headers[name];
			}
			
			return val;
		}
		
		public virtual void SetHeader(string name, string value)
		{
            if (false == this.Headers.ContainsKey(name))
            {
                this.Headers.Add(name, string.Empty);
            }

            this.Headers[name] = value;
		}

        public override string ToString()
        {
            StringBuilder sb = new StringBuilder();
            sb.Append("[REPLYTO=");
            sb.Append(this.GetReplyTo());
            sb.Append(",EVENT_TYPE=");
            sb.Append(this.GetEventType());
            sb.Append(",TOPIC=");
            sb.Append(this.GetTopic());
            sb.Append(",ID=");
            sb.Append(this.GetId().ToString());
            sb.Append(",CORRELATION_ID=");
            sb.Append(this.GetCorrelationId().ToString());
            sb.Append("]");

            return sb.ToString();
        }
	}
}

