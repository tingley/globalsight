using System;
using System.Collections.Generic;
using System.Text;
using System.Threading;
using GlobalSight.Common;
using System.IO;

namespace windowsIllustratorConverter
{
    class Watcher
    {
        private static String m_watchDirName;
        private static bool start = true;
        private static Logger m_log = null;
        private Thread thread = null;

        public Watcher(String dir)
        {
            m_watchDirName = dir;
        }

         public  void OnStart()
        {
            //String m_watchDirName = watchDir;
            start = true;
            if (thread == null)
            {
                Logger.Initialize(m_watchDirName + @"\AdobeConverter.log");
                m_watchDirName += @"\illustrator";
                DirectoryInfo watchDir = new DirectoryInfo(m_watchDirName);
                watchDir.Create();
                m_log = Logger.GetLogger();
                m_log.Log("[Illustrator]: Illustrator Converter is started");
                thread = new Thread(new ThreadStart(Run));
                if (!thread.IsAlive)
                {
                    thread.Start();
                }
            }

        }
        public void OnStop()
        {
            start = false;
          
        }

        private static void Run()
        {
            m_log = Logger.GetLogger();
            m_log.Log("[Illustrator]: Converter thread is running");
            FileSystemWatcher watcherForIm = new FileSystemWatcher();
            FileSystemWatcher watcherForEx = new FileSystemWatcher();
            watcherForIm.Path = m_watchDirName;
            watcherForIm.IncludeSubdirectories = true;
            watcherForEx.Path = m_watchDirName;
            watcherForEx.IncludeSubdirectories = true;
            /* Watch for changes in LastAccess and LastWrite times, and 
               the renaming of files or directories. */
            watcherForIm.NotifyFilter = NotifyFilters.LastAccess | NotifyFilters.LastWrite
               | NotifyFilters.FileName | NotifyFilters.DirectoryName;
            watcherForEx.NotifyFilter = NotifyFilters.LastAccess | NotifyFilters.LastWrite
              | NotifyFilters.FileName | NotifyFilters.DirectoryName;
           
            // Only watch im_command.
            watcherForIm.Filter = "*.im_command";

            // Only watch ex_command.
            watcherForEx.Filter = "*.ex_command";
            
            // Add event handlers.
            
                watcherForIm.Created += new FileSystemEventHandler(OnChanged);
                watcherForEx.Created += new FileSystemEventHandler(OnChanged);


                // Begin watching.
                watcherForIm.EnableRaisingEvents = true;
                watcherForEx.EnableRaisingEvents = true;
            

            //while(start);
      
        }

        // Define the event handlers.
        private static void OnChanged(object source, FileSystemEventArgs e)
        {
            if (start)
            {
                m_log = Logger.GetLogger();
                m_log.Log("[Illustrator]: File: " + e.FullPath + " be found.");
                //Console.WriteLine();
                String fullPath = e.FullPath;
                try
                {
                    dealChanged(fullPath);
                }
                catch (Exception ex)
                {
                    m_log.Log("[Illustrator]: "+ ex.StackTrace + "  exception");
                    dealChanged(fullPath);
                }
            }
        }
        private static void writeError(string error, string fileName)
        {
            FileStream fs = new FileStream(fileName, FileMode.Create, FileAccess.Write);
            StreamWriter sw = new StreamWriter(fs);
            sw.WriteLine("error=" + error);
            sw.Flush();
            sw.Close();
            fs.Close();
        }

        private static void dealChanged(String fullPath)
        {
           
            IllustratorConverter iConver = new IllustratorConverter();
            m_log = Logger.GetLogger();
           
            if (fullPath.Contains("im_command"))
            {
                string aiFileName = fullPath.Substring(0, fullPath.LastIndexOf(".")) + ".ai";
                m_log.Log("[Illustrator]: Start to convert " + aiFileName);
                iConver.OpenFile(aiFileName, IllustratorConverter.ConversionType.IMPORT);
                iConver.Convert("", "");
                string error = iConver.getErrorInfo();
                m_log.Log("[Illustrator]: " + error + " ERROR");
                writeError(error, fullPath);
                string statusName = fullPath.Substring(0, fullPath.LastIndexOf(".")) + ".status";
                File.Move(fullPath, statusName);

            }
            else if (fullPath.Contains("ex_command"))

                {
                    string xmlFileName = fullPath.Substring(0, fullPath.LastIndexOf(".")) + ".xml";
                    m_log.Log("[Illustrator]: Start to write " + xmlFileName + " to ai files");
                    iConver.OpenFile(xmlFileName, IllustratorConverter.ConversionType.EXPORT);
                    iConver.Convert("", "");
                    string error = iConver.getErrorInfo();
                    m_log.Log("[Illustrator]: " + error + " ERROR");
                    writeError(error, fullPath);
                    string statusName = fullPath.Substring(0, fullPath.LastIndexOf(".")) + ".status";
                    File.Move(fullPath, statusName);
                }
             
        }
       
    }
}

