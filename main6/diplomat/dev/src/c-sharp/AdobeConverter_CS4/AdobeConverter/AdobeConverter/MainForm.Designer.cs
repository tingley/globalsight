namespace GlobalSight.AdobeConverter
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
            this.components = new System.ComponentModel.Container();
            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(MainForm));
            this.checkBox1 = new System.Windows.Forms.CheckBox();
            this.checkBox2 = new System.Windows.Forms.CheckBox();
            this.button1 = new System.Windows.Forms.Button();
            this.button2 = new System.Windows.Forms.Button();
            this.notifyIcon1 = new System.Windows.Forms.NotifyIcon(this.components);
            this.textBox_dir_indesign = new System.Windows.Forms.TextBox();
            this.textBox_dir_illustrator = new System.Windows.Forms.TextBox();
            this.button3 = new System.Windows.Forms.Button();
            this.button4 = new System.Windows.Forms.Button();
            this.label1 = new System.Windows.Forms.Label();
            this.folderBrowserDialog1 = new System.Windows.Forms.FolderBrowserDialog();
            this.folderBrowserDialog2 = new System.Windows.Forms.FolderBrowserDialog();
            this.label2 = new System.Windows.Forms.Label();
            this.cbAutoStart = new System.Windows.Forms.CheckBox();
            this.SuspendLayout();
            // 
            // checkBox1
            // 
            this.checkBox1.AutoSize = true;
            this.checkBox1.ForeColor = System.Drawing.Color.OrangeRed;
            this.checkBox1.Location = new System.Drawing.Point(12, 65);
            this.checkBox1.Name = "checkBox1";
            this.checkBox1.Size = new System.Drawing.Size(156, 16);
            this.checkBox1.TabIndex = 0;
            this.checkBox1.Text = "InDesign Converter CS4";
            this.checkBox1.UseVisualStyleBackColor = true;
            this.checkBox1.CheckedChanged += new System.EventHandler(this.checkBox1_CheckedChanged);
            // 
            // checkBox2
            // 
            this.checkBox2.AutoSize = true;
            this.checkBox2.ForeColor = System.Drawing.Color.OrangeRed;
            this.checkBox2.Location = new System.Drawing.Point(11, 111);
            this.checkBox2.Name = "checkBox2";
            this.checkBox2.Size = new System.Drawing.Size(174, 16);
            this.checkBox2.TabIndex = 1;
            this.checkBox2.Text = "Illustrator Converter CS2";
            this.checkBox2.UseVisualStyleBackColor = true;
            this.checkBox2.CheckedChanged += new System.EventHandler(this.checkBox2_CheckedChanged);
            // 
            // button1
            // 
            this.button1.Location = new System.Drawing.Point(157, 171);
            this.button1.Name = "button1";
            this.button1.Size = new System.Drawing.Size(67, 25);
            this.button1.TabIndex = 4;
            this.button1.Text = "Start";
            this.button1.UseVisualStyleBackColor = true;
            this.button1.Click += new System.EventHandler(this.button1_Click);
            // 
            // button2
            // 
            this.button2.Location = new System.Drawing.Point(272, 171);
            this.button2.Name = "button2";
            this.button2.Size = new System.Drawing.Size(67, 25);
            this.button2.TabIndex = 5;
            this.button2.Text = "Stop";
            this.button2.UseVisualStyleBackColor = true;
            this.button2.Click += new System.EventHandler(this.button2_Click);
            // 
            // notifyIcon1
            // 
            this.notifyIcon1.Icon = ((System.Drawing.Icon)(resources.GetObject("notifyIcon1.Icon")));
            this.notifyIcon1.Text = "Adobe Converter";
            this.notifyIcon1.Visible = true;
            this.notifyIcon1.MouseClick += new System.Windows.Forms.MouseEventHandler(this.notifyIcon1_MouseClick);
            // 
            // textBox_dir_indesign
            // 
            this.textBox_dir_indesign.Location = new System.Drawing.Point(190, 63);
            this.textBox_dir_indesign.Name = "textBox_dir_indesign";
            this.textBox_dir_indesign.Size = new System.Drawing.Size(164, 21);
            this.textBox_dir_indesign.TabIndex = 6;
            // 
            // textBox_dir_illustrator
            // 
            this.textBox_dir_illustrator.Location = new System.Drawing.Point(190, 109);
            this.textBox_dir_illustrator.Name = "textBox_dir_illustrator";
            this.textBox_dir_illustrator.Size = new System.Drawing.Size(164, 21);
            this.textBox_dir_illustrator.TabIndex = 7;
            // 
            // button3
            // 
            this.button3.Location = new System.Drawing.Point(360, 63);
            this.button3.Name = "button3";
            this.button3.Size = new System.Drawing.Size(58, 21);
            this.button3.TabIndex = 8;
            this.button3.Text = "Browse...";
            this.button3.UseVisualStyleBackColor = true;
            this.button3.Click += new System.EventHandler(this.button3_Click);
            // 
            // button4
            // 
            this.button4.Location = new System.Drawing.Point(360, 109);
            this.button4.Name = "button4";
            this.button4.Size = new System.Drawing.Size(58, 21);
            this.button4.TabIndex = 9;
            this.button4.Text = "Browse...";
            this.button4.UseVisualStyleBackColor = true;
            this.button4.Click += new System.EventHandler(this.button4_Click);
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Font = new System.Drawing.Font("Microsoft Sans Serif", 12F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(134)));
            this.label1.Location = new System.Drawing.Point(104, 24);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(183, 20);
            this.label1.TabIndex = 10;
            this.label1.Text = "Adobe CS4 Converter";
            // 
            // folderBrowserDialog1
            // 
            this.folderBrowserDialog1.Description = "Select InDesign Converter Watching Folder";
            // 
            // folderBrowserDialog2
            // 
            this.folderBrowserDialog2.Description = "Select Illustrator Converter Watching Folder";
            // 
            // label2
            // 
            this.label2.AutoSize = true;
            this.label2.Font = new System.Drawing.Font("Microsoft Sans Serif", 9F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(134)));
            this.label2.Location = new System.Drawing.Point(254, 27);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(0, 15);
            this.label2.TabIndex = 11;
            // 
            // cbAutoStart
            // 
            this.cbAutoStart.AutoSize = true;
            this.cbAutoStart.Location = new System.Drawing.Point(35, 176);
            this.cbAutoStart.Name = "cbAutoStart";
            this.cbAutoStart.Size = new System.Drawing.Size(84, 16);
            this.cbAutoStart.TabIndex = 12;
            this.cbAutoStart.Text = "Auto Start";
            this.cbAutoStart.UseVisualStyleBackColor = true;
            this.cbAutoStart.CheckedChanged += new System.EventHandler(this.cbAutoStart_CheckedChanged);
            // 
            // MainForm
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 12F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(430, 241);
            this.Controls.Add(this.cbAutoStart);
            this.Controls.Add(this.label2);
            this.Controls.Add(this.label1);
            this.Controls.Add(this.button4);
            this.Controls.Add(this.button3);
            this.Controls.Add(this.textBox_dir_illustrator);
            this.Controls.Add(this.textBox_dir_indesign);
            this.Controls.Add(this.button2);
            this.Controls.Add(this.button1);
            this.Controls.Add(this.checkBox2);
            this.Controls.Add(this.checkBox1);
            this.Icon = ((System.Drawing.Icon)(resources.GetObject("$this.Icon")));
            this.MaximizeBox = false;
            this.Name = "MainForm";
            this.ShowInTaskbar = false;
            this.Text = "Adobe CS4 Converter - Version: 8.6.2";
            this.SizeChanged += new System.EventHandler(this.Form1_SizeChanged);
            this.FormClosed += new System.Windows.Forms.FormClosedEventHandler(this.MainForm_FormClosed);
            this.ResumeLayout(false);
            this.PerformLayout();

        }
      


        #endregion

        private System.Windows.Forms.CheckBox checkBox1;
        private System.Windows.Forms.CheckBox checkBox2;
        private System.Windows.Forms.Button button1;
        private System.Windows.Forms.Button button2;
        private System.Windows.Forms.NotifyIcon notifyIcon1;
        private System.Windows.Forms.TextBox textBox_dir_indesign;
        private System.Windows.Forms.TextBox textBox_dir_illustrator;
        private System.Windows.Forms.Button button3;
        private System.Windows.Forms.Button button4;
        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.FolderBrowserDialog folderBrowserDialog1;
        private System.Windows.Forms.FolderBrowserDialog folderBrowserDialog2;
        private System.Windows.Forms.Label label2;
        private System.Windows.Forms.CheckBox cbAutoStart;
    }
}

