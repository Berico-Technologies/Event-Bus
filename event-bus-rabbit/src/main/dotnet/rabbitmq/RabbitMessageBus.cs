using System;
using System.Collections;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using log4net;
using RabbitMQ.Client;
using RabbitMQ.Client.Events;

using pegasus.eventbus.amqp;
using pegasus.eventbus.client;


namespace pegasus.eventbus.rabbitmq
{
    public class RabbitMessageBus : IAmqpMessageBus
    {
        public event Action<bool> UnexpectedClose;

        private static readonly ILog LOG = LogManager.GetLogger(typeof(RabbitMessageBus));
        private static readonly string TOPIC_HEADER_KEY = "pegasus.eventbus.event.topic";
        private static readonly string PUB_TIMESTAMP_HEADER_KEY = "pegasus.eventbus.event.publication_timestamp";

        private RabbitConnection _connection;
        private IModel _cmdChannel;
        private IDictionary<string, IModel> _consumerChannels;
        private bool _isClosing;


        public static Envelope CreateEnvelope(IBasicProperties props, byte[] body)
        {
            Envelope envelope = new Envelope();

            IDictionary rabbitHeaders = props.Headers;
            IDictionary<string, string> ourHeaders = new Dictionary<string, string>();
            long javaTimestamp = 0L;

            if (null != rabbitHeaders)
            {
                foreach (string key in rabbitHeaders.Keys)
                {
                    ourHeaders.Add(key, rabbitHeaders[key].ToString());
                }

                javaTimestamp = long.Parse(ourHeaders[PUB_TIMESTAMP_HEADER_KEY]);
            }

            envelope.Body = body;
            envelope.SetId(props.MessageId == null ? Guid.Empty : Guid.Parse(props.MessageId));
            envelope.SetCorrelationId(props.CorrelationId == null ? Guid.Empty : Guid.Parse(props.CorrelationId));
            envelope.SetEventType(props.Type);
            envelope.SetReplyTo(props.ReplyTo);
            envelope.SetSendTime(javaTimestamp == 0L ? DateTime.MinValue : javaTimestamp.ToDateTimeFromJavaTimestamp());
            envelope.SetTopic(ourHeaders[TOPIC_HEADER_KEY]);

            // We don't want our internally used headers to be a Header property of the envelope.
            ourHeaders.Remove(TOPIC_HEADER_KEY);
            ourHeaders.Remove(PUB_TIMESTAMP_HEADER_KEY);

            return envelope;
        }


        public RabbitMessageBus(RabbitConnection connection)
        {
            _connection = connection;
            _connection.UnexpectedClose += new Action<bool>(_connection_UnexpectedClose);
        }


        public void Start()
        {
            LOG.Info("Starting the RabbitMQ message bus");

            _isClosing = false;

            this.OpenConnection();
        }

        public void Close()
        {
            LOG.Info("Closing connection to RabbitMQ");

            try
            {
                _isClosing = true;

                if (_cmdChannel.IsOpen) _cmdChannel.Close();
                if (_connection.IsOpen) _connection.Close();
            }
            catch (Exception ex)
            {
                LOG.Error("An exception occurred while closing the connection to RabbitMQ", ex);
            }
        }

        public void CreateExchange(Exchange exchange)
        {
            LOG.DebugFormat("Creating exchange '{0}'", exchange.Name);

            try
            {
                _cmdChannel.ExchangeDeclare(exchange.Name, exchange.Type.ToString().ToLower(), exchange.IsDurable);
            }
            catch (Exception ex)
            {
                LOG.Error("Failed to create the '" + exchange.Name + "' exchange", ex);
                throw;
            }
        }

        public void CreateQueue(string name, IEnumerable<RoutingInfo> bindings, bool durable)
        {
            LOG.DebugFormat("Declaring queue [name: {0}, durable: {1}]", name, durable);

            try
            {
                IDictionary args = new Hashtable();
                args.Add("x-expires", new TimeSpan(0, 30, 0).TotalMilliseconds);

                _cmdChannel.QueueDeclare(name, durable, false, false, args);
            }
            catch (Exception ex)
            {
                LOG.Error("Failed to create queue '" + name + "'", ex);
                throw;
            }

            foreach (RoutingInfo binding in bindings)
            {
                string msg = string.Format("Binding queue '{0}' on exchange '{1}' with expression '{2}'", 
                    name, binding.Exchange.Name, binding.RoutingKey);
                LOG.DebugFormat(msg);

                try
                {
                    _cmdChannel.QueueBind(name, binding.Exchange.Name, binding.RoutingKey);
                }
                catch (Exception ex)
                {
                    LOG.Error("Failed to Create " + msg, ex);
                    throw;
                }
            }
        }

        public void DeleteQueue(string queueName)
        {
            LOG.DebugFormat("Deleting queue '{0}'", queueName);

            try
            {
                _cmdChannel.QueueDelete(queueName);
            }
            catch (Exception ex)
            {
                LOG.Error("Failed to delete queue '" + queueName + "'", ex);
                throw;
            }
        }

