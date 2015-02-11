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
                IntPtr hwndStart1 = Win32Pinvoker.FindWindow(DIALOG_CLASSNAME, "Adobe Illustrator");
                IntPtr hwndStart2 = Win32Pinvoker.FindWindow(DIALOG_CLASSNAME, "Text Import Options");
                IntPtr hwndStart3 = Win32Pinvoker.FindWindow(DIALOG_CLASSNAME, "");
                IntPtr hwndStart4 = Win32Pinvoker.FindWindow(DIALOG_CLASSNAME, "Font Problems");

                if (hwndStart1 == IntPtr.Zero && hwndStart4 == IntPtr.Zero && hwndStart2 == IntPtr.Zero)
                {
                    Thread.Sleep(500);
                }
                if (hwndStart3 == IntPtr.Zero)
                {
                    Thread.Sleep(100);
                }
             
                else
                {
                    IntPtr hwnd2 = Win32Pinvoker.FindWindowEx(hwndStart1, IntPtr.Zero, "Button", "OK");
                    IntPtr hwnd3 = Win32Pinvoker.FindWindowEx(hwndStart2, IntPtr.Zero, "Button", "OK");
                    IntPtr hwnd4 = Win32Pinvoker.FindWindowEx(hwndStart3, IntPtr.Zero, "Button", "&Ignore");
                    IntPtr hwnd5 = Win32Pinvoker.FindWindowEx(hwndStart4, IntPtr.Zero, "Button", "Open");
          
                    if (hwnd2 != IntPtr.Zero)
                    {
                        Win32Pinvoker.ClosePopupDialog(hwnd2);
                    }
                    if (hwnd3 != IntPtr.Zero)
                    {
                        Win32Pinvoker.ClosePopupDialog(hwnd3);
                    }
                    if (hwnd4 != IntPtr.Zero)
                    {
                        Win32Pinvoker.ClosePopupDialog(hwnd4);
                    }
                    if (hwnd5 != IntPtr.Zero)
                    {
                        Win32Pinvoker.ClosePopupDialog(hwnd5);
                    }
                                               
                }
            }
        }
    }
}
