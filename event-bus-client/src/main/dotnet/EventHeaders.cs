using System;

namespace pegasus.eventbus.client
{
	public static class EventHeaders
	{
		private static readonly string BASE = "pegasus.eventbus.client.event.";
		
		public static readonly string ID = BASE + "id";
		public static readonly string CorrelationID = BASE + "correlationId";
		public static readonly string Topic = BASE + "topic";
		public static readonly string Type = BASE + "type";
		public static readonly string ReplyTo = BASE + "replyTo";
		public static readonly string SendTime = BASE + "sendTime";
	}
}

