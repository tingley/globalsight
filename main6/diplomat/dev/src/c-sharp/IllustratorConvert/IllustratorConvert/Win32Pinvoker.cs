using System;
using System.Collections.Generic;
using System.Text;
using System.Runtime.InteropServices;

namespace IllustratorConvert
{
    class Win32Pinvoker
    {
        private const int WM_KEYDOWN = 0x0100;
        private const int VK_RETURN = 0x0D;
        private const int VK_CLICK = 245;

        // The FindWindow function retrieves a handle
        // to the top-level window whose class name
        // and window name match the specified strings.
        // This function does not search child windows.
        // This function does not perform a case-sensitive search.
        [DllImport("user32.dll")]
        public static extern IntPtr FindWindow(string className, string windowsName);

        // The FindWindowEx function retrieves
        // a handle to a window whose class name 
        // and window name match the specified strings.
        // The function searches child windows, beginning
        // with the one following the specified child window.
        // This function does not perform a case-sensitive search.
        [DllImport("user32.dll")]
        public static extern IntPtr FindWindowEx(IntPtr parentHwnd, IntPtr childAfter, string className, string windowTitle);

        [DllImport("user32.dll")]
        public static extern int SendMessage(IntPtr hWnd, uint Msg, int wParam, int lParam);

        // The SendMessage function sends the specified message 
        // to a window or windows. It calls the window procedure 
        // for the specified window and does not return until 
        // the window procedure has processed the message.
        public static void ClosePopupDialog(IntPtr p_hWnd)
        {
            SendMessage(p_hWnd, VK_CLICK, 0, 0);
        }
    }
   }
