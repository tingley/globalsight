using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Diagnostics;
using System.Drawing;
using System.Text;
using System.Windows.Forms;
using InDesignConverter;
using windowsIllustratorConverter;
using System.Configuration;
using GlobalSight.Common;
using System.IO;

namespace GlobalSight.AdobeConverter
{
    public partial class MainForm : Form
    {
        private bool isInddStarted = false;
        private bool isIllustratorStarted = false;
        private string keyInddDir = "WatchDir.Indesign";
        private string keyIlluDir = "WatchDir.Illustrator";

        public MainForm()
        {
            Form.CheckForIllegalCrossThreadCalls = false;

            InitializeComponent();

            InitData();
        }

        public void InitData()
        {
            try
            {
                string inddDir = AppConfig.GetAppConfig(keyInddDir);
                string illuDir = AppConfig.GetAppConfig(keyIlluDir);

                this.textBox_dir_indesign.Text = inddDir;
                this.textBox_dir_illustrator.Text = illuDir;
                this.cbAutoStart.Checked = AppConfig.AutoStart;
            }
            catch { }
        }

        private void button1_Click(object sender, EventArgs e)
        {
            startConverter();
        }

        private void startConverter()
        {
            String dirIndd = textBox_dir_indesign.Text;
            String dirIllustrator = textBox_dir_illustrator.Text;
            bool isIndd = checkBox1.Checked;
            bool isIllustrator = checkBox2.Checked;

            if (!isInddStarted && isIndd)
            {
                if (!dirIndd.Trim().Equals(""))
                {
                    if (Directory.Exists(dirIndd))
                    {
                        try
                        {
                            AppConfig.UpdateAppConfig(keyInddDir, dirIndd);
                        }
                        catch { }
                        try
                        {
                            InDesignController.start(dirIndd);
                            isInddStarted = true;
                            this.checkBox1.ForeColor = System.Drawing.Color.Green;
                            button1.Enabled = false;
                            button2.Enabled = true;
                            //MessageBox.Show("InDesign Converter start successfully.");
                        }
                        catch (Exception ex)
                        {
                            MessageBox.Show(this, "InDesign Converter can't be started.");
                        }
                    }
                    else
                    {
                        MessageBox.Show(this, "InDesign Converter can't be started because of Directory (" + dirIndd + ") does not exists!");
                    }
                }
                else
                {
                    MessageBox.Show(this, "Please set the conversion directory first");
                }
            }
            if (!isIllustratorStarted && isIllustrator)
            {
                if (!dirIllustrator.Trim().Equals(""))
                {
                    if (Directory.Exists(dirIllustrator))
                    {
                        try
                        {
                            AppConfig.UpdateAppConfig(keyIlluDir, dirIllustrator);
                        }
                        catch { }
                        try
                        {
                            IllustratorController.start(dirIllustrator);
                            isIllustratorStarted = true;
                            this.checkBox2.ForeColor = System.Drawing.Color.Green;
                            button1.Enabled = false;
                            button2.Enabled = true;
                        }
                        catch (Exception em)
                        {
                            MessageBox.Show(this, "Illustrator Converter can't be started.");
                        }
                    }
                    else
                    {
                        MessageBox.Show(this, "Illustrator Converter can't be started because of Directory (" + dirIllustrator + ") does not exists!");
                    }
                }
                else
                {
                    MessageBox.Show(this, "Please set the conversion directory first");
                }
            }
        }

        private void button2_Click(object sender, EventArgs e)
        {
            bool isIndd = checkBox1.Checked;
            bool isIllustrator = checkBox2.Checked;

            if (isInddStarted && isIndd)
            {
                InDesignController.stop();
                isInddStarted = false;
                this.checkBox1.ForeColor = System.Drawing.Color.OrangeRed;
                button2.Enabled = false;
                button1.Enabled = true;
            }

            if (isIllustratorStarted && isIllustrator)
            {
                IllustratorController.stop();
                isIllustratorStarted = false;
                this.checkBox2.ForeColor = System.Drawing.Color.OrangeRed;
                button2.Enabled = false;
                button1.Enabled = true;
            }

        }


