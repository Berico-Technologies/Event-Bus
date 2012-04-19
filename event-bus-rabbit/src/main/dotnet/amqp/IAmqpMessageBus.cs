using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using pegasus.eventbus.client;


namespace pegasus.eventbus.amqp
{
    public interface IAmqpMessageBus
    {
        event Action<bool> UnexpectedClose;


		/// <summary>
		/// Start the bus and open the connections.
		/// </summary>
	    void Start();

		/// <summary>
		/// Close the underlying connection the bus.
		/// </summary>
	    void Close();

	    /**
	     * Create and Exchange on the Bus.
	     *
	     * @param exchange
	     *            Exchange Info
	     */
	    void CreateExchange(Exchange exchange);

	    /**
	     * Create a Queue on the Bus.
	     *
	     * @param name
	     *            Name of the Queue to be created.
	     * @param bindings
	     *            Bindings between that Queue and an Exchange(s)
	     * @param durable
	     *            Is the Queue durable
	     */
	    void CreateQueue(string name, IEnumerable<RoutingInfo> bindings, bool durable);
	
	    /**
	     * Delete a Queue on the Bus.
	     * 
	     * @param queueName
	     *            Name of the Queue to remove.
	     */
	    void DeleteQueue(string queueName);
	
	    /**
	     * Publish a Message on the Queue, using the provided Routing Info
	     * 
	     * @param route
	     *            Routing Info that designates the Exchange to publish the event on.
	     * @param message
	     *            Message being published
	     */
	    void Publish(RoutingInfo route, Envelope message);
	
	    /**
	     * Begins consuming messages off of the specified queue
	     * 
	     * @param queueName
	     *            The name of the queue
	     * @param consumer
	     *            The EnvelopeHandler that will consume the messages.
	     * @return A tag that must be used when calling stopConsumingMessages(tag)
	     */
	    string BeginConsumingMessages(string queueName, IEnvelopeHandler consumer);
	
	    /**
	     * Stops consuming messages that are being consumed as a result of a call to beginConsumingMessages.
	     * 
	     * @param consumerTag
	     *            The tag returned by beginConsumingMessages
	     */
	    void StopConsumingMessages(string consumerTag);
    }
}
