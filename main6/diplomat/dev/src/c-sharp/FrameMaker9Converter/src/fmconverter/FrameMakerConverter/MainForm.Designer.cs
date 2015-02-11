namespace FrameMakerConverter
{
    partial class MainForm
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

            this.stopFrame();

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
            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(MainForm));
            this.tbFM = new System.Windows.Forms.TextBox();
            this.bBrowseFM = new System.Windows.Forms.Button();
            this.label1 = new System.Windows.Forms.Label();
            this.folderBrowserDialog2 = new System.Windows.Forms.FolderBrowserDialog();
            this.bFrameStop = new System.Windows.Forms.Button();
            this.bFrameStart = new System.Windows.Forms.Button();
            this.panel1 = new System.Windows.Forms.Panel();
            this.label5 = new System.Windows.Forms.Label();
            this.label3 = new System.Windows.Forms.Label();
            this.bBrowseConv = new System.Windows.Forms.Button();
            this.tbConv = new System.Windows.Forms.TextBox();
            this.cbAutoStart = new System.Windows.Forms.CheckBox();
            this.SuspendLayout();
            // 
            // tbFM
            // 
            this.tbFM.Font = new System.Drawing.Font("Microsoft Sans Serif", 11F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.tbFM.Location = new System.Drawing.Point(173, 96);
            this.tbFM.Name = "tbFM";
            this.tbFM.Size = new System.Drawing.Size(193, 24);
            this.tbFM.TabIndex = 5;
            // 
            // bBrowseFM
            // 
            this.bBrowseFM.Font = new System.Drawing.Font("Microsoft Sans Serif", 11F);
            this.bBrowseFM.Location = new System.Drawing.Point(372, 94);
            this.bBrowseFM.Name = "bBrowseFM";
            this.bBrowseFM.Size = new System.Drawing.Size(89, 28);
            this.bBrowseFM.TabIndex = 6;
            this.bBrowseFM.Text = "Browse...";
            this.bBrowseFM.UseVisualStyleBackColor = true;
            this.bBrowseFM.Click += new System.EventHandler(this.bBrowseFM_Click);
            // 
            // label1
            // 
            this.label1.Anchor = ((System.Windows.Forms.AnchorStyles)((((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom)
                        | System.Windows.Forms.AnchorStyles.Left)
                        | System.Windows.Forms.AnchorStyles.Right)));
            this.label1.AutoSize = true;
            this.label1.Font = new System.Drawing.Font("Microsoft Sans Serif", 15F, System.Drawing.FontStyle.Bold);
            this.label1.Location = new System.Drawing.Point(114, 20);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(252, 25);
            this.label1.TabIndex = 9;
            this.label1.Text = "FrameMaker 9 Converter";
            // 
            // bFrameStop
            // 
            this.bFrameStop.Enabled = false;
            this.bFrameStop.Font = new System.Drawing.Font("Microsoft Sans Serif", 11F);
            this.bFrameStop.Location = new System.Drawing.Point(316, 219);
            this.bFrameStop.Name = "bFrameStop";
            this.bFrameStop.Size = new System.Drawing.Size(89, 28);
            this.bFrameStop.TabIndex = 14;
            this.bFrameStop.Text = "Stop";
            this.bFrameStop.UseVisualStyleBackColor = true;
            this.bFrameStop.Click += new System.EventHandler(this.bFrameStop_Click);
            // 
            // bFrameStart
            // 
            this.bFrameStart.Font = new System.Drawing.Font("Microsoft Sans Serif", 11F);
            this.bFrameStart.Location = new System.Drawing.Point(173, 217);
            this.bFrameStart.Name = "bFrameStart";
            this.bFrameStart.Size = new System.Drawing.Size(89, 28);
            this.bFrameStart.TabIndex = 15;
            this.bFrameStart.Text = "Start";
            this.bFrameStart.UseVisualStyleBackColor = true;
            this.bFrameStart.Click += new System.EventHandler(this.bFrameStart_Click);
            // 
            // panel1
            // 
            this.panel1.BorderStyle = System.Windows.Forms.BorderStyle.FixedSingle;
            this.panel1.Location = new System.Drawing.Point(0, 65);
            this.panel1.Name = "panel1";
            this.panel1.Size = new System.Drawing.Size(500, 1);
            this.panel1.TabIndex = 20;
            // 
            // label5
            // 
            this.label5.Font = new System.Drawing.Font("Microsoft Sans Serif", 11F);
            this.label5.Location = new System.Drawing.Point(13, 99);
            this.label5.Name = "label5";
            this.label5.Size = new System.Drawing.Size(154, 26);
            this.label5.TabIndex = 0;
            this.label5.Text = "FrameMaker 9 Path";
            // 
            // label3
            // 
            this.label3.Font = new System.Drawing.Font("Microsoft Sans Serif", 11F);
            this.label3.Location = new System.Drawing.Point(13, 148);
            this.label3.Name = "label3";
            this.label3.Size = new System.Drawing.Size(154, 26);
            this.label3.TabIndex = 21;
            this.label3.Text = "Conversion Directory";
            // 
            // bBrowseConv
            // 
            this.bBrowseConv.Font = new System.Drawing.Font("Microsoft Sans Serif", 11F);
            this.bBrowseConv.Location = new System.Drawing.Point(372, 143);
            this.bBrowseConv.Name = "bBrowseConv";
            this.bBrowseConv.Size = new System.Drawing.Size(89, 28);
            this.bBrowseConv.TabIndex = 23;
            this.bBrowseConv.Text = "Browse...";
            this.bBrowseConv.UseVisualStyleBackColor = true;
            this.bBrowseConv.Click += new System.EventHandler(this.bBrowseConv_Click);
            // 
            // tbConv
            // 
            this.tbConv.Font = new System.Drawing.Font("Microsoft Sans Serif", 11F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.tbConv.Location = new System.Drawing.Point(173, 145);
            this.tbConv.Name = "tbConv";
            this.tbConv.Size = new System.Drawing.Size(193, 24);
            this.tbConv.TabIndex = 22;
            // 
            // cbAutoStart
            // 
            this.cbAutoStart.AutoSize = true;
            this.cbAutoStart.Location = new System.Drawing.Point(30, 223);
            this.cbAutoStart.Name = "cbAutoStart";
            this.cbAutoStart.Size = new System.Drawing.Size(92, 22);
            this.cbAutoStart.TabIndex = 24;
            this.cbAutoStart.Text = "Auto Start";
            this.cbAutoStart.UseVisualStyleBackColor = true;
            this.cbAutoStart.CheckedChanged += new System.EventHandler(this.cbAutoStart_CheckedChanged);
            // 
            // MainForm
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(9F, 18F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(477, 341);
            this.Controls.Add(this.cbAutoStart);
            this.Controls.Add(this.label3);
            this.Controls.Add(this.bBrowseConv);
            this.Controls.Add(this.tbConv);
            this.Controls.Add(this.label5);
            this.Controls.Add(this.panel1);
            this.Controls.Add(this.bFrameStart);
            this.Controls.Add(this.bFrameStop);
            this.Controls.Add(this.label1);
            this.Controls.Add(this.bBrowseFM);
            this.Controls.Add(this.tbFM);
            this.Font = new System.Drawing.Font("Microsoft Sans Serif", 11F);
            this.FormBorderStyle = System.Windows.Forms.FormBorderStyle.Fixed3D;
            this.Icon = ((System.Drawing.Icon)(resources.GetObject("$this.Icon")));
            this.MaximizeBox = false;
            this.Name = "MainForm";
            this.StartPosition = System.Windows.Forms.FormStartPosition.CenterScreen;
            this.Text = "FrameMaker 9 Converter - Verion: 8.2.1";
            this.FormClosed += new System.Windows.Forms.FormClosedEventHandler(this.MainForm_FormClosed);
            this.FormClosing += new System.Windows.Forms.FormClosingEventHandler(this.MainForm_FormClosing);
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.TextBox tbFM;
        private System.Windows.Forms.Button bBrowseFM;
        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.FolderBrowserDialog folderBrowserDialog2;
        private System.Windows.Forms.Button bFrameStop;
        private System.Windows.Forms.Button bFrameStart;
        private System.Windows.Forms.Panel panel1;
        private System.Windows.Forms.Label label5;
        private System.Windows.Forms.Label label3;
        private System.Windows.Forms.Button bBrowseConv;
        private System.Windows.Forms.TextBox tbConv;
        private System.Windows.Forms.CheckBox cbAutoStart;
    }
}