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

namespace WindowsApplication1
{
    public partial class Form1 : Form
    {
        private bool isInddStarted = false;
        private bool isIllustratorStarted = false;

        public Form1()
        {
            InitializeComponent();
        }

        private void button1_Click(object sender, EventArgs e)
        {
            String dirIndd = textBox1.Text;
            String dirIllustrator = textBox2.Text;
            bool isIndd = checkBox1.Checked;
            bool isIllustrator = checkBox2.Checked;

            if (!dirIndd.Trim().Equals("") && !isInddStarted
                 && isIndd)
            {
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
                    MessageBox.Show("InDesign Converter can't be started.");
                }
            }
            if (!dirIllustrator.Trim().Equals("") && !isIllustratorStarted
                 && isIllustrator)
            {
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
                    MessageBox.Show("Illustrator Converter can't be started.");
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
                this.Hide();
                this.notifyIcon1.Visible = true; 
            }
        }

        private void notifyIcon1_Click(object sender, EventArgs e)
        {
            this.Visible = true;

            this.WindowState = FormWindowState.Normal;

            this.notifyIcon1.Visible = true;
        }

        private void button3_Click(object sender, EventArgs e)
        {
            //this.folderBrowserDialog1.ShowDialog();
            if (folderBrowserDialog1.ShowDialog() == DialogResult.OK)
            {
                textBox1.Text = folderBrowserDialog1.SelectedPath;
            }
        }

        private void button4_Click(object sender, EventArgs e)
        {
            //this.folderBrowserDialog2.ShowDialog();
            if (folderBrowserDialog2.ShowDialog() == DialogResult.OK)
            {
                this.textBox2.Text = folderBrowserDialog2.SelectedPath;
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

            if((inddSelected && isInddStarted && !illustratorSelected)
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

            if((inddSelected && isInddStarted && !illustratorSelected)
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

        private void Form1_Close(object sender, EventArgs e)
        {
            Application.Exit();
            Process[] pary = Process.GetProcessesByName(Process.GetCurrentProcess().ProcessName);
            foreach (Process p in pary)
            {
                p.Kill();
            }
            
        }   

    }
}