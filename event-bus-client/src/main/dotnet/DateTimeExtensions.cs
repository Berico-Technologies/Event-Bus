using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace pegasus.eventbus.client
{
    public static class DateTimeExtensions
    {
        public static readonly DateTime EPOCH = new DateTime(1970, 1, 1, 0, 0, 0, 0);

        /// <summary>
        /// The Unix epoch (or Unix time or POSIX time or Unix timestamp) is the number of seconds that have elapsed 
        /// since January 1, 1970 (midnight UTC/GMT), not counting leap seconds (in ISO 8601: 1970-01-01T00:00:00Z). 
        /// Literally speaking the epoch is Unix time 0 (midnight 1/1/1970), but 'epoch' is often used as a synonym 
        /// for 'Unix time'.
        /// </summary>
        /// <param name="date"></param>
        /// <returns></returns>
        public static long GetEpochTime(this DateTime date)
        {
            return (date.ToUniversalTime().Ticks - 621355968000000000) / 10000000;
        }

        /// <summary>
        /// The Java timestamp is milliseconds past epoch (not seconds)
        /// </summary>
        /// <param name="date"></param>
        /// <returns></returns>
        public static long GetJavaTime(this DateTime date)
        {
            return (date.ToUniversalTime().Ticks - 621355968000000000) / 10000;
        }

        /// <summary>
        /// Parses the given java timestamp and returns the System.DateTime.  It is assumed
        /// </summary>
        /// <param name="javaTimeStamp"></param>
        /// <returns></returns>
        public static DateTime ToDateTimeFromJavaTimestamp(this long javaTimeStamp)
        {
            return EPOCH.AddSeconds(javaTimeStamp / 1000).ToLocalTime();
        }
    }
}
