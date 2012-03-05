using System;

namespace pegasus.eventbus.client
{
	public class Envelope
	{
		// actual properties
		public byte[] Body { get; set; }
		public IDictionary<string, string> Headers { get; set; }
		
		// convenience properties that pull from headers
		public Guid Id
		{
			get { return new Guid(this.GetHeader(EventHeaders.ID)); }
			set { this.SetHeader(EventHeaders.ID, value.ToString()); }
		}
			
		
		public Envelope ()
		{
			this.Headers = new Dictionary<string, string>();
		}
		
		
		public string GetHeader(string name)
		{
			string val = null;
			
			if (this.Headers.ContainsKey(name))
			{
				val = this.Headers[name];
			}
			
			return val;
		}
		
		public string SetHeader(string name, string value)
		{
			
		}
	}
}

