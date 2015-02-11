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
    public partial class MainForm : Form
    {
        private bool isInddStarted = false;
        private string keyInddDir = "WatchDir.Indesign";
        private string keyIlluDir = "WatchDir.Illustrator";

        public MainForm()
        {
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

            if (!isInddStarted)
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
                            button1.Enabled = false;
                            button2.Enabled = true;
                        }
                        catch (Exception e)
                        {
                            MessageBox.Show(this, "Idml Converter can't be started.");
                        }
                    }
                    else
                    {
                        MessageBox.Show(this, "Idml Converter can't be started because of Directory (" + dirIndd + ") does not exist!");
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
            if (isInddStarted)
            {
                InDesignController.stop();
                isInddStarted = false;
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