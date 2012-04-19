using System;
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
        private IDictionary<string, IModel> _consumerChannel;
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

        }

        public void CreateQueue(string name, IEnumerable<RoutingInfo> bindings, bool durable)
        {
            throw new NotImplementedException();
        }

        public void DeleteQueue(string queueName)
        {
            throw new NotImplementedException();
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
