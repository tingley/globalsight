using System;
using System.Collections.Generic;
using System.Text;
using Illustrator;
using GlobalSight.Common;
using System.Threading;

namespace windowsIllustratorConverter
{
    class IllustratorApp
    {
        private static Illustrator.Application m_app;
        private const string DIALOG_CLASSNAME = "#32770";
        private const string DIALOG_TITLE = "Adobe Illustrator CS2";
        private static bool started = true;
        private static IllustratorApp app = null;

        private IllustratorApp()
        {
            init();
        }
        public static IllustratorApp getInstance()
        {
            return app = new IllustratorApp();
        }


        public void startApp()
        {

        }

        public void restartApp()
        {
            m_app.Quit();
            while (true)
            {
                try
                {
                    String version = m_app.Version;

                }
                catch (Exception ex)
                {
                    init();
                    return;
                }
            }

        }

        public Illustrator.Document open(String fileName)
        {
            Illustrator.OpenOptions openOptions = new Illustrator.OpenOptionsClass();
            return m_app.Open(fileName, Illustrator.AiDocumentColorSpace.aiDocumentCMYKColor, openOptions);
        }

        private void init()
        {
            started = true;
            Thread thread1 = new Thread(new ThreadStart(clickButton));
            thread1.Start();
            Thread thread2 = new Thread(new ThreadStart(clickErrorButton));
            thread2.Start();
            Type myObjType;
            object myObjValue;
            myObjType = Type.GetTypeFromProgID("Illustrator.Application.3");
            myObjValue = Activator.CreateInstance(myObjType);
            m_app = (Illustrator.Application)myObjValue;
            started = false;
        }

        private static void clickButton()
        {
            while (started)
            {
                IntPtr hwnd = Win32Pinvoker.FindWindow(DIALOG_CLASSNAME, DIALOG_TITLE);
                if (hwnd == IntPtr.Zero)
                {
                    Thread.Sleep(2000);
                }
                else
                {
                    IntPtr hwnd1 = Win32Pinvoker.FindWindowEx(hwnd, IntPtr.Zero, "Button", "Launch");
                    if (hwnd1 != IntPtr.Zero)
                    {
                        Win32Pinvoker.ClosePopupDialog(hwnd1);
                    }
                }
            }

        }
        private static void clickErrorButton()
        {
            while (true)
            {
                IntPtr hwndStart = Win32Pinvoker.FindWindow(DIALOG_CLASSNAME, "Adobe Illustrator");
                IntPtr hwndStart2 = Win32Pinvoker.FindWindow(DIALOG_CLASSNAME, "Text Import Options");

                if (hwndStart == IntPtr.Zero && hwndStart2 == IntPtr.Zero)
                {
                    Thread.Sleep(3000);
                }
                else
                {
                    IntPtr hwnd2 = Win32Pinvoker.FindWindowEx(hwndStart, IntPtr.Zero, "Button", "OK");
                    IntPtr hwnd3 = Win32Pinvoker.FindWindowEx(hwndStart2, IntPtr.Zero, "Button", "OK");
                    if (hwnd2 != IntPtr.Zero)
                    {
                        Win32Pinvoker.ClosePopupDialog(hwnd2);
                    }
                    if (hwnd3 != IntPtr.Zero)
                    {
                        Win32Pinvoker.ClosePopupDialog(hwnd3);
                    }
                }
            }
        }
    }
}
