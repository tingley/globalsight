using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Text;
using System.Windows.Forms;
using GlobalSight.Common;
using System.IO;

namespace GlobalSight.Office2003Converters
{
    public partial class MainForm : Form
    {
        private Boolean isWordStart = false;
        private Boolean isExcelStart = false;
        private Boolean isPptStart = false;

        private WordConverterUti wordUtil = new WordConverterUti();
        private ExcelConverterUtil excelUtil = new ExcelConverterUtil();
        private PowerPointConverterUtil pptUtil = new PowerPointConverterUtil();


        private void stopConverters()
        {
            if (isWordStart)
            {
                stopWord();
            }

            if (isExcelStart)
            {
                stopExcel();
            }

            if (isPptStart)
            {
                stopPpt();
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
                if (dir.Equals(""))
                {
                    dir = RegistryUtil.GetGlobalSightRegistryValue("MsOfficeConvDir");
                    if (dir == null)
                    {
                        dir = "";
                    }
                    else
                    {
                        dir = dir.Replace("\\\\", "\\");
                    }
                }
                this.textBox1.Text = dir;
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

        private void bWordStart_Click(object sender, EventArgs e)
        {
            startWord();

            setAllButtonStatus();
        }

        private void bWordStop_Click(object sender, EventArgs e)
        {
            stopWord();

            setAllButtonStatus();
        }

        private void setAllButtonStatus()
        {
            this.bAllStop.Enabled = isWordStart || isExcelStart || isPptStart;

            this.bAllStart.Enabled = !isWordStart || !isExcelStart || !isPptStart;
        }

        private void bExcelStart_Click(object sender, EventArgs e)
        {

            startExcel();

            setAllButtonStatus();
        }

        private void bExcelStop_Click(object sender, EventArgs e)
        {
            stopExcel();

            setAllButtonStatus();
        }

        private void bPptStart_Click(object sender, EventArgs e)
        {
            startPpt();

            setAllButtonStatus();
        }

        private void bPptStop_Click(object sender, EventArgs e)
        {
            stopPpt();

            setAllButtonStatus();
        }

        private void startWord()
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
                        wordUtil.start(dir);
                        isWordStart = true;
                        this.bWordStart.Enabled = false;
                        this.bWordStop.Enabled = true;
                    }
                    catch (Exception ex)
                    {
                        MessageBox.Show("Word 2003 Converter can't be started. Exception: \r\n\r\n" + ex.ToString());
                    }
                }
                else
                {
                    MessageBox.Show("Word 2003 Converter can't be started because of directory (" + dir + ") does not exists!");
                }
            }
            else
            {
                MessageBox.Show("Please set the directory first");
            }
        }

        private void stopWord()
        {
            wordUtil.stop();
            isWordStart = false;
            this.bWordStop.Enabled = false;
            this.bWordStart.Enabled = true;
        }

        private void startExcel()
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
                        excelUtil.start(dir);
                        isExcelStart = true;
                        this.bExcelStart.Enabled = false;
                        this.bExcelStop.Enabled = true;
                    }
                    catch (Exception ex)
                    {
                        MessageBox.Show("Excel 2003 Converter can't be started. Exception: \r\n\r\n" + ex.ToString());
                    }
                }
                else
                {
                    MessageBox.Show("Excel 2003 Converter can't be started because of directory (" + dir + ") does not exists!");
                }
            }
            else
            {
                MessageBox.Show("Please set the directory first");
            }
        }

        private void stopExcel()
        {
            excelUtil.stop();
            isExcelStart = false;
            this.bExcelStart.Enabled = true;
            this.bExcelStop.Enabled = false;
        }

        private void startPpt()
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
                        pptUtil.start(dir);
                        isPptStart = true;
                        this.bPptStart.Enabled = false;
                        this.bPptStop.Enabled = true;
                    }
                    catch (Exception ex)
                    {
                        MessageBox.Show("PowerPoint 2003 Converter can't be started. Exception: \r\n\r\n" + ex.ToString());
                    }
                }
                else
                {
                    MessageBox.Show("PowerPoint 2003 Converter can't be started because of directory (" + dir + ") does not exists!");
                }
            }
            else
            {
                MessageBox.Show("Please set the directory first");
            }
        }

        private void stopPpt()
        {
            pptUtil.stop();
            isPptStart = false;
            this.bPptStart.Enabled = true;
            this.bPptStop.Enabled = false;
        }

        private void bAllStart_Click(object sender, EventArgs e)
        {
            if (!isWordStart)
            {
                startWord();
            }

            if (!isExcelStart)
            {
                startExcel();
            }

            if (!isPptStart)
            {
                startPpt();
            }
            

            setAllButtonStatus();
        }

        private void bAllStop_Click(object sender, EventArgs e)
        {
            stopConverters();

            setAllButtonStatus();
        }

        private void MainForm_FormClosing(object sender, FormClosingEventArgs e)
        {
            DialogResult result = MessageBox.Show(this, "Exit Converters?", "Confirm Exit", MessageBoxButtons.OKCancel, MessageBoxIcon.Question);

            e.Cancel = result == DialogResult.Cancel;
        }


    }
}
