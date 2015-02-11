﻿using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Text;
using System.Windows.Forms;
using GlobalSight.Common;
using System.IO;

namespace FrameMakerConverter
{
    public partial class MainForm : Form
    {
        private Boolean isFrameStart = false;

        private FrameMakerConverterUtil frameUtil = new FrameMakerConverterUtil();


        public MainForm()
        {
            InitializeComponent();
            InitData();
        }

        private void InitData()
        {
            try
            {
                // conv dir
                string dir = AppConfig.GetAppConfig("Dir");
                this.tbConv.Text = dir;

                // fm dir
                dir = AppConfig.GetAppConfig("FmDir");
                this.tbFM.Text = dir;
            }
            catch { }
        }

        private void bFrameStart_Click(object sender, EventArgs e)
        {
            startFrame();
        }

        private void bFrameStop_Click(object sender, EventArgs e)
        {
            stopFrame();
        }

        private void startFrame()
        {
            String dir = this.tbConv.Text.Trim();
            String fmdir = this.tbFM.Text.Trim();
            bool isConvOk = false;
            bool isFrameOk = false;

            // check frame dir
            if (!fmdir.Equals(""))
            {
                if (Directory.Exists(fmdir) && File.Exists(fmdir + "\\FrameMaker.exe"))
                {
                    try
                    {
                        AppConfig.UpdateAppConfig("FmDir", fmdir);
                    }
                    catch { }
                    isFrameOk = true;
                }
                else
                {
                    MessageBox.Show("FrameMaker Converter can't be started because of file (" + fmdir + "\\FrameMaker.exe) does not exists!");
                }
            }
            else
            {
                MessageBox.Show("Please set the FrameMaker directory first");
            }

            if (!isFrameOk)
            {
                return;
            }

            // check conv dir
            if (!dir.Equals(""))
            {
                if (Directory.Exists(dir))
                {
                    try
                    {
                        AppConfig.UpdateAppConfig("Dir", dir);
                    }
                    catch { }
                    isConvOk = true;
                }
                else
                {
                    MessageBox.Show("FrameMaker Converter can't be started because of directory (" + dir + ") does not exists!");
                }
            }
            else
            {
                MessageBox.Show("Please set the Conversion directory first");
            }

            if (isConvOk && isFrameOk)
            {
                try
                {
                    frameUtil.start(dir, "\"" + fmdir + "\\FrameMaker.exe\"");
                    isFrameStart = true;
                    this.bFrameStart.Enabled = false;
                    this.bFrameStop.Enabled = true;
                }
                catch (Exception ex)
                {
                    MessageBox.Show("FrameMaker Converter can't be started with exception: " + ex.ToString());
                }
            }
        }

        private void stopFrame()
        {
            frameUtil.stop();
            isFrameStart = false;
            this.bFrameStop.Enabled = false;
            this.bFrameStart.Enabled = true;
        }

        private void bBrowseConv_Click(object sender, EventArgs e)
        {
            if (folderBrowserDialog2.ShowDialog() == DialogResult.OK)
            {
                this.tbConv.Text = folderBrowserDialog2.SelectedPath;
            } 
        }

        private void bBrowseFM_Click(object sender, EventArgs e)
        {
            if (folderBrowserDialog2.ShowDialog() == DialogResult.OK)
            {
                this.tbFM.Text = folderBrowserDialog2.SelectedPath;
            }
        }

        private void MainForm_FormClosing(object sender, FormClosingEventArgs e)
        {
            DialogResult result = MessageBox.Show(this, "Exit Converter?", "Confirm Exit", MessageBoxButtons.OKCancel, MessageBoxIcon.Question);

            e.Cancel = result == DialogResult.Cancel;
        }
    }
}
