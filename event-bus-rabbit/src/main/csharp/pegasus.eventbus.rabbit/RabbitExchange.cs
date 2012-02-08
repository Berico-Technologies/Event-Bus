using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using log4net;
using RabbitMQ.Client;

using pegasus.eventbus.amqp;


namespace pegasus.eventbus.rabbit
{
    public class RabbitExchange : IExchange, IDisposable
    {
        private static ILog LOG = LogManager.GetLogger(typeof(RabbitExchange));

        protected IConnection _conn;
        protected IModel _channel;
        protected object _channelLock = new object();


        public string Name
        {
            get;
            protected set;
        }

        public string Hostname
        {
            get;
            protected set;
        }

        public string VirtualHost
        {
            get;
            protected set;
        }


        public RabbitExchange(string name, string hostname, string vHost)
        {
            this.Name = name;
            this.Hostname = hostname;
            this.VirtualHost = string.IsNullOrEmpty(vHost) ? "/" : vHost;

            ConnectionFactory connFactory = new ConnectionFactory();
            connFactory.HostName = this.Hostname;
            connFactory.VirtualHost = this.VirtualHost;
            connFactory.Port = AmqpTcpEndpoint.UseDefaultPort;

            _conn = connFactory.CreateConnection();
            _channel = _conn.CreateModel();

            _channel.ExchangeDeclare(this.Name, ExchangeType.Topic, true);
        }


        public void Publish(byte[] message, string routingKey)
        {
            lock (_channelLock)
            {
                _channel.BasicPublish(this.Name, routingKey, _channel.CreateBasicProperties(), message);
            }
        }

        public void Publish(AmqpEnvelope env)
        {
            this.Publish(env.Serialize(), env.Topic);
        }

        public string ToUri()
        {
            return string.Format("rabbitmq-exchange://{0}@{1}/{2}", 
                this.Name, this.Hostname, this.VirtualHost);
        }


        public override bool Equals(object obj)
        {
            bool isEqual = false;

            if (obj is IExchange)
            {
                isEqual = this.Equals(obj as IExchange);
            }

            return isEqual;
        }

        public override int GetHashCode()
        {
            int hash = 17;

            unchecked
            {
                hash = hash * 11 + this.Name.GetHashCode();
                hash = hash * 11 + this.Hostname.GetHashCode();
                hash = hash * 11 + this.VirtualHost.GetHashCode();
            }

            return hash;
        }

        public bool Equals(IExchange other)
        {
            bool isEqual = false;

            try
            {
                isEqual = (
                    (this.Name == other.Name) &&
                    (this.Hostname == other.Hostname) &&
                    (this.VirtualHost == other.VirtualHost));
            }
            catch (NullReferenceException) { }

            return isEqual;
        }

        public void Dispose()
        {
            _channel.Close(200, "Goodbye");
            _conn.Close();

            _channel.Dispose();
            _conn.Dispose();
        }
    }
}
