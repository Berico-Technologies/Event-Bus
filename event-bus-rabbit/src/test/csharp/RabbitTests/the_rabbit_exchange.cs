using System;
using System.Text;
using System.Collections.Generic;
using System.Linq;

using Microsoft.VisualStudio.TestTools.UnitTesting;

using pegasus.eventbus.rabbit;


namespace RabbitTests
{
    [TestClass]
    public class the_rabbit_exchange
    {
        [TestMethod]
        public void is_equal_to_another_with_the_same_values()
        {
            RabbitExchange ex1 = new RabbitExchange("Test", "Host", "vHost");
            RabbitExchange ex2 = new RabbitExchange("Test", "Host", "vHost");

            Assert.IsTrue(ex1.Equals(ex2));
        }

        public void is_not_equal_to_another_with_different_values()
        {
            RabbitExchange ex1 = new RabbitExchange("Test", "Host", "vHost");
            RabbitExchange ex2 = new RabbitExchange("Test", "Host", "v####");

            Assert.IsFalse(ex1.Equals(ex2));
        }
    }
}
