namespace GlobalSight.Office2010Converters
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

            this.stopConverters();

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
            this.textBox1 = new System.Windows.Forms.TextBox();
            this.button1 = new System.Windows.Forms.Button();
            this.bPptStart = new System.Windows.Forms.Button();
            this.label1 = new System.Windows.Forms.Label();
            this.folderBrowserDialog2 = new System.Windows.Forms.FolderBrowserDialog();
            this.label2 = new System.Windows.Forms.Label();
            this.bPptStop = new System.Windows.Forms.Button();
            this.bExcelStop = new System.Windows.Forms.Button();
            this.bExcelStart = new System.Windows.Forms.Button();
            this.bWordStop = new System.Windows.Forms.Button();
            this.bWordStart = new System.Windows.Forms.Button();
            this.label3 = new System.Windows.Forms.Label();
            this.label4 = new System.Windows.Forms.Label();
            this.bAllStart = new System.Windows.Forms.Button();
            this.bAllStop = new System.Windows.Forms.Button();
            this.panel1 = new System.Windows.Forms.Panel();
            this.label5 = new System.Windows.Forms.Label();
            this.cbAutoStartAll = new System.Windows.Forms.CheckBox();
            this.SuspendLayout();
            // 
            // textBox1
            // 
            this.textBox1.Font = new System.Drawing.Font("Calibri", 11F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.textBox1.Location = new System.Drawing.Point(171, 285);
            this.textBox1.Name = "textBox1";
            this.textBox1.Size = new System.Drawing.Size(153, 25);
            this.textBox1.TabIndex = 5;
            // 
            // button1
            // 
            this.button1.Font = new System.Drawing.Font("Calibri", 11F);
            this.button1.Location = new System.Drawing.Point(353, 283);
            this.button1.Name = "button1";
            this.button1.Size = new System.Drawing.Size(89, 28);
            this.button1.TabIndex = 6;
            this.button1.Text = "Browse...";
            this.button1.UseVisualStyleBackColor = true;
            this.button1.Click += new System.EventHandler(this.button1_Click);
            // 
            // bPptStart
            // 
            this.bPptStart.Font = new System.Drawing.Font("Calibri", 11F);
            this.bPptStart.Location = new System.Drawing.Point(235, 183);
            this.bPptStart.Name = "bPptStart";
            this.bPptStart.Size = new System.Drawing.Size(89, 28);
            this.bPptStart.TabIndex = 7;
            this.bPptStart.Text = "Start";
            this.bPptStart.UseVisualStyleBackColor = true;
            this.bPptStart.Click += new System.EventHandler(this.bPptStart_Click);
            // 
            // label1
            // 
            this.label1.Anchor = ((System.Windows.Forms.AnchorStyles)((((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom)
                        | System.Windows.Forms.AnchorStyles.Left)
                        | System.Windows.Forms.AnchorStyles.Right)));
            this.label1.AutoSize = true;
            this.label1.Font = new System.Drawing.Font("Calibri", 15F, System.Drawing.FontStyle.Bold);
            this.label1.Location = new System.Drawing.Point(44, 20);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(388, 24);
            this.label1.TabIndex = 9;
            this.label1.Text = "MS Office 2010 In Context Review Converters";
            // 
            // label2
            // 
            this.label2.AutoSize = true;
            this.label2.Font = new System.Drawing.Font("Calibri", 11F);
            this.label2.Location = new System.Drawing.Point(25, 94);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(137, 18);
            this.label2.TabIndex = 10;
            this.label2.Text = "Word 2010 Converter";
            // 
            // bPptStop
            // 
            this.bPptStop.Enabled = false;
            this.bPptStop.Font = new System.Drawing.Font("Calibri", 11F);
            this.bPptStop.Location = new System.Drawing.Point(353, 183);
            this.bPptStop.Name = "bPptStop";
            this.bPptStop.Size = new System.Drawing.Size(89, 28);
            this.bPptStop.TabIndex = 11;
            this.bPptStop.Text = "Stop";
            this.bPptStop.UseVisualStyleBackColor = true;
            this.bPptStop.Click += new System.EventHandler(this.bPptStop_Click);
            // 
            // bExcelStop
            // 
            this.bExcelStop.Enabled = false;
            this.bExcelStop.Font = new System.Drawing.Font("Calibri", 11F);
            this.bExcelStop.Location = new System.Drawing.Point(353, 136);
            this.bExcelStop.Name = "bExcelStop";
            this.bExcelStop.Size = new System.Drawing.Size(89, 28);
            this.bExcelStop.TabIndex = 12;
            this.bExcelStop.Text = "Stop";
            this.bExcelStop.UseVisualStyleBackColor = true;
            this.bExcelStop.Click += new System.EventHandler(this.bExcelStop_Click);
            // 
            // bExcelStart
            // 
            this.bExcelStart.Font = new System.Drawing.Font("Calibri", 11F);
            this.bExcelStart.Location = new System.Drawing.Point(235, 136);
            this.bExcelStart.Name = "bExcelStart";
            this.bExcelStart.Size = new System.Drawing.Size(89, 28);
            this.bExcelStart.TabIndex = 13;
            this.bExcelStart.Text = "Start";
            this.bExcelStart.UseVisualStyleBackColor = true;
            this.bExcelStart.Click += new System.EventHandler(this.bExcelStart_Click);
            // 
            // bWordStop
            // 
            this.bWordStop.Enabled = false;
            this.bWordStop.Font = new System.Drawing.Font("Calibri", 11F);
            this.bWordStop.Location = new System.Drawing.Point(353, 89);
            this.bWordStop.Name = "bWordStop";
            this.bWordStop.Size = new System.Drawing.Size(89, 28);
            this.bWordStop.TabIndex = 14;
            this.bWordStop.Text = "Stop";
            this.bWordStop.UseVisualStyleBackColor = true;
            this.bWordStop.Click += new System.EventHandler(this.bWordStop_Click);
            // 
            // bWordStart
            // 
            this.bWordStart.Font = new System.Drawing.Font("Calibri", 11F);
            this.bWordStart.Location = new System.Drawing.Point(235, 89);
            this.bWordStart.Name = "bWordStart";
            this.bWordStart.Size = new System.Drawing.Size(89, 28);
            this.bWordStart.TabIndex = 15;
            this.bWordStart.Text = "Start";
            this.bWordStart.UseVisualStyleBackColor = true;
            this.bWordStart.Click += new System.EventHandler(this.bWordStart_Click);
            // 
            // label3
            // 
            this.label3.AutoSize = true;
            this.label3.Font = new System.Drawing.Font("Calibri", 11F);
            this.label3.Location = new System.Drawing.Point(27, 141);
            this.label3.Name = "label3";
            this.label3.Size = new System.Drawing.Size(136, 18);
            this.label3.TabIndex = 16;
            this.label3.Text = "Excel 2010 Converter";
            // 
            // label4
            // 
            this.label4.AutoSize = true;
            this.label4.Font = new System.Drawing.Font("Calibri", 11F);
            this.label4.Location = new System.Drawing.Point(25, 188);
            this.label4.Name = "label4";
            this.label4.Size = new System.Drawing.Size(177, 18);
            this.label4.TabIndex = 17;
            this.label4.Text = "PowerPoint 2010 Converter";
            // 
            // bAllStart
            // 
            this.bAllStart.Font = new System.Drawing.Font("Calibri", 11F);
            this.bAllStart.Location = new System.Drawing.Point(235, 231);
            this.bAllStart.Name = "bAllStart";
            this.bAllStart.Size = new System.Drawing.Size(89, 28);
            this.bAllStart.TabIndex = 18;
            this.bAllStart.Text = "Start All";
            this.bAllStart.UseVisualStyleBackColor = true;
            this.bAllStart.Click += new System.EventHandler(this.bAllStart_Click);
            // 
            // bAllStop
            // 
            this.bAllStop.Enabled = false;
            this.bAllStop.Font = new System.Drawing.Font("Calibri", 11F);
            this.bAllStop.Location = new System.Drawing.Point(353, 231);
            this.bAllStop.Name = "bAllStop";
            this.bAllStop.Size = new System.Drawing.Size(89, 28);
            this.bAllStop.TabIndex = 19;
            this.bAllStop.Text = "Stop All";
            this.bAllStop.UseVisualStyleBackColor = true;
            this.bAllStop.Click += new System.EventHandler(this.bAllStop_Click);
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
            this.label5.Font = new System.Drawing.Font("Calibri", 11F);
            this.label5.Location = new System.Drawing.Point(27, 288);
            this.label5.Name = "label5";
            this.label5.Size = new System.Drawing.Size(138, 26);
            this.label5.TabIndex = 0;
            this.label5.Text = "Conversion Directory";
            // 
            // cbAutoStartAll
            // 
            this.cbAutoStartAll.AutoSize = true;
            this.cbAutoStartAll.Location = new System.Drawing.Point(30, 231);
            this.cbAutoStartAll.Name = "cbAutoStartAll";
            this.cbAutoStartAll.Size = new System.Drawing.Size(109, 22);
            this.cbAutoStartAll.TabIndex = 21;
            this.cbAutoStartAll.Text = "Auto Start All";
            this.cbAutoStartAll.UseVisualStyleBackColor = true;
            this.cbAutoStartAll.CheckedChanged += new System.EventHandler(this.cbAutoStartAll_CheckedChanged);
            // 
            // MainForm
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(8F, 18F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(477, 341);
            this.Controls.Add(this.cbAutoStartAll);
            this.Controls.Add(this.label5);
            this.Controls.Add(this.panel1);
            this.Controls.Add(this.bAllStop);
            this.Controls.Add(this.bAllStart);
            this.Controls.Add(this.label4);
            this.Controls.Add(this.label3);
            this.Controls.Add(this.bWordStart);
            this.Controls.Add(this.bWordStop);
            this.Controls.Add(this.bExcelStart);
            this.Controls.Add(this.bExcelStop);
            this.Controls.Add(this.bPptStop);
            this.Controls.Add(this.label2);
            this.Controls.Add(this.label1);
            this.Controls.Add(this.bPptStart);
            this.Controls.Add(this.button1);
            this.Controls.Add(this.textBox1);
            this.Font = new System.Drawing.Font("Calibri", 11F);
            this.FormBorderStyle = System.Windows.Forms.FormBorderStyle.Fixed3D;
            this.Icon = ((System.Drawing.Icon)(resources.GetObject("$this.Icon")));
            this.MaximizeBox = false;
            this.Name = "MainForm";
            this.StartPosition = System.Windows.Forms.FormStartPosition.CenterScreen;
            this.Text = "In Context Review Converter - Version: 8.6.2";
            this.FormClosing += new System.Windows.Forms.FormClosingEventHandler(this.MainForm_FormClosing);
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.TextBox textBox1;
        private System.Windows.Forms.Button button1;
        private System.Windows.Forms.Button bPptStart;
        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.FolderBrowserDialog folderBrowserDialog2;
        private System.Windows.Forms.Label label2;
        private System.Windows.Forms.Button bPptStop;
        private System.Windows.Forms.Button bExcelStop;
        private System.Windows.Forms.Button bExcelStart;
        private System.Windows.Forms.Button bWordStop;
        private System.Windows.Forms.Button bWordStart;
        private System.Windows.Forms.Label label3;
        private System.Windows.Forms.Label label4;
        private System.Windows.Forms.Button bAllStart;
        private System.Windows.Forms.Button bAllStop;
        private System.Windows.Forms.Panel panel1;
        private System.Windows.Forms.Label label5;
        private System.Windows.Forms.CheckBox cbAutoStartAll;
    }
}