using System;
using System.Collections;
using System.ComponentModel;
using System.Data;
using System.Diagnostics;
using System.ServiceProcess;
using System.IO;
using GlobalSight.Common;

namespace GlobalSight.PowerPointConverter
{
    public class Service1 : System.ServiceProcess.ServiceBase
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.Container components = null;

        private Logger m_log = null;
        private String m_watchDirName = null;
        private bool m_alreadyStarted = false;

        //maintain a separate ConverterRunner for import and export
        private ConverterRunner m_importConverterRunner = null;
        private ConverterRunner m_exportConverterRunner = null;
        private PowerPointConverterImpl m_importConverter = null;
        private PowerPointConverterImpl m_exportConverter = null;

        public Service1()
        {
            // This call is required by the Windows.Forms Component Designer.
            InitializeComponent();

            // TODO: Add any initialization after the InitComponent call
        }

        // The main entry point for the process
        static void Main()
        {
            // for deliver
            RunWithService();

            // for debug
            //RunWithoutService();
        }

        /// <summary>
        /// for deliver with system service
        /// </summary>
        private static void RunWithService()
        {
            // More than one user Service may run within the same process. To add
            // another service to this process, change the following line to
            // create a second service object. For example,
            //
            //   ServicesToRun = New System.ServiceProcess.ServiceBase[] {new Service1(), new MySecondUserService()};
            //

            System.ServiceProcess.ServiceBase[] ServicesToRun;
            ServicesToRun = new System.ServiceProcess.ServiceBase[] { new Service1() };

            System.ServiceProcess.ServiceBase.Run(ServicesToRun);
        }

        /// <summary>
        /// for test with application
        /// </summary>
        private static void RunWithoutService()
        {
            // For test easier
            PowerPointConverterTester tester = new PowerPointConverterTester();
            tester.start();
            // end For test
        }

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            //
            // Service1
            //
            this.ServiceName = "GlobalSight Converter - PowerPoint 2007";

        }

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        protected override void Dispose(bool disposing)
        {
            if (disposing)
            {
                if (components != null)
                {
                    components.Dispose();
                }
            }
            base.Dispose(disposing);
        }

        /// <summary>
        /// Set things in motion so your service can do its work.
        /// </summary>
        protected override void OnStart(string[] args)
        {
            if (m_alreadyStarted == true)
            {
                return;
            }

            m_alreadyStarted = true;

            try
            {
                m_watchDirName = RegistryUtil.GetGlobalSightRegistryValue(
                    "MsOfficeConvDir") + @"\powerpoint";
                DirectoryInfo watchDir = new DirectoryInfo(m_watchDirName);
                watchDir.Create();
                Logger.Initialize(m_watchDirName + @"\powerpoint2007Converter.log");
                m_log = Logger.GetLogger();
                m_log.Log("PowerPoint 2007 Converter starting up.");
                m_log.Log("Creating and starting threads to watch directory " +
                    m_watchDirName);

                m_importConverter = new PowerPointConverterImpl(
                    PowerPointConverterImpl.ConversionType.IMPORT);
                m_exportConverter = new PowerPointConverterImpl(
                    PowerPointConverterImpl.ConversionType.EXPORT);
                m_importConverterRunner = new ConverterRunner(
                    m_importConverter, m_watchDirName);
                m_exportConverterRunner = new ConverterRunner(
                    m_exportConverter, m_watchDirName);

                m_importConverterRunner.Start();
                m_exportConverterRunner.Start();
            }
            catch (Exception e)
            {
                string msg = "PowerPoint 2007 Converter failed to initialize because of: " +
                    e.Message + "\r\n" + e.StackTrace;
                EventLog.WriteEntry(msg, EventLogEntryType.Error);
                Logger.LogWithoutException(msg);
                throw e;
            }
        }

        /// <summary>
        /// Stop this service.
        /// </summary>
        protected override void OnStop()
        {
            // TODO: Add code here to perform any tear-down necessary to stop your service.
            m_log.Log("PowerPoint 2007 Converter shutting down.");
            m_importConverterRunner.Stop();
            m_exportConverterRunner.Stop();
            m_alreadyStarted = false;
            m_importConverter.QuitPowerPoint();
            m_exportConverter.QuitPowerPoint();
        }
    }
}
