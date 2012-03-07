using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace pegasus.eventbus.client
{
    public static class EnvelopeExtensions
    {
        public static Guid GetId(this Envelope env)
        {
            return new Guid(env.GetHeader(EventHeaders.ID));
        }

        public static void SetId(this Envelope env, Guid id)
        {
            env.SetHeader(EventHeaders.ID, id.ToString());
        }


        public static Guid GetCorrelationId(this Envelope env)
        {
            return new Guid(env.GetHeader(EventHeaders.CorrelationID));
        }

        public static void SetCorrelationId(this Envelope env, Guid correlationId)
        {
            env.SetHeader(EventHeaders.CorrelationID, correlationId.ToString());
        }


        public static string GetTopic(this Envelope env)
        {
            return env.GetHeader(EventHeaders.Topic);
        }

        public static void SetTopic(this Envelope env, string topic)
        {
            env.SetHeader(EventHeaders.Topic, topic);
        }


        public static string GetEventType(this Envelope env)
        {
            return env.GetHeader(EventHeaders.Type);
        }

        public static void SetEventType(this Envelope env, string type)
        {
            env.SetHeader(EventHeaders.Type, type);
        }


        public static string GetReplyTo(this Envelope env)
        {
            return env.GetHeader(EventHeaders.ReplyTo);
        }

        public static void SetReplyTo(this Envelope env, string replyTo)
        {
            env.SetHeader(EventHeaders.ReplyTo, replyTo);
        }


        public static DateTime GetSendTime(this Envelope env)
        {
            return DateTime.Parse(env.GetHeader(EventHeaders.SendTime));
        }

        public static void SetSendTime(this Envelope env, DateTime sendTime)
        {
            env.SetHeader(EventHeaders.SendTime, sendTime.ToString());
        }
    }
}
