using System;
using System.Collections.Generic;
using System.Linq;

using NUnit.Framework;


namespace pegasus.eventbus.amqp.tests
{
	[TestFixture]
	public class AmqpConnectionParametersTests
	{
		[TestCase]
		public void should_have_default_values()
		{
			AmqpConnectionParameters cut = new AmqpConnectionParameters();

			Assert.AreEqual("rabbit", 		cut.Host);
			Assert.AreEqual("5672", 		cut.Port);
			Assert.AreEqual("guest", 		cut.Username);
			Assert.AreEqual("guest", 		cut.Password);
			Assert.AreEqual(string.Empty, 	cut.VHost);
		}

		[TestCase]
		public void should_parse_semicolon_delimited_string()
		{
			string parameters = "host=eventbus.orion.mil;port=5672;username=service1;password=guest;vhost=default";

			AmqpConnectionParameters cut = new AmqpConnectionParameters(parameters);

			Assert.AreEqual("eventbus.orion.mil", 	cut.Host);
			Assert.AreEqual("5672", 				cut.Port);
			Assert.AreEqual("service1", 			cut.Username);
			Assert.AreEqual("guest", 				cut.Password);
			Assert.AreEqual("default", 				cut.VHost);
		}

		[TestCase]
		public void should_parse_uri_string()
		{
			string parameters = "amqp://test:password123@rabbit-master.pegasus.mil:1234/myVhost";

			AmqpConnectionParameters cut = new AmqpConnectionParameters(parameters);

			Assert.AreEqual("rabbit-master.pegasus.mil", 	cut.Host);
			Assert.AreEqual("1234", 						cut.Port);
			Assert.AreEqual("test", 						cut.Username);
			Assert.AreEqual("password123", 					cut.Password);
			Assert.AreEqual("myVhost", 						cut.VHost);
		}

		[TestCase]
		public void should_parse_uri_with_no_port()
		{
			string parameters = "amqp://test:password123@rabbit-master.pegasus.mil/myVhost";

			AmqpConnectionParameters cut = new AmqpConnectionParameters(parameters);

			Assert.AreEqual("rabbit-master.pegasus.mil", 	cut.Host);
			Assert.AreEqual(string.Empty, 					cut.Port);
			Assert.AreEqual("test", 						cut.Username);
			Assert.AreEqual("password123", 					cut.Password);
			Assert.AreEqual("myVhost", 						cut.VHost);
		}

		[TestCase]
		public void should_parse_uri_with_no_port_or_host()
		{
			string parameters = "amqp://test:password123@rabbit-master.pegasus.mil";

			AmqpConnectionParameters cut = new AmqpConnectionParameters(parameters);

			Assert.AreEqual("rabbit-master.pegasus.mil", 	cut.Host);
			Assert.AreEqual(string.Empty, 					cut.Port);
			Assert.AreEqual("test", 						cut.Username);
			Assert.AreEqual("password123", 					cut.Password);
			Assert.AreEqual(string.Empty, 					cut.VHost);
		}

		[TestCase]
		public void AmqpConnectionParamers_should_parse_uri_with_no_host()
		{
			string parameters = "amqp://test:password123@rabbit-master.pegasus.mil:1234";

			AmqpConnectionParameters cut = new AmqpConnectionParameters(parameters);

			Assert.AreEqual("rabbit-master.pegasus.mil", 	cut.Host);
			Assert.AreEqual("1234", 						cut.Port);
			Assert.AreEqual("test", 						cut.Username);
			Assert.AreEqual("password123", 					cut.Password);
			Assert.AreEqual(string.Empty, 					cut.VHost);
		}
	}
}