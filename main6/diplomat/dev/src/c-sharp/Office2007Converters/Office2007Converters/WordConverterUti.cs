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
    class WordConverterUti
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
                String wordDir = dir + "\\word";
                DirectoryInfo watchDir = new DirectoryInfo(wordDir);
                watchDir.Create();

                if (m_log == null)
                {
                    m_log = Logger.Initialize(wordDir + @"\word2007Converter.log");
                }
                
                m_log.Log("Word 2007 Converter starting up.");
                m_log.Log("Creating and starting threads to watch directory " +
                    dir);

                m_importConverterRunner = new ConverterRunner(
                    new WordConverterImpl(WordConverterImpl.ConversionType.IMPORT),
                    wordDir, m_log);
                m_exportConverterRunner = new ConverterRunner(
                    new WordConverterImpl(WordConverterImpl.ConversionType.EXPORT),
                    wordDir, m_log);

                m_importConverterRunner.Start();
                m_exportConverterRunner.Start();
            }
            catch (Exception e)
            {
                string msg = "Word 2007 Converter failed to initialize because of: " +
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
