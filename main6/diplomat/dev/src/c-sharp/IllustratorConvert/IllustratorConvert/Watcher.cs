using System;
using System.Collections.Generic;
using System.Text;
using System.IO;
using System.Threading;
using System.ServiceProcess;
using System.Diagnostics;
using System.ComponentModel;
using GlobalSight.Common;


namespace IllustratorConvert
{
    public class Watcher : ServiceBase
    {
        private System.ComponentModel.IContainer components = null;
        private static bool start = true;
        private static Logger m_log = null;
        private static String m_watchDirName = null; 

        public Watcher()
        {
            InitializeComponent();

        }

        static void Main()
        {
            System.ServiceProcess.ServiceBase[] ServicesToRun;
            ServicesToRun = new System.ServiceProcess.ServiceBase[] { new Watcher() };
            System.ServiceProcess.ServiceBase.Run(ServicesToRun);
            // Run();

        }

        private void InitializeComponent()
        {
            this.ServiceName = "GlobalSight Converter - Illustrator";


        }
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        protected override void OnStart(string[] args)
        {
            m_watchDirName = RegistryUtil.GetGlobalSightRegistryValue(
                 "IllustratorConvDir") + @"\Illustrator";
            DirectoryInfo watchDir = new DirectoryInfo(m_watchDirName);
            watchDir.Create();
            Logger.Initialize(m_watchDirName + @"\IllustratorConverter.log");
            m_log = Logger.GetLogger();
            m_log.Log("Illustrator Converter is started");
            start = true;
            Thread thread = new Thread(new ThreadStart(Run));
            thread.Start();

        }
        protected override void OnStop()
        {
            start = false;
          
        }

        public static void Run()
        {
            m_log = Logger.GetLogger();
            m_log.Log("Converter thread is running");
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
      
        }

        // Define the event handlers.
        private static void OnChanged(object source, FileSystemEventArgs e)
        {
            m_log = Logger.GetLogger();
            m_log.Log("File: " + e.FullPath + " ");
            //Console.WriteLine();
            String fullPath = e.FullPath;
            try
            {
                dealChanged(fullPath);
            }
            catch (Exception ex)
            {
                m_log.Log(ex.StackTrace +"  exception");
                dealChanged(fullPath);
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
                m_log.Log(aiFileName);
                iConver.OpenFile(aiFileName, IllustratorConverter.ConversionType.IMPORT);
                iConver.Convert("", "");
                string error = iConver.getErrorInfo();
                m_log.Log(error + " ERROR");
                writeError(error, fullPath);
                string statusName = fullPath.Substring(0, fullPath.LastIndexOf(".")) + ".status";
                File.Move(fullPath, statusName);

            }
            else if (fullPath.Contains("ex_command"))

                {
                    string xmlFileName = fullPath.Substring(0, fullPath.LastIndexOf(".")) + ".xml";
                    iConver.OpenFile(xmlFileName, IllustratorConverter.ConversionType.EXPORT);
                    iConver.Convert("", "");
                    string error = iConver.getErrorInfo();
                    m_log.Log(error + " ERROR");
                    writeError(error, fullPath);
                    string statusName = fullPath.Substring(0, fullPath.LastIndexOf(".")) + ".status";
                    File.Move(fullPath, statusName);
                }
             
        }
       
    }



}
