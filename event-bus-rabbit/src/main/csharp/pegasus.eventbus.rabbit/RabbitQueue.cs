using System;
using System.Collections;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading;

using log4net;
using Newtonsoft.Json;
using RabbitMQ.Client;
using RabbitMQ.Client.Events;
using RabbitMQ.Client.Exceptions;

using pegasus.eventbus.amqp;


namespace pegasus.eventbus.rabbit
{
    public class RabbitQueue : IQueue, IDisposable
    {
        public event AmqpEnvelopeHandler EnvelopeReceived;

        private static ILog LOG = LogManager.GetLogger(typeof(RabbitQueue));

        private IExchange _exchange;
        private IList<AmqpBinding> _bindings;
        private string _queueName;
        private bool _isDisposing;
        private IConnection _connection;
        private IModel _channel;
        private QueueDeclareOk _queue;
        private AutoResetEvent _startEvent;


        public RabbitQueue(IExchange exchange)
        {
            _exchange = exchange;
            _bindings = new List<AmqpBinding>();
            _isDisposing = false;

            _queueName = string.Format("{0}_{1}", exchange.Name, Process.GetCurrentProcess().ProcessName);

            this.StartListening();
            this.WaitUntilStarted();
        }


        public void AddBinding(AmqpBinding binding)
        {
            _bindings.Add(binding);

            _channel.QueueBind(_queueName, _exchange.Name, binding.Topic);
        }

        public void RemoveBinding(AmqpBinding binding)
        {
            _bindings.Remove(binding);
        }

        public bool ContainsBinding(AmqpBinding binding)
        {
            return _bindings.Contains(binding);
        }


        public void Listen()
        {
            ConnectionFactory factory = new ConnectionFactory();
            factory.HostName = _exchange.Hostname;
            factory.VirtualHost = _exchange.VirtualHost;
            factory.Port = AmqpTcpEndpoint.UseDefaultPort;

            _connection = factory.CreateConnection();
            _channel = _connection.CreateModel();

            // declare the exchange
            _channel.ExchangeDeclare(_exchange.Name, ExchangeType.Topic, true);
            // declare the queue
            _queue = _channel.QueueDeclare(_queueName, false, true, false, null);

            // create a basic queueing consumer
            QueueingBasicConsumer consumer = new QueueingBasicConsumer(_channel);
            String consumerTag = _channel.BasicConsume(_queue.QueueName, false, consumer);

            // let anyone interested know that we've started
            this.MarkAsStarted();

            // and consume until we've been disposed
            while (!_isDisposing)
            {
                while (_bindings.Count > 0)
                {
                    try
                    {
                        BasicDeliverEventArgs e = (BasicDeliverEventArgs)consumer.Queue.Dequeue();
                        IBasicProperties props = e.BasicProperties;

                        // create an amqp envelope out of the byte array
                        AmqpEnvelope env = new AmqpEnvelope(e.Body);

                        // do some receipt-specific stuff
                        this.LogAndSetReceiptHeaders(env, props);

                        // raise the envelope to interested clients
                        this.Raise_EnvelopeReceived_Event(env);

                        // wait for the envelope to be marked as processed before getting the next message
                        env.WaitToBeProcessed();

                        // we may want the WaitToBeProcessed method to return a completion type
                        // one day so that we can do smarter things than just "ack", for example,
                        // "nack'ing" events we failed to process.
                        _channel.BasicAck(e.DeliveryTag, false);
                    }
                    catch (EndOfStreamException)
                    {
                        // this happens when we get disconnected somehow.  Typically, this
                        // happens when the process is shutting down.  Let's log this and
                        // not freak out about it.
                        LOG.InfoFormat("Rabbit Queue {0} is no longer listening for new events", _queueName);
                        break;
                    }
                    catch (Exception ex)
                    {
                        // The consumer was removed, either through
                        // channel or connection closure, or through the
                        // action of IModel.BasicCancel().
                        LOG.Error(string.Format(
                            "The Rabbit queue {0} encountered an exception attempting to consume a message.", _queueName
                            ), ex);
                        break;
                    }
                }

                if (_bindings.Count == 0)
                {
                    // avoid a hard-loop
                    Thread.Sleep(100);
                }
            }
        }

        public void LogAndSetReceiptHeaders(AmqpEnvelope env, IBasicProperties rabbitProps)
        {
            LOG.DebugFormat("Received an envelope from rabbit queue {0} containing an event of topic {1}", _queueName, env.Topic);

            env.Headers.Add(AmqpHeaders.RECEIVE_DATETIME, DateTime.Now.ToString());
            env.Headers.Add(AmqpHeaders.RECEVING_QUEUE, _queueName);

            if (null != rabbitProps)
            {
                env.Headers.Add(AmqpHeaders.REPLY_TO_QUEUE, rabbitProps.ReplyTo);

                // let's carry any string/string properties from the rabbit message in case
                // the client needs them for further use.
                if (null != rabbitProps.Headers)
                {
                    foreach (DictionaryEntry prop in rabbitProps.Headers)
                    {
                        if ((prop.Key is string) && (prop.Value is string))
                        {
                            LOG.DebugFormat("Adding rabbit property {0} with value {1}", prop.Key, prop.Value);
                            env.Headers.Add(prop.Key as string, prop.Value as string);
                        }
                    }
                }
            }
        }

        public void Raise_EnvelopeReceived_Event(AmqpEnvelope env)
        {
            if (null != this.EnvelopeReceived)
            {
                foreach (Delegate callback in this.EnvelopeReceived.GetInvocationList())
                {
                    try
                    {
                        callback.DynamicInvoke(env);
                    }
                    catch (Exception ex)
                    {
                        LOG.Warn(
                            "Caught an unhandled exception from a client while raising the envelope received event",
                            ex);
                    }
                }
            }
        }

        public void Dispose()
        {
            _isDisposing = true;
            _channel.Close(200, "Goodbye");
            _connection.Close();

            _channel.Dispose();
            _connection.Dispose();
        }

        protected virtual void StartListening()
        {
            Thread listeningThread = new Thread(this.Listen);
            listeningThread.Name = "Listening on Queue: " + _queueName;
            listeningThread.Start();
        }

        protected virtual void WaitUntilStarted()
        {
            _startEvent = new AutoResetEvent(false);
            _startEvent.WaitOne();
        }

        protected virtual void MarkAsStarted()
        {
            _startEvent.Set();
        }
    }
}
