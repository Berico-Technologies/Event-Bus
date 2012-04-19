using System;
using System.Diagnostics;
using System.Threading;

using log4net;
using RabbitMQ.Client;
using RabbitMQ.Client.Events;

using pegasus.eventbus.amqp;


namespace pegasus.eventbus.rabbitmq
{
	public class RabbitConnection
	{
        public event Action<bool> UnexpectedClose;

        private static readonly ILog LOG = LogManager.GetLogger(typeof(RabbitConnection));
        private static readonly TimeSpan DEFAULT_RETRY_TIMEOUT = new TimeSpan(0, 0, 30);

        private ConnectionFactory _connFactory;
        private IConnection _connection;
        private TimeSpan _retryTimeout = DEFAULT_RETRY_TIMEOUT;
        private bool _isClosing;


        public bool IsOpen
        {
            get
            {
                return (null != _connection) ? _connection.IsOpen : false;
            }
        }


		public RabbitConnection(AmqpConnectionParameters connectionParameters)
		{
            _isClosing = false;

            _connFactory = new ConnectionFactory();
            _connFactory.UserName = connectionParameters.Username;
            _connFactory.Password = connectionParameters.Password;
            _connFactory.VirtualHost = connectionParameters.VHost;
            _connFactory.HostName = connectionParameters.Host;
            _connFactory.Port = int.Parse(connectionParameters.Port);
		}


        public void Open()
        {
            if ((null == _connection) || (false == _connection.IsOpen))
            {
                _isClosing = false;
                _connection = _connFactory.CreateConnection();
                _connection.ConnectionShutdown += new ConnectionShutdownEventHandler(_connection_ConnectionShutdown);
            }
        }

        public void Close()
        {
            if ((null != _connection) && (_connection.IsOpen))
            {
                _isClosing = true;
                _connection.Close();
            }
        }

        public IModel CreateChannel()
        {
            this.Open();

            return _connection.CreateModel();
        }


        private void _connection_ConnectionShutdown(IConnection connection, ShutdownEventArgs reason)
        {
            // if we know we're closing, all is well
            if (_isClosing) return;

            if (null == reason)
            {
                LOG.Warn("Connection shutdown notice received unexpectedly");
            }
            else
            {
                LOG.WarnFormat("Connection shutdown exception received unexpectedly: [classId: {0}, initiator: {1}]",
                    reason.ClassId, reason.Initiator);
            }

            bool isInErrorState = true;

            Stopwatch watch = new Stopwatch();
            watch.Start();

            try
            {
                while (watch.Elapsed < _retryTimeout)
                {
                    try
                    {
                        this.Open();

                        LOG.Info("Connection successfully reopened");
                        isInErrorState = false;

                        break;
                    }
                    catch (Exception ex)
                    {
                        LOG.Warn("Reconnect attempt failed", ex);
                        Thread.Sleep(100);
                    }
                }

                if (isInErrorState)
                {
                    LOG.Error("Attempt to reopen rabbit connection permanently failed");
                }

                this.RaiseUnexpectedCloseEvent(!isInErrorState);
            }
            finally
            {
                watch.Stop();
            }
        }

        private void RaiseUnexpectedCloseEvent(bool successfullyReopened)
        {
            if (null != this.UnexpectedClose)
            {
                foreach (Action<bool> a in this.UnexpectedClose.GetInvocationList())
                {
                    try { a(successfullyReopened); }
                    catch (Exception ex) { LOG.Warn("Caught unhandled exception raising unexpected close event", ex); }
                }
            }
        }
	}
}

