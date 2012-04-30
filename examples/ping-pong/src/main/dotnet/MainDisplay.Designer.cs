namespace pegasus.eventbus.examples.pingpong
{
    partial class MainDisplay
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            this.groupBox1 = new System.Windows.Forms.GroupBox();
            this._sender = new System.Windows.Forms.TextBox();
            this.label1 = new System.Windows.Forms.Label();
            this._sendBtn = new System.Windows.Forms.Button();
            this.groupBox2 = new System.Windows.Forms.GroupBox();
            this._pongList = new System.Windows.Forms.ListView();
            this.id = ((System.Windows.Forms.ColumnHeader)(new System.Windows.Forms.ColumnHeader()));
            this.sender = ((System.Windows.Forms.ColumnHeader)(new System.Windows.Forms.ColumnHeader()));
            this.latency = ((System.Windows.Forms.ColumnHeader)(new System.Windows.Forms.ColumnHeader()));
            this.groupBox3 = new System.Windows.Forms.GroupBox();
            this._output = new System.Windows.Forms.RichTextBox();
            this.groupBox1.SuspendLayout();
            this.groupBox2.SuspendLayout();
            this.groupBox3.SuspendLayout();
            this.SuspendLayout();
            // 
            // groupBox1
            // 
            this.groupBox1.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.groupBox1.Controls.Add(this._sendBtn);
            this.groupBox1.Controls.Add(this.label1);
            this.groupBox1.Controls.Add(this._sender);
            this.groupBox1.Location = new System.Drawing.Point(12, 12);
            this.groupBox1.Name = "groupBox1";
            this.groupBox1.Size = new System.Drawing.Size(595, 54);
            this.groupBox1.TabIndex = 0;
            this.groupBox1.TabStop = false;
            this.groupBox1.Text = "Ping Control Panel";
            // 
            // _sender
            // 
            this._sender.Location = new System.Drawing.Point(181, 19);
            this._sender.Name = "_sender";
            this._sender.Size = new System.Drawing.Size(323, 20);
            this._sender.TabIndex = 0;
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Location = new System.Drawing.Point(6, 22);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(169, 13);
            this.label1.TabIndex = 1;
            this.label1.Text = "Who should we say the sender is?";
            // 
            // _sendBtn
            // 
            this._sendBtn.Location = new System.Drawing.Point(510, 17);
            this._sendBtn.Name = "_sendBtn";
            this._sendBtn.Size = new System.Drawing.Size(75, 23);
            this._sendBtn.TabIndex = 2;
            this._sendBtn.Text = "Send Ping";
            this._sendBtn.UseVisualStyleBackColor = true;
            this._sendBtn.Click += new System.EventHandler(this._sendBtn_Click);
            // 
            // groupBox2
            // 
            this.groupBox2.Anchor = ((System.Windows.Forms.AnchorStyles)((((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom) 
            | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.groupBox2.Controls.Add(this._pongList);
            this.groupBox2.Location = new System.Drawing.Point(12, 72);
            this.groupBox2.Name = "groupBox2";
            this.groupBox2.Size = new System.Drawing.Size(595, 190);
            this.groupBox2.TabIndex = 1;
            this.groupBox2.TabStop = false;
            this.groupBox2.Text = "Pong Event Log";
            // 
            // _pongList
            // 
            this._pongList.Columns.AddRange(new System.Windows.Forms.ColumnHeader[] {
            this.id,
            this.sender,
            this.latency});
            this._pongList.Dock = System.Windows.Forms.DockStyle.Fill;
            this._pongList.Location = new System.Drawing.Point(3, 16);
            this._pongList.Name = "_pongList";
            this._pongList.Size = new System.Drawing.Size(589, 171);
            this._pongList.TabIndex = 0;
            this._pongList.UseCompatibleStateImageBehavior = false;
            this._pongList.View = System.Windows.Forms.View.Details;
            // 
            // id
            // 
            this.id.Text = "Pong Event ID";
            this.id.Width = 240;
            // 
            // sender
            // 
            this.sender.Text = "Pong Sender";
            this.sender.Width = 180;
            // 
            // latency
            // 
            this.latency.Text = "Latency (ms)";
            this.latency.Width = 90;
            // 
            // groupBox3
            // 
            this.groupBox3.Controls.Add(this._output);
            this.groupBox3.Location = new System.Drawing.Point(15, 268);
            this.groupBox3.Name = "groupBox3";
            this.groupBox3.Size = new System.Drawing.Size(592, 100);
            this.groupBox3.TabIndex = 2;
            this.groupBox3.TabStop = false;
            this.groupBox3.Text = "Log";
            // 
            // _output
            // 
            this._output.Dock = System.Windows.Forms.DockStyle.Fill;
            this._output.Location = new System.Drawing.Point(3, 16);
            this._output.Name = "_output";
            this._output.Size = new System.Drawing.Size(586, 81);
            this._output.TabIndex = 0;
            this._output.Text = "";
            // 
            // MainDisplay
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(619, 378);
            this.Controls.Add(this.groupBox3);
            this.Controls.Add(this.groupBox2);
            this.Controls.Add(this.groupBox1);
            this.Name = "MainDisplay";
            this.Text = "MainDisplay";
            this.FormClosed += new System.Windows.Forms.FormClosedEventHandler(this.MainDisplay_FormClosed);
            this.Load += new System.EventHandler(this.MainDisplay_Load);
            this.groupBox1.ResumeLayout(false);
            this.groupBox1.PerformLayout();
            this.groupBox2.ResumeLayout(false);
            this.groupBox3.ResumeLayout(false);
            this.ResumeLayout(false);

        }

        #endregion

        private System.Windows.Forms.GroupBox groupBox1;
        private System.Windows.Forms.Button _sendBtn;
        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.TextBox _sender;
        private System.Windows.Forms.GroupBox groupBox2;
        private System.Windows.Forms.ListView _pongList;
        private System.Windows.Forms.ColumnHeader id;
        private System.Windows.Forms.ColumnHeader sender;
        private System.Windows.Forms.ColumnHeader latency;
        private System.Windows.Forms.GroupBox groupBox3;
        private System.Windows.Forms.RichTextBox _output;
    }
}