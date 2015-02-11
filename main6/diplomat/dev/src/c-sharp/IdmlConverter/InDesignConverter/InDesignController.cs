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

        public static void start(String dir)
        {
            Logger m_log = null;
            String m_watchDirName = dir;
            String m_watchDirNameIndd = null;
            String m_watchDirNames = null; 
            InDesignApplication m_InDesignApp = null;

            try
            {
                m_watchDirNameIndd = m_watchDirName + @"\idml";
                DirectoryInfo watchDirIndd = new DirectoryInfo(m_watchDirNameIndd);
                watchDirIndd.Create();
                Logger.Initialize(m_watchDirNameIndd + @"\IdmlConverter.log");
                m_watchDirNames = m_watchDirNameIndd;

                m_log = Logger.GetLogger();
                m_log.EnableDebug = AppConfig.IsDebugEnabled();
                m_log.Log("[Idml]: GlobalSight Idml Converter starting up.");
                m_log.Log("[Idml]: Creating and starting threads to watch directory " +
                    m_watchDirNames);

                m_importConverterRunner = new ConverterRunner(
                    new InDesignConverterImpl(InDesignConverterImpl.ConversionType.IMPORT),
                    m_watchDirNames);

                m_importConverterRunner.Start();

                m_InDesignApp = InDesignApplication.getInstance();
            }
            catch (Exception e)
            {
                string msg = "GlobalSight Idml Converter failed to initialize because of: " +
                    e.Message + "\r\n" + e.StackTrace;
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
        }
    }
}
