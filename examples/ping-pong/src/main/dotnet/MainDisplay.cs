using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;

using pegasus.eventbus.client;
using pegasus.eventbus.amqp;

namespace pegasus.eventbus.examples.pingpong
{
    public partial class MainDisplay : Form
    {
        private static readonly string CLIENT_NAME = "dotnet-pingpong";

        private IEventManager _eventMgr;
        private List<Ping> _sentPings;
        private SubscriptionToken _pongSubToken;


        public MainDisplay()
        {
            _sentPings = new List<Ping>();

            InitializeComponent();

            AmqpConfiguration config = AmqpConfiguration.GetDefault(
                CLIENT_NAME,
                new AmqpConnectionParameters(
                    "amqp://guest:guest@osd-app02.osd.mil:5672/"));

            _eventMgr = new AmqpEventManager(config);

            _pongSubToken = _eventMgr.Subscribe<Pong>(this.Handle_PongEvent);

            _eventMgr.Start();

        }

        private void MainDisplay_Load(object sender, EventArgs e)
        {
            this.Log(".NET pingpong loaded");
        }


        private EventResult Handle_PongEvent(Pong pong)
        {
            Ping ping = _sentPings.SingleOrDefault(p => p.Timestamp.Equals(pong.PreviousTimestamp));
            if (null != ping)
            {
                double latency = DateTime
                    .Now
                    .Subtract(pong.PreviousTimestamp.ToDateTimeFromJavaTimestamp())
                    .TotalMilliseconds;

                _pongList.Items.Add(new ListViewItem(new string[]
                {
                    pong.Timestamp.ToString(),
                    pong.Sender,
                    latency.ToString()
                }));
            }
            else
            {
                this.Log("Received a pong for which we sent no ping");
            }

            return EventResult.Handled;
        }

        private void _sendBtn_Click(object sender, EventArgs e)
        {
            string eventSender = _sender.Text.Trim();

            if (string.IsNullOrEmpty(eventSender))
            {
                eventSender = CLIENT_NAME;
            }

            Ping ping = new Ping()
            {
                Sender = eventSender,
                Timestamp = DateTime.Now.GetJavaTime()
            };

            try
            {
                _eventMgr.Publish(ping);
                _sentPings.Add(ping);
            }
            catch (Exception ex)
            {
                this.Log(ex.ToString());
            }
        }

        private void MainDisplay_FormClosed(object sender, FormClosedEventArgs e)
        {
            _eventMgr.Unsubscribe(_pongSubToken);
            _eventMgr.Close();
        }

        private void Log(string message)
        {
            _output.AppendText(message);
            _output.AppendText(Environment.NewLine);
            _output.Select(_output.TextLength, 0);
            _output.ScrollToCaret();
        }
    }
}
