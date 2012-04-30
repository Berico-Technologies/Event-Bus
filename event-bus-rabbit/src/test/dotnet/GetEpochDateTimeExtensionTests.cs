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
        public void my_birthday_at_nine_PM_should_be_339382800()
        {
            long expectedEpoch = 339382800L; // in our local (GMT-5) time, according to http://www.epochconverter.com/
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

        [TestCase]
        public void dateTime_to_java_time_should_parse_back_to_same_dateTime()
        {
            DateTime myBirthday = new DateTime(1980, 10, 2, 21, 0, 0);

            long javaTimestamp = myBirthday.GetJavaTime();

            DateTime parsedBirthday = javaTimestamp.ToDateTimeFromJavaTimestamp();

            Assert.AreEqual(myBirthday, parsedBirthday);
        }
    }
}
