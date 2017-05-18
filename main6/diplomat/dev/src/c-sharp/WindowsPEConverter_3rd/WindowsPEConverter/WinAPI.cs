using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Runtime.InteropServices;

namespace GlobalSight.WinPEConverter
{
    public class WinAPI
    {
        //[DllImport("kernel32.dll", EntryPoint = "")]
        //public static extern 

        #region kernel32.dll

        [DllImport("kernel32.dll", SetLastError = true)]
        public static extern IntPtr BeginUpdateResource(string pFileName,
            [MarshalAs(UnmanagedType.Bool)]bool bDeleteExistingResources);

        [DllImport("kernel32.dll", SetLastError = true)]
        public static extern bool UpdateResource(IntPtr hUpdate, string lpType, string lpName, 
            ushort wLanguage, IntPtr lpData, uint cbData);

        [DllImport("kernel32.dll", SetLastError = true)]
        public static extern bool EndUpdateResource(IntPtr hUpdate, bool fDiscard);

        [DllImport("kernel32", SetLastError = true)]
        public static extern IntPtr LoadLibrary(string lpFileName);

        [DllImport("Kernel32.dll", EntryPoint = "FindResourceW", SetLastError = true, CharSet = CharSet.Unicode)]
        public static extern IntPtr FindResource(IntPtr hModule, string pName, string pType);

        [DllImport("Kernel32.dll", EntryPoint = "SizeofResource", SetLastError = true)]
        public static extern uint SizeofResource(IntPtr hModule, IntPtr hResource);

        [DllImport("Kernel32.dll", EntryPoint = "LoadResource", SetLastError = true)]
        public static extern IntPtr LoadResource(IntPtr hModule, IntPtr hResource);

        #endregion kernel32.dll


        #region version.dll

        [DllImport("version.dll", SetLastError = true)]
        public static extern bool GetFileVersionInfo(string sFileName, int handle, int size, byte[] infoBuffer);

        [DllImport("version.dll", SetLastError = true)]
        public static extern int GetFileVersionInfoSize(string sFileName, out int handle);

        // The third parameter - "out string pValue" - is automatically
        // marshaled from ANSI to Unicode:
        [DllImport("version.dll", SetLastError = true)]
        unsafe public static extern bool VerQueryValue(byte[] pBlock, string pSubBlock, out string pValue, out uint len);

        // This VerQueryValue overload is marked with 'unsafe' because 
        // it uses a short*:
        [DllImport("version.dll", SetLastError = true)]
        unsafe public static extern bool VerQueryValue(byte[] pBlock, string pSubBlock, out short* pValue, out uint len);

        #endregion version.dll
    }
}
