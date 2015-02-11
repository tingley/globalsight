using System.Collections.Generic;
using System.Text;
using System;
using System.Collections;
using System.ComponentModel;
using System.Data;
using System.Diagnostics;
using System.ServiceProcess;
using System.IO;
using GlobalSight.Common;

namespace GlobalSight.WordConverter
{
    class WordConverterTester
    {
        private String m_watchDirName = "D:\\WINFILES\\word";
        private Logger m_log = null;
        //maintain a separate ConverterRunner for import and export
        private ConverterRunner m_importConverterRunner = null;
        private ConverterRunner m_exportConverterRunner = null;

        //static void main()
        //{
            
        //}

        public void start()
        {
            try
            {
                DirectoryInfo watchDir = new DirectoryInfo(m_watchDirName);
                watchDir.Create();

                Logger.Initialize(m_watchDirName + @"\word2007Converter.log");

                m_log = Logger.GetLogger();
                m_log.Log("Word 2007 Converter starting up.");
                m_log.Log("Creating and starting threads to watch directory " +
                    m_watchDirName);

                m_importConverterRunner = new ConverterRunner(
                    new WordConverterImpl(WordConverterImpl.ConversionType.IMPORT),
                    m_watchDirName);
                m_exportConverterRunner = new ConverterRunner(
                    new WordConverterImpl(WordConverterImpl.ConversionType.EXPORT),
                    m_watchDirName);

                m_importConverterRunner.Start();
                m_exportConverterRunner.Start();
            }
            catch (Exception e)
            {
                string msg = "Word 2007 Converter failed to initialize because of: " +
                    e.Message + "\r\n" + e.StackTrace;
                Logger.LogWithoutException(msg);
                throw e;
            }
        }
    }
}
