using System.Collections.Generic;
using System.Text;
using System;
using System.Collections;
using System.ComponentModel;
using System.Diagnostics;
using System.IO;
using GlobalSight.Common;

namespace GlobalSight.Office2003Converters
{
    class PowerPointConverterUtil
    {
        private Logger m_log = null;
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
                String pptDir = dir + "\\powerpoint";
                DirectoryInfo watchDir = new DirectoryInfo(pptDir);
                watchDir.Create();

                if (m_log == null)
                {
                    Logger.Initialize(pptDir + @"\powerpoint2003Converter.log");
                    m_log = Logger.GetLogger();
                }

                m_log.Log("PowerPoint 2003 Converter starting up.");
                m_log.Log("Creating and starting threads to watch directory " +
                    pptDir);


                m_importConverterRunner = new ConverterRunner(
                   new PowerPointConverterImpl(PowerPointConverterImpl.ConversionType.IMPORT), pptDir);
                m_exportConverterRunner = new ConverterRunner(
                    new PowerPointConverterImpl(PowerPointConverterImpl.ConversionType.EXPORT), pptDir);

                m_importConverterRunner.Start();
                m_exportConverterRunner.Start();
            }
            catch (Exception e)
            {
                string msg = "PowerPoint 2003 Converter failed to initialize because of: " +
                    e.Message + "\r\n" + e.StackTrace;
                Logger.LogWithoutException(msg);
                throw e;
            }
        }
    }
}
