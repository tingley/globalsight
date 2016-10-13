using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Text;
using System.Windows.Forms;
using GlobalSight.Common;
using System.IO;

namespace GlobalSight.WinPEConverter
{
    public partial class MainForm : Form
    {
        private Boolean isPEStart = false;

        private WinPEConverterUtil peUtil = new WinPEConverterUtil();


        private void stopConverters()
        {
            if (isPEStart)
            {
                stopPE();
            }
        }

        public MainForm()
        {
            InitializeComponent();
            InitData();
        }

        private void InitData()
        {
            try
            {
                string dir = AppConfig.GetAppConfig("Dir");
                this.textBox1.Text = dir;
                this.cbAutoStart.Checked = AppConfig.AutoStart;
            }
            catch { }
        }

        private void button1_Click(object sender, EventArgs e)
        {
            if (folderBrowserDialog2.ShowDialog() == DialogResult.OK)
            {
                this.textBox1.Text = folderBrowserDialog2.SelectedPath;
            }          
        }

        private void startPE(List<String> paras)
        {
            String dir = this.textBox1.Text.Trim();

            if (!dir.Equals(""))
            {
                if (Directory.Exists(dir))
                {
                    try
                    {
                        AppConfig.UpdateAppConfig("Dir", dir);
                    }
                    catch { }
                    try
                    {
                        peUtil.start(dir);
                        isPEStart = true;
                    }
                    catch (Exception ex)
                    {
                        MessageBox.Show(this, "Windows Portable Executable Converter can't be started. Exception: \r\n\r\n" + ex.ToString());
                    }
                }
                else
                {
                    if (paras == null || paras.Count == 0)
                    {
                        if (paras != null)
                            paras.Add("alerted");
                        MessageBox.Show(this, "Windows Portable Executable Converter can't be started because of directory (" + dir + ") does not exists!");
                    }
                }
            }
            else
            {
                if (paras == null || paras.Count == 0)
                {
                    if (paras != null)
                        paras.Add("alerted");
                    MessageBox.Show(this, "Please set the directory first");
                }
            }
        }

        private void stopPE()
        {
            peUtil.stop();
            isPEStart = false;
        }

        private void bStart_Click(object sender, EventArgs e)
        {
            startAllConverters();
            setAllButtonStatus();
        }

        private void startAllConverters()
        {
            List<String> paras = new List<string>();

            if (!isPEStart)
            {
                startPE(paras);
            }

            setAllButtonStatus();
        }

        private void bStop_Click(object sender, EventArgs e)
        {
            stopConverters();
            setAllButtonStatus();
        }

        private void MainForm_FormClosing(object sender, FormClosingEventArgs e)
        {
            DialogResult result = MessageBox.Show(this, "Exit Converter?", "Confirm Exit", MessageBoxButtons.OKCancel, MessageBoxIcon.Question);

            if (result == DialogResult.Cancel)
            {
                e.Cancel = true;
            }
            else
            {
                stopConverters();
                setAllButtonStatus();
            }
        }

        private void cbAutoStart_CheckedChanged(object sender, EventArgs e)
        {
            AppConfig.AutoStart = cbAutoStart.Checked;
        }

        public void autoStart()
        {
            this.cbAutoStart.Checked = AppConfig.AutoStart;
            string dir = AppConfig.GetAppConfig("Dir");
            if (dir == null || "".Equals(dir.Trim()))
            {
                return;
            }

            String oriText = this.cbAutoStart.Text;
            this.cbAutoStart.Text = "Auto Starting...";
            this.cbAutoStart.Enabled = false;

            startAllConverters();

            this.cbAutoStart.Text = oriText;
            this.cbAutoStart.Enabled = true;
        }

        private void setAllButtonStatus()
        {
            this.bStop.Enabled = isPEStart;
            this.bStart.Enabled = !isPEStart;
        }
    }
}
