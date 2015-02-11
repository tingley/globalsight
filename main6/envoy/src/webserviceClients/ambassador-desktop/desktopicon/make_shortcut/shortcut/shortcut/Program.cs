using System;
using IWshRuntimeLibrary;

namespace shortcut
{
    static class Program
    {
        private const string DESKTOPICON_NAME = "GlobalSight Desktop Icon";
        private const string DESKTOPICON_LINK_NAME = DESKTOPICON_NAME + ".lnk";
        private const string DESKTOPICON_SCRIPT = "DesktopIcon.bat";
        private const string DESKTOPICON_IMAGE = "GlobalSight_Icon.ico";
        private const string FILE_SEPARATOR = "\\";
        /// <summary>
        /// The main entry point for the application.
        /// </summary>
        [STAThread]
        static void Main()
        {
            WshShell shell = new WshShell();
            IWshShortcut shortcut = (IWshShortcut)shell.CreateShortcut(
              Environment.GetFolderPath(Environment.SpecialFolder.DesktopDirectory) + FILE_SEPARATOR + DESKTOPICON_LINK_NAME
            );
            shortcut.WorkingDirectory = System.Environment.CurrentDirectory;
            shortcut.TargetPath = shortcut.WorkingDirectory + FILE_SEPARATOR + DESKTOPICON_SCRIPT;
            //shortcut.TargetPath = System.Reflection.Assembly.GetExecutingAssembly().Location;
            shortcut.WindowStyle = 1;
            shortcut.Description = DESKTOPICON_NAME;
            shortcut.IconLocation = shortcut.WorkingDirectory + FILE_SEPARATOR + DESKTOPICON_IMAGE;
            shortcut.Save();
        }
    }
}