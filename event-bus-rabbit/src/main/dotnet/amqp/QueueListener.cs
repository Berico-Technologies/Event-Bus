using System;
using System.Collections.Generic;

using log4net;

using pegasus.eventbus.client;


namespace pegasus.eventbus.amqp
{
	/// <summary>
	/// Watches a Queue for new messages on a background thread, calling the EnvelopeHandler when new messages arrive.
	/// </summary>
	public class QueueListener
	{
		protected static readonly ILog LOG = LogManager.GetLogger(typeof(QueueListener));
		
		private IAmqpMessageBus _messageBus;
		private string _queueName;
		private bool _isDurable;
		private IEnumerable<RoutingInfo> _routes;
		private IEnvelopeHandler _envHandler;
		private string _consumerTag;
		private bool _isListening;
		
		
		public bool IsListening
		{
			get { return _isListening; }
		}
		
		
		/// <summary>
		/// Start up an new Queue Listener bound on the supplied queue name, with the provided EnvelopeHander dealing 
		/// with new messages.
		/// </summary>
		/// <param name='messageBus'>
		/// Message bus.
		/// </param>
		/// <param name='queueName'>
		/// The name of the queue to watch
		/// </param>
		/// <param name='queueIsDurable'>
		/// Queue is durable.
		/// </param>
		/// <param name='routes'>
		/// Routes.
		/// </param>
		/// <param name='envelopeHandler'>
		/// the handler that deals with incoming envelopes
		/// </param>
		public QueueListener (
			IAmqpMessageBus messageBus,
			string queueName,
			bool queueIsDurable,
			IEnumerable<RoutingInfo> routes,
			IEnvelopeHandler envelopeHandler)
		{
			_messageBus = messageBus;
			_queueName = queueName;
			_isDurable = queueIsDurable;
			_routes = routes;
			_envHandler = envelopeHandler;
		}
		
		
		/// <summary>
		/// Begin listening for messages on the Queue.
		/// </summary>
		public void StartListening()
		{
			LOG.DebugFormat("Creating new queue [{0}] (unless it already exists.", _queueName);
			
			_messageBus.CreateQueue(_queueName, _routes, _isDurable);
			_consumerTag = _messageBus.BeginConsumingMessages(_queueName, _envHandler);
			
			LOG.DebugFormat("Now consuming queue [{0}] with consumerTag [{1}].", _queueName, _consumerTag);
			
			_isListening = true;
		}
		
		/// <summary>
		/// stop listening on the queue, thereby stopping the background thread.
		/// </summary>
		public void StopListening()
		{
			LOG.DebugFormat("Stopping consumption of queue [{0}], consumerTag [{1}].", _queueName, _consumerTag);
			
			_messageBus.StopConsumingMessages(_consumerTag);
			
			LOG.DebugFormat("Stopped consumption of queue [{0}], consumerTag [{1}]", _queueName, _consumerTag);
			
			_isListening = false;
		}
	}
}

