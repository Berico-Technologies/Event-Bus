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
            throw new NotImplementedException();
        }

        public string BeginConsumingMessages(string queueName, IEnvelopeHandler consumer)
        {
            throw new NotImplementedException();
        }

        public void StopConsumingMessages(string consumerTag)
        {
            throw new NotImplementedException();
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