        public void Publish(RoutingInfo route, Envelope message)
        {
            try
            {
                if (null == message.Headers) { message.Headers = new Dictionary<string, string>(); }

                if (null != message.GetTopic())
                {
                    message.Headers.Add(TOPIC_HEADER_KEY, message.GetTopic());
                }

                if (null != message.GetSendTime())
                {
                    message.Headers.Add(PUB_TIMESTAMP_HEADER_KEY, message.GetSendTime().GetEpochTime().ToString());
                }

                IBasicProperties props = _cmdChannel.CreateBasicProperties();
                props.MessageId = (null == message.GetId()) ? null : message.GetId().ToString();
                props.CorrelationId = (null == message.GetCorrelationId()) ? null : message.GetCorrelationId().ToString();
                props.Type = message.GetEventType();
                props.ReplyTo = message.GetReplyTo();
                props.Headers = (IDictionary)message.Headers;

                _cmdChannel.BasicPublish(route.Exchange.Name, route.RoutingKey, props, message.Body);

                LOG.DebugFormat("Event {0} has been published", message.GetId().ToString());
            }
            catch (Exception ex)
            {
                LOG.ErrorFormat("Failed to publish event {0}: {1}", message.GetId(), ex.ToString());
                throw;
            }
        }

        public string BeginConsumingMessages(string queueName, IEnvelopeHandler consumer)
        {
            LOG.DebugFormat("Begin consuming messages for queue [{0}] with an EnvelopeHandler of type [{1}].", 
                queueName, consumer.GetType().FullName);

            string consumerTag = this.CreateConsumerTag(queueName);

            IModel consumerChannel = this.CreateConsumerChannel(_connection, consumerTag);
            _consumerChannels.Add(consumerTag, consumerChannel);

            this.BeginConsumingMessages(queueName, consumerChannel, false, consumerTag, consumer);

            return consumerTag;
        }

        public void StopConsumingMessages(string consumerTag)
        {
            throw new NotImplementedException();
        }



        protected virtual void BeginConsumingMessages(string queueName, IModel channel, bool noAck, string consumerTag, IEnvelopeHandler consumer)
        {
            LOG.DebugFormat("Beginning basicConsume for ConsumerTag [{0}].", consumerTag);

            try
            {
                channel.BasicConsume(queueName, false, consumerTag, new EnvelopeHandlerBasicConsumer(channel, consumerTag, consumer));
                LOG.DebugFormat("Begun basicConsume for ConsumerTag [{0}].", consumerTag);
            }
            catch (Exception ex)
            {
                LOG.ErrorFormat("Failed to initiate basicConsume ConsumerTag [{0}].", consumerTag, ex);
                throw;
            }
        }



        private string CreateConsumerTag(string queueName)
        {
            string consumerTag = queueName + ":" + Guid.NewGuid().ToString();
            LOG.DebugFormat("Created consumerTag [{0}].", consumerTag);

            return consumerTag;
        }

        private IModel CreateConsumerChannel(RabbitConnection conn, string consumerTag)
        {
            IModel consumerChannel = null;

            try
            {
                consumerChannel = conn.CreateChannel();
                consumerChannel.ModelShutdown += new ModelShutdownEventHandler(_consumerChannel_ModelShutdown);

                LOG.DebugFormat("Successfully opened dedicated channel for ConsumerTag [{0}], Model [{1}].", 
                    consumerTag, consumerChannel.GetHashCode());
            }
            catch (Exception ex)
            {
                LOG.ErrorFormat("Could not create channel to consume messages with consumerTag {0}: {1}", consumerTag, ex);
                throw;
            }

            return consumerChannel;
        }

        private void _consumerChannel_ModelShutdown(IModel model, ShutdownEventArgs reason)
        {
 	        if (!_isClosing)
            {
                LOG.ErrorFormat("Consumer channel shutdown signal received for channel [{0}]: {1}", 
                    model.GetHashCode(), reason.Cause);
            }
        }

        private void _connection_UnexpectedClose(bool successfullyReopened)
        {
            LOG.DebugFormat("Unexpected connection close [successfullyReopened: {0}]", successfullyReopened);

            if (successfullyReopened)
            {
                LOG.Info("Reopening command channel");
                this.OpenCommandChannel();
            }

            this.RaiseUnexpectedCloseEvent(successfullyReopened);
        }

        private void OpenConnection()
        {
            try
            {
                if (!_connection.IsOpen) _connection.Open();
                this.OpenCommandChannel();
            }
            catch (Exception ex)
            {
                LOG.Error("Could not connect to RabbitMQ", ex);
                throw;
            }
        }

        private void OpenCommandChannel()
        {
            try
            {
                _cmdChannel = _connection.CreateChannel();
                _cmdChannel.ModelShutdown += new ModelShutdownEventHandler(_cmdChannel_ModelShutdown);
            }
            catch (Exception ex)
            {
                LOG.Error("Failed to open command channel", ex);
                throw;
            }
        }

        private void _cmdChannel_ModelShutdown(IModel model, ShutdownEventArgs reason)
        {
            if (!_isClosing)
            {
                LOG.ErrorFormat("Command channel shutdown unexpectedly [classId: {0}, initiator: {1}", reason.ClassId, reason.Initiator);
            }
        }

        private void RaiseUnexpectedCloseEvent(bool succcessfullyReopened)
        {
            if (null != this.UnexpectedClose)
            {
                foreach (Action<bool> a in this.UnexpectedClose.GetInvocationList())
                {
                    try { a(succcessfullyReopened); }
                    catch (Exception ex) { LOG.Warn("Unhandled exception raising unexpected close event", ex); }
                }
            }
        }
    }
}
