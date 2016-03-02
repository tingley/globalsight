using System.Collections.Generic;
using System.Text;
using System;
using System.Collections;
using System.ComponentModel;
using System.Data;
using System.Diagnostics;
using System.IO;
using GlobalSight.Common;

namespace GlobalSight.WinPEConverter
{
    class WinPEConverterUtil
    {
        private static Logger m_log = null;
        //maintain a separate ConverterRunner for import and export
        private ConverterRunner m_importConverterRunner = null;
        private ConverterRunner m_exportConverterRunner = null;

        public void stop()
        {
            if (m_importConverterRunner != null)
            {
                m_importConverterRunner.Stop();
            }

            if (m_exportConverterRunner != null)
            {
                m_exportConverterRunner.Stop();
            }
        }

        public void start(String dir)
        {
            try
            {
                String winpeDir = dir + "\\winpe";
                DirectoryInfo watchDir = new DirectoryInfo(winpeDir);
                watchDir.Create();

                if (m_log == null)
                {
                    m_log = Logger.Initialize(winpeDir + @"\WindowsPEConverter.log");
                }

                m_log.Log("WindowsPEConverter starting up.");
                m_log.Log("Creating and starting threads to watch directory " +
                    dir);

                m_importConverterRunner = new ConverterRunner(
                    new WinPEConverterImpl(WinPEConverterImpl.ConversionType.IMPORT),
                    winpeDir, m_log);
                m_exportConverterRunner = new ConverterRunner(
                    new WinPEConverterImpl(WinPEConverterImpl.ConversionType.EXPORT),
                    winpeDir, m_log);

                m_importConverterRunner.Start();
                m_exportConverterRunner.Start();
            }
            catch (Exception e)
            {
                string msg = "WindowsPEConverter failed to initialize because of: " +
                    e.Message + "\r\n" + e.StackTrace;
                Logger.LogWithoutException(m_log, msg);
                throw e;
            }
        }

        public static Logger GetLogger()
        {
            return m_log;
        }
    }
}
