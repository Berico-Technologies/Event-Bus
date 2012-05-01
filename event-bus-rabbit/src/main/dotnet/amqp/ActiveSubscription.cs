using System;

namespace pegasus.eventbus.amqp
{
	/// <summary>
	/// Represents a subscription with an active queue listener, pulling messages from the Bus.
	/// </summary>
	public class ActiveSubscription
	{
		private string _queueName;
		private bool _isDurable;
		private QueueListener _listener;
		private bool _isActive;
		
		
		public string QueueName
		{
			get { return _queueName; }
		}
		
		public bool IsForDurableQueue
		{
			get { return _isDurable; }
		}
		
		public QueueListener Listener
		{
			get { return _listener; }
		}
		
		public bool IsActive
		{
			get { return _isActive; }
			set { _isActive = value; }
		}
		
		
		public ActiveSubscription(string queueName, bool queueIsDurable, QueueListener listener)
		{
			_queueName = queueName;
			_isDurable = queueIsDurable;
			_listener = listener;
		}
	}
}

