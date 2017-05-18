using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using GlobalSight.Common;
using System.IO;
using System.Threading;

namespace FrameMakerConverter
{
    class FrameMakerConverterUtil
    {
        private Logger m_log = null;

        private ConverterRunner m_ConverterRunner = null;
        private Thread closeThread = null;

        public void stop()
        {
            if (m_ConverterRunner != null)
            {
                m_ConverterRunner.Stop();
            }

            if (m_log != null)
            {
                m_log.Log("FrameMaker Converter stopped.");
            }

            if (closeThread != null)
            {
                try
                {
                    closeThread.Abort();
                }
                catch { }
            }
        }

        public void start(String dir, String fmExePath)
        {
            try
            {
                closeThread = new Thread(new ThreadStart(CloseFrameMakerWindow));
                closeThread.Start();

                String inputDir = dir + "\\FrameMaker9";
                DirectoryInfo watchDir = new DirectoryInfo(inputDir);
                watchDir.Create();

                if (m_log == null)
                {
                    Logger.Initialize(inputDir + @"\FrameMakerConverter.log");
                    m_log = Logger.GetLogger();
                }

                m_log.Log("FrameMaker Converter starting up.");
                m_log.Log("Creating and starting threads to watch directory " +
                    dir);

                m_ConverterRunner = new ConverterRunner(
                    new FrameMakerConverterImpl(fmExePath), inputDir);

                m_ConverterRunner.Start();
            }
            catch (Exception e)
            {
                string msg = "FrameMaker Converter failed to initialize because of: " +
                    e.Message + "\r\n" + e.StackTrace;
                Logger.LogWithoutException(msg);
                throw e;
            }
        }

        private void CloseFrameMakerWindow()
        {
            string FM_WINDOW_NAME = "FrameMaker";
            string FM_MISS_FILE = "Missing File";
            int sleepTime = 3000;

            while (true)
            {
                IntPtr hwnd1 = Win32Pinvoker.FindWindow(null, FM_WINDOW_NAME);
                IntPtr hwnd2 = Win32Pinvoker.FindWindow(null, FM_MISS_FILE);

                if (hwnd1 == IntPtr.Zero && hwnd2 == IntPtr.Zero)
                {
                    Thread.Sleep(sleepTime);
                }
                else
                {
                    bool findButton = false;
                    if (hwnd1 != IntPtr.Zero)
                    {
                        IntPtr h1 = Win32Pinvoker.FindWindowEx(hwnd1, IntPtr.Zero, null, "OK");
                        if (h1 != IntPtr.Zero)
                        {
                            findButton = true;
                            Win32Pinvoker.ClickButtonAndClose(h1);
                        }

                        h1 = Win32Pinvoker.FindWindowEx(hwnd1, IntPtr.Zero, null, "确定");
                        if (h1 != IntPtr.Zero)
                        {
                            findButton = true;
                            Win32Pinvoker.ClickButtonAndClose(h1);
                        }

                        h1 = Win32Pinvoker.FindWindowEx(hwnd1, IntPtr.Zero, null, "Yes");
                        if (h1 != IntPtr.Zero)
                        {
                            findButton = true;
                            Win32Pinvoker.ClickButtonAndClose(h1);
                        }
                    }

                    if (hwnd2 != IntPtr.Zero)
                    {
                        IntPtr hRadioParent = Win32Pinvoker.FindWindowEx(hwnd2, IntPtr.Zero, "#32770", "");

                        IntPtr h1 = Win32Pinvoker.FindWindowEx(hRadioParent, IntPtr.Zero, null, "Ignore &All Missing Files");
                        if (h1 != IntPtr.Zero)
                        {
                            findButton = true;
                            Win32Pinvoker.ClickButtonAndClose(h1);
                        }

                        h1 = Win32Pinvoker.FindWindowEx(hwnd2, IntPtr.Zero, null, "&Continue");
                        if (h1 != IntPtr.Zero)
                        {
                            findButton = true;
                            Win32Pinvoker.ClickButtonAndClose(h1);
                        }
                    }

                    if (!findButton)
                    {
                        Thread.Sleep(sleepTime);
                    }
                }
            }
        }
    }
}
