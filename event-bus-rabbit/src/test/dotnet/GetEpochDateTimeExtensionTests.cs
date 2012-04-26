using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using NUnit.Framework;

using pegasus.eventbus.client;


namespace eventbusrabbittests
{
    [TestFixture]
    public class GetEpochDateTimeExtensionTests
    {
        [TestCase]
        public void my_birthday_at_nine_PM_should_be_339368400()
        {
            long expectedEpoch = 339368400L; // according to http://www.epochconverter.com/
            DateTime myBirthday = new DateTime(1980, 10, 2, 21, 0, 0);

            long epoch = myBirthday.GetEpochTime();

            Assert.AreEqual(expectedEpoch, epoch);
        }

        [TestCase]
        public void java_time_divided_by_1000_should_equal_epoch_time()
        {
            DateTime myBirthday = new DateTime(1980, 10, 2, 21, 0, 0);

            long epoch = myBirthday.GetEpochTime();
            long epochMilli = myBirthday.GetJavaTime();

            Assert.AreEqual(epoch, epochMilli / 1000);
        }
    }
}
