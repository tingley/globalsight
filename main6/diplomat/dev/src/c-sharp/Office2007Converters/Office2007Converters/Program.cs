using System;
using System.Collections.Generic;
using System.Linq;
using System.Windows.Forms;
using System.Threading;
using GlobalSight.Common;

namespace GlobalSight.Office2007Converters
{
    static class Program
    {
        /// <summary>
        /// The main entry point for the application.
        /// </summary>
        [STAThread]
        static void Main()
        {
            Application.EnableVisualStyles();
            Application.SetCompatibleTextRenderingDefault(false);
            MainForm mf = new MainForm();

            if (AppConfig.AutoStartAll)
            {
                Thread t = new Thread(new ThreadStart(mf.autoStartAll));
                t.Start();
            }

            Application.Run(mf);
        }
    }
}
