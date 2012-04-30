using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace pegasus.eventbus.client
{
    /// <summary>
    /// Interface to be implemented by classes which provide handling of raw envelopes to be used in conjunction with 
    /// the <see cref="Subscription"/> constructor(s) that accept envelope handlers in lieu of event handlers.
    /// </summary>
    public interface IEnvelopeHandler
    {
        /// <summary>
        /// This method will be invoked when a received event cannot be handed to the registered event handler or when 
        /// the event handler returns <see cref="EventResult.Failed"/> or throws an exception.
        /// </summary>
        /// <param name="envelope">
        /// The envelope representing the headers and raw byte stream of the the event.
        /// </param>
        /// <returns>
        /// An <see cref="EventResult"/> indicating the final disposition of the event.
        /// </returns>
        EventResult HandleEnvelope(Envelope envelope);

        /// <summary>
        /// The EventSetName getter.
        /// </summary>
        /// <returns>
        /// The EventSetName
        /// </returns>
        string GetEventSetName();
    }
}
