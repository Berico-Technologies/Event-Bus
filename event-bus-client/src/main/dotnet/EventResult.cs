using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace pegasus.eventbus.client
{
    /// <summary>
    /// An enumeration that defines the values that may be returned from the <see cref="IEventHandler.HandleEvent(object)"/>
    /// and <see cref="IEnvelopeHandler.HandleEnvelope(Envelope)"/> methods
    /// </summary>
    public enum EventResult
    {
        /// <summary>
        /// Indicates that the event was successfully handled.
        /// </summary>
        Handled,

        /// <summary>
        /// Indicates that the event could not be handled and should not be retried.
        /// </summary>
        Failed,

        /// <summary>
        /// Indicates that the event could not be handled at this time.  The event will be placed at 
        /// the back of the queue and re-raised when it reaches the front again.
        /// </summary>
        Retry
    }
}
