using System;
using System.Collections.Generic;
using System.Linq;
using System.Windows.Forms;
using GlobalSight.Common;
using System.Threading;

namespace FrameMakerConverter
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

            if (AppConfig.AutoStart)
            {
                Thread t = new Thread(new ThreadStart(mf.autoStart));
                t.Start();
            }

            Application.Run(mf);
        }
    }
}
