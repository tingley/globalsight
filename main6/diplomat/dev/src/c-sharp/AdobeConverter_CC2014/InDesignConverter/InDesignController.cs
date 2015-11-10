using System;
using System.Collections.Generic;
using System.Text;
using GlobalSight.Common;
using GlobalSight.InDesignConverter;
using System.IO;

namespace InDesignConverter
{
    public class InDesignController
    {
        static ConverterRunner m_importConverterRunner = null;
        static ConverterRunner m_exportConverterRunner = null;
        static ConverterRunner m_previewConverterRunner = null;
        static ConverterRunner m_idmlPreviewConverterRunner = null;
        static ConverterRunner m_inctxrvConverterRunner = null;
        static ConverterRunner m_testRunner = null;

        public static void start(String dir)
        {
            Logger m_log = null;
            String m_watchDirName = dir;
            String m_watchDirNameIndd = null;
            String m_watchDirNameInx = null;
            String m_watchDirNames = null; 
            InDesignApplication m_InDesignApp = null;

            try
            {
                Logger.Initialize(m_watchDirName + @"\AdobeConverter.log");
                m_watchDirNameIndd = m_watchDirName + @"\indd";
                m_watchDirNameInx = m_watchDirName + @"\inx";
                DirectoryInfo watchDirIndd = new DirectoryInfo(m_watchDirNameIndd);
                DirectoryInfo watchDirInx = new DirectoryInfo(m_watchDirNameInx);
                watchDirIndd.Create();
                watchDirInx.Create();
                m_watchDirNames = m_watchDirNameIndd + ";" + m_watchDirNameInx;

                m_log = Logger.GetLogger();
                m_log.EnableDebug = AppConfig.IsDebugEnabled();
                m_log.Log("[Indesign]: GlobalSight InDesign CC 2014 Converter starting up.");
                m_log.Log("[Indesign]: Creating and starting threads to watch directory " +
                    m_watchDirNames);

                m_importConverterRunner = new ConverterRunner(
                    new InDesignConverterImpl(InDesignConverterImpl.ConversionType.IMPORT),
                    m_watchDirNames);
                m_exportConverterRunner = new ConverterRunner(
                    new InDesignConverterImpl(InDesignConverterImpl.ConversionType.EXPORT),
                    m_watchDirNames);
                m_previewConverterRunner = new ConverterRunner(
                    new InDesignConverterImpl(InDesignConverterImpl.ConversionType.PREVIEW),
                    m_watchDirNames);
                m_idmlPreviewConverterRunner = new ConverterRunner(
                    new InDesignConverterImpl(InDesignConverterImpl.ConversionType.IDML_PREVIEW),
                    m_watchDirNames);
                m_inctxrvConverterRunner = new ConverterRunner(
                    new InDesignConverterImpl(InDesignConverterImpl.ConversionType.INCTXRV),
                    m_watchDirNames);
                m_testRunner = new ConverterRunner(
                    new InDesignConverterImpl(InDesignConverterImpl.ConversionType.TEST),
                    m_watchDirNames);

                m_testRunner.SleepSeconds = 5;

                m_importConverterRunner.Start();
                m_exportConverterRunner.Start();
                m_previewConverterRunner.Start();
                m_idmlPreviewConverterRunner.Start();
                m_inctxrvConverterRunner.Start();
                m_testRunner.Start();

                m_InDesignApp = InDesignApplication.getInstance();
            }
            catch (Exception e)
            {
                string msg = "GlobalSight InDesign Converter failed to initialize because of: " +
                    e.Message + "\r\n" + e.StackTrace;
                //  EventLog.WriteEntry(msg, EventLogEntryType.Error);
                Logger.LogWithoutException(msg);
                throw e;
            }
        }

        public static void stop()
        { 
            if(m_importConverterRunner != null)
            {
                m_importConverterRunner.Stop();
            }
            if (m_exportConverterRunner != null)
            {
                m_exportConverterRunner.Stop();
            }
            if (m_previewConverterRunner != null)
            {
                m_previewConverterRunner.Stop();
            }
            if (m_idmlPreviewConverterRunner != null)
            {
                m_idmlPreviewConverterRunner.Stop();
            }
            if (m_inctxrvConverterRunner != null)
            {
                m_inctxrvConverterRunner.Stop();
            }
            if (m_testRunner != null)
            {
                m_inctxrvConverterRunner.Stop();
            }
        }
    }
}
