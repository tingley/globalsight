//                             -*- Mode: Csharp -*- 
// 
// Copyright (c) 2005 GlobalSight Corporation. All rights reserved.
// 
// THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF 
// GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
// IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
// OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
// AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.
// 
// THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
// SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
// UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
// BY LAW.
// 

using System;
using System.Collections.Generic;
using System.Text;
using System.Runtime.InteropServices;

namespace GlobalSight.Office2003Converters
{
    class Win32Pinvoker
    {
        private const int WM_CLOSE = 16;
        private const int BM_CLICK = 0x00F5; //Button Click

        /// <summary>
        /// The FindWindow function retrieves a handle
        /// to the top-level window whose class name
        /// and window name match the specified strings.
        /// This function does not search child windows.
        /// This function does not perform a case-sensitive search.
        /// </summary>
        [DllImport("user32.dll")]
        public static extern IntPtr FindWindow(string className, string windowsName);

        /// <summary>
        /// The FindWindowEx function retrieves
        /// a handle to a window whose class name 
        /// and window name match the specified strings.
        /// The function searches child windows, beginning
        /// with the one following the specified child window.
        /// This function does not perform a case-sensitive search.
        /// </summary>
        [DllImport("user32.dll")]
        public static extern IntPtr FindWindowEx(IntPtr parentHwnd, IntPtr childAfter, string className, string windowTitle);

        [DllImport("user32.dll")]
        public static extern bool IsChild(IntPtr hWndParent, IntPtr hwnd);

        [DllImport("user32.dll")]
        public static extern IntPtr GetParent(IntPtr hWnd);

        /// <summary>
        /// The SendMessage function sends the specified message 
        /// to a window or windows. It calls the window procedure 
        /// for the specified window and does not return until 
        /// the window procedure has processed the message.
        /// </summary>
        [DllImport("user32.dll")]
        public static extern int SendMessage(IntPtr hWnd, uint Msg, int wParam, int lParam);

        public static void ClosePopupDialog(IntPtr p_hWnd)
        {
            SendMessage(p_hWnd, WM_CLOSE, 0, 0);
        }

        public static void ClickButtonAndClose(IntPtr p_hWnd)
        {
            SendMessage(p_hWnd, BM_CLICK, 0, 0);
        }
    }
}
