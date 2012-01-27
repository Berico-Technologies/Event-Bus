using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace pegasus.eventbus.amqp
{
    public struct AmqpHeaders
    {
        public static readonly string RECEVING_QUEUE = "pegasus.eventbus.amqp.receiving_queue";
        public static readonly string RECEIVE_DATETIME = "pegasus.eventbus.amqp.receive_datetime";
        public static readonly string REPLY_TO_QUEUE = "pegasus.eventbus.amqp.reply_to_queue";
        public static readonly string REPLY_TO_EXCHANGE = "pegasus.eventbus.amqp.reply_to_exchange";
        public static readonly string SEND_DATETIME = "pegasus.eventbus.amqp.send_datetime";
    }
}
