using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Text;
using System.Windows.Forms;

namespace windowsIllustratorConverter
{
    public partial class Form1 : Form
    {
        Watcher watcher = null;
        public Form1()
        {
            InitializeComponent();
        }
         
        private void button1_Click(object sender, EventArgs e)
        {
            
            String dir = this.textBox1.Text;
            if (!dir.Trim().Equals(""))
            {
                watcher = new Watcher(dir);
                watcher.OnStart();
                this.button1.Enabled = false;
               
            }

        }

        private void button2_Click(object sender, EventArgs e)
        {
            if (watcher != null)
            {
                watcher.OnStop();
                this.button1.Enabled = true;
            }
        }

        
    }
}