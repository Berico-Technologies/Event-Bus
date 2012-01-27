using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace pegasus.eventbus.client
{
    public struct EventHeaders
    {
        public static readonly string EMPTY_TOPIC = string.Empty;
        public static readonly string ID = "pegasus.eventbus.event.id";
        public static readonly string CORRELATION_ID = "pegasus.eventbus.event.correlation_id";
        public static readonly string TOPIC = "pegasus.eventbus.event.topic";
        public static readonly string EVENT_TYPE = "pegasus.eventbus.event.type";
    }
}