        private void Form1_SizeChanged(object sender, EventArgs e)
        {
            if (this.WindowState == FormWindowState.Minimized)
            {
                this.notifyIcon1.Visible = true;
                this.Hide();
            }
        }

        private void button3_Click(object sender, EventArgs e)
        {
            //this.folderBrowserDialog1.ShowDialog();
            if (folderBrowserDialog1.ShowDialog() == DialogResult.OK)
            {
                textBox_dir_indesign.Text = folderBrowserDialog1.SelectedPath;
            }
        }

        private void button4_Click(object sender, EventArgs e)
        {
            //this.folderBrowserDialog2.ShowDialog();
            if (folderBrowserDialog2.ShowDialog() == DialogResult.OK)
            {
                this.textBox_dir_illustrator.Text = folderBrowserDialog2.SelectedPath;
            }
        }

        private void checkBox1_CheckedChanged(object sender, EventArgs e)
        {
            bool inddSelected = checkBox1.Checked;
            bool illustratorSelected = checkBox2.Checked;
            if ((inddSelected && !isInddStarted && !illustratorSelected)
                 || (inddSelected && !isInddStarted && illustratorSelected && !isIllustratorStarted)
                 || (!inddSelected && !illustratorSelected)
                 || (!inddSelected && illustratorSelected && !isIllustratorStarted))
            {
                button1.Enabled = true;
            }
            else
            {
                button1.Enabled = false;
            }

            if ((inddSelected && isInddStarted && !illustratorSelected)
                 || (inddSelected && isInddStarted && illustratorSelected && isIllustratorStarted)
                 || (!inddSelected && !illustratorSelected)
                 || (!inddSelected && illustratorSelected && isIllustratorStarted))
            {
                button2.Enabled = true;
            }
            else
            {
                button2.Enabled = false;
            }

        }

        private void checkBox2_CheckedChanged(object sender, EventArgs e)
        {
            bool inddSelected = checkBox1.Checked;
            bool illustratorSelected = checkBox2.Checked;
            if ((inddSelected && !isInddStarted && !illustratorSelected)
                 || (inddSelected && !isInddStarted && illustratorSelected && !isIllustratorStarted)
                 || (!inddSelected && !illustratorSelected)
                 || (!inddSelected && illustratorSelected && !isIllustratorStarted))
            {
                button1.Enabled = true;
            }
            else
            {
                button1.Enabled = false;
            }

            if ((inddSelected && isInddStarted && !illustratorSelected)
                 || (inddSelected && isInddStarted && illustratorSelected && isIllustratorStarted)
                 || (!inddSelected && !illustratorSelected)
                 || (!inddSelected && illustratorSelected && isIllustratorStarted))
            {
                button2.Enabled = true;
            }
            else
            {
                button2.Enabled = false;
            }

        }

        private void notifyIcon1_MouseClick(object sender, MouseEventArgs e)
        {
            this.Show();
            this.WindowState = FormWindowState.Normal;
            this.Show();
            this.Activate();
        }

        private void MainForm_FormClosed(object sender, FormClosedEventArgs e)
        {
            InDesignController.stop();
            IllustratorController.stop();
            this.notifyIcon1.Visible = false;

            Application.Exit();
            Process[] pary = Process.GetProcessesByName(Process.GetCurrentProcess().ProcessName);
            foreach (Process p in pary)
            {
                p.Kill();
            }
        }

        private void cbAutoStart_CheckedChanged(object sender, EventArgs e)
        {
            AppConfig.AutoStart = cbAutoStart.Checked;
        }

        public void autoStart()
        {
            this.cbAutoStart.Checked = AppConfig.AutoStart;
            string dir = AppConfig.GetAppConfig(keyInddDir);
            if (dir == null || "".Equals(dir.Trim()))
            {
                return;
            }

            String oriText = this.cbAutoStart.Text;
            this.cbAutoStart.Text = "Auto Starting...";
            this.cbAutoStart.Enabled = false;
            checkBox1.Checked = true;

            startConverter();

            this.cbAutoStart.Text = oriText;
            this.cbAutoStart.Enabled = true;
        }        
    }
}