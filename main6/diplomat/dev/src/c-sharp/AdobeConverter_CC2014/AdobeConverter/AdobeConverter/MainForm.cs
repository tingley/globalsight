using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Diagnostics;
using System.Drawing;
using System.Text;
using System.Windows.Forms;
using InDesignConverter;
using System.Configuration;
using GlobalSight.Common;
using System.IO;

namespace GlobalSight.AdobeConverter
{
    public enum StatusColor
    {
        success,
        failed,
        normal
    }
    public partial class MainForm : Form
    {
        private bool isInddStarted = false;
        private string keyInddDir = "WatchDir.Indesign";

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

                this.textBox_dir_indesign.Text = inddDir;
                this.cbAutoStart.Checked = AppConfig.AutoStart;
            }
            catch { }
        }

        private void button1_Click(object sender, EventArgs e)
        {
            startConverter();
        }

        private void showStatus(String msg, StatusColor color)
        {
            toolStripStatusLabel2.Text = msg;

            switch (color)
            {
                case StatusColor.failed:
                    toolStripStatusLabel2.ForeColor = Color.Red;
                    break;

                case StatusColor.success:
                    toolStripStatusLabel2.ForeColor = Color.Green;
                    break;

                default:
                    toolStripStatusLabel2.ForeColor = Color.Black;
                    break;
            }
        }

        private void startConverter()
        {
            String dirIndd = textBox_dir_indesign.Text;
            bool isIndd = true;

            if (isIndd)
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
                            showStatus("Starting converter...", StatusColor.normal);
                            InDesignController.start(dirIndd);
                            isInddStarted = true;
                            button1.Enabled = false;
                            button2.Enabled = true;
                            //MessageBox.Show("InDesign Converter start successfully.");
                            showStatus("Converter started.", StatusColor.success);
                        }
                        catch (Exception ex)
                        {
                            MessageBox.Show(this, "InDesign Converter can't be started.");
                            showStatus("Error occur, please check the log.", StatusColor.failed);
                        }
                    }
                    else
                    {
                        MessageBox.Show(this, "InDesign Converter can't be started because of Directory (" + dirIndd + ") does not exists!");
                        showStatus("Conversion directory does not exists!", StatusColor.failed);
                    }
                }
                else
                {
                    MessageBox.Show(this, "Please set the conversion directory first");
                    showStatus("Please set the conversion directory first.", StatusColor.failed);
                }
            }
        }

        private void button2_Click(object sender, EventArgs e)
        {
            bool isIndd = true;

            if (isInddStarted && isIndd)
            {
                showStatus("Stopping converter...", StatusColor.normal);
                InDesignController.stop();
                isInddStarted = false;
                button2.Enabled = false;
                button1.Enabled = true;
                showStatus("Converter stopped.", StatusColor.normal);
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

            startConverter();

            this.cbAutoStart.Text = oriText;
            this.cbAutoStart.Enabled = true;
        }
    }
}