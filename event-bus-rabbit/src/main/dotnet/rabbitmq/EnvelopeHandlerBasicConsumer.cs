using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using log4net;
using RabbitMQ.Client;

using pegasus.eventbus.client;


namespace pegasus.eventbus.rabbitmq
{
    public class EnvelopeHandlerBasicConsumer : DefaultBasicConsumer
    {
        private static readonly ILog LOG = LogManager.GetLogger(typeof(EnvelopeHandlerBasicConsumer));

        private IModel _channel;
        private string _consumerTag;
        private IEnvelopeHandler _consumer;


        public EnvelopeHandlerBasicConsumer(IModel channel, string consumerTag, IEnvelopeHandler consumer)
            : base(channel)
        {
            _channel = channel;
            _consumerTag = consumerTag;
            _consumer = consumer;

            LOG.DebugFormat("Created an envelope-based consumer with tag [{0}].", consumerTag);
        }


        public override void HandleBasicDeliver(
            string consumerTag, 
            ulong deliveryTag, 
            bool redelivered, 
            string exchange, 
            string routingKey, 
            IBasicProperties properties, 
            byte[] body)
        {
            base.HandleBasicDeliver(consumerTag, deliveryTag, redelivered, exchange, routingKey, properties, body);

            LOG.DebugFormat("Handling delivery for consumerTag [{0}], deliveryTag [{1}]", consumerTag, deliveryTag);

            // generate an envelope with proper headers
            Envelope env = RabbitMessageBus.CreateEnvelope(properties, body);

            // let our envelope handler process the event
            EventResult result = this.HandleEnvelope(env);

            // depending on the result, tell rabbitMQ to do the correct thing
            this.HandleEventResult(result, _channel, deliveryTag);
        }


        protected virtual EventResult HandleEnvelope(Envelope env)
        {
            EventResult result = EventResult.Failed;

			try
            {
				//TODO: PEGA-726 what if handler incorrectly returns null? I think we should assume Failed to be safe.
				result = _consumer.HandleEnvelope(env);
			}
            catch (Exception ex)
            {
				Guid id = env.GetId();
				
                LOG.ErrorFormat("An envelope handler failed to process an envelope {0}: {1}",
                    env.ToString(), ex);
			}

            return result;
        }

        protected virtual void HandleEventResult(EventResult result, IModel channel, ulong deliveryTag)
        {
            LOG.DebugFormat("HandleEventResult(result={0}, deliveryTag={1})", result.ToString(), deliveryTag.ToString());

            try
            {
                switch (result)
                {
                    case EventResult.Handled:
                        channel.BasicAck(deliveryTag, false);
                        break;
                    case EventResult.Failed:
                        channel.BasicReject(deliveryTag, false);
                        break;
                    case EventResult.Retry:
                        channel.BasicReject(deliveryTag, true);
                        break;
                }
            }
            catch (Exception ex)
            {
                LOG.Error("Failed to properly acknowledge (or reject) a message", ex);
            }
        }
    }
}
