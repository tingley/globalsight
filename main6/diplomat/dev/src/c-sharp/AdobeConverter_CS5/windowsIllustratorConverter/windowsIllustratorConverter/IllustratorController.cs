using System;
using System.Collections.Generic;
using System.Text;

namespace windowsIllustratorConverter
{
    public class IllustratorController
    {
        static Watcher watcher = null;
        public static void start(String dir)
        {
            if (watcher == null)
            {
                watcher = new Watcher(dir);
            }
                watcher.OnStart();
            
        }

        public static void stop()
        {
            if (watcher != null)
            {
                watcher.OnStop();
            }
        }
    }
}
