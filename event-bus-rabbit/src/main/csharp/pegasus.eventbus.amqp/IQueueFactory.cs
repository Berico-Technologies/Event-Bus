﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace pegasus.eventbus.amqp
{
    public interface IQueueFactory
    {
        IQueue CreateQueue(IExchange exchange);
    }
}
