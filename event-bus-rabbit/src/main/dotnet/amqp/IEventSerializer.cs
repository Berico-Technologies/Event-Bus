using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace pegasus.eventbus.amqp
{
    /// <summary>
    /// Defines the interface for a component that can serialize and deserialize events.
    /// </summary>
    public interface IEventSerializer
    {
        /// <summary>
        /// Serializes an event into a byte array that represents it.
        /// </summary>
        /// <param name="ev">The event to serialize</param>
        /// <returns>a byte array representation of the event</returns>
        byte[] Serialize(object ev);

        /// <summary>
        /// Deserializes an event from its raw bytes.
        /// </summary>
        /// <typeparam name="TEvent">The type of event</typeparam>
        /// <param name="rawEvent">The byte array</param>
        /// <returns>The deserialized event</returns>
        TEvent Deserialize<TEvent>(byte[] rawEvent);
    }
}
