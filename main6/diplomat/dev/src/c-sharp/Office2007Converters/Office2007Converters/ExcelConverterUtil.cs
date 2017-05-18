using System.Collections.Generic;
using System.Text;
using System;
using System.Collections;
using System.ComponentModel;
using System.Data;
using System.Diagnostics;
using System.IO;
using GlobalSight.Common;

namespace GlobalSight.Office2007Converters
{
    class ExcelConverterUtil
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
                String excelDir = dir + "\\excel";
                DirectoryInfo watchDir = new DirectoryInfo(excelDir);
                watchDir.Create();

                if (m_log == null)
                {
                    m_log = Logger.Initialize(excelDir + @"\excel2007Converter.log");
                }

                m_log.Log("Excel 2007 Converter starting up.");
                m_log.Log("Creating and starting threads to watch directory " +
                    excelDir);

                m_importConverterRunner = new ConverterRunner(
                    new ExcelConverterImpl(ExcelConverterImpl.ConversionType.IMPORT), excelDir, m_log);
                m_exportConverterRunner = new ConverterRunner(
                    new ExcelConverterImpl(ExcelConverterImpl.ConversionType.EXPORT), excelDir, m_log);

                m_importConverterRunner.Start();
                m_exportConverterRunner.Start();
            }
            catch (Exception e)
            {
                string msg = "Excel 2007 Converter failed to initialize because of: " +
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
