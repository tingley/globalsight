using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Text;

namespace GlobalSight.WinPEConverter
{
    /// <summary>
    /// read and write 3rd exe to locale folder if not found
    /// </summary>
    class CommandUtil
    {
        private static Object locker = new object();
        private string exepath = null;
        private string logpath = null;

        public CommandUtil()
        {
            Assembly assembly = Assembly.GetExecutingAssembly();
            FileInfo assFile = new FileInfo(assembly.Location);
            string parent = assFile.DirectoryName;
            exepath = "\"" + parent + "\\resh.exe\"";
            logpath = parent + "\\resh.log";
            string filepath = parent + "\\resh.exe";

            if (!File.Exists(filepath))
            {
                lock (locker)
                {
                    if (!File.Exists(filepath))
                    {
                        
                        using (Stream exeStream = assembly.GetManifestResourceStream("GlobalSight.WinPEConverter.resh.exe"))
                        {
                            using (Stream s = File.Create(filepath))
                            {
                                exeStream.CopyTo(s);
                            }
                        }
                    }
                }
            }
        }

        public string[] Extract(string filename)
        {
            string rcMenu = filename + ".menu.rc";
            string argMenu = "-extract \"" + filename + "\",\"" + rcMenu + "\",menu,,";
            string outputMenu = RunExternalExe(exepath, argMenu);

            string rcString = filename + ".string.rc";
            string argString = "-extract \"" + filename + "\",\"" + rcString + "\",StringTable,,";
            string outputString = RunExternalExe(exepath, argString);

            string rcdialog = filename + ".dialog.rc";
            string argdialog = "-extract \"" + filename + "\",\"" + rcdialog + "\",dialog,,";
            string outputdialog = RunExternalExe(exepath, argdialog);

            string rcversion = filename + ".version.rc";
            string argversion = "-extract \"" + filename + "\",\"" + rcversion + "\",VERSIONINFO,,";
            string outputversion = RunExternalExe(exepath, argversion);

            return new string[]{rcMenu, rcString, rcdialog, rcversion};
        }

        public void Compile(string rcfilename, string resfilename)
        {
            string argCompile = "-compile \"" + rcfilename + "\",\"" + resfilename + "\"";
            RunExternalExe(exepath, argCompile);
        }

        public void Modify(string filename, String newfilename, string resfilename)
        {
            string argModify = "-modify \"" + filename + "\",\"" + newfilename + "\",\"" + resfilename + "\",,,";
            RunExternalExe(exepath, argModify);
        }

        public string GetLog()
        {
            string log = File.ReadAllText(logpath, Encoding.Unicode);

            if (!log.Contains("CurrentDir") || !log.Contains("resh.exe"))
            {
                log = File.ReadAllText(logpath, Encoding.Default);
            }

            return log;
        }

        private string RunExternalExe(string filename, string arguments = null)
        {
            var process = new Process();

            process.StartInfo.FileName = filename;
            if (!string.IsNullOrEmpty(arguments))
            {
                process.StartInfo.Arguments = arguments;
            }

            process.StartInfo.CreateNoWindow = true;
            process.StartInfo.WindowStyle = ProcessWindowStyle.Hidden;
            process.StartInfo.UseShellExecute = false;

            process.StartInfo.RedirectStandardError = true;
            process.StartInfo.RedirectStandardOutput = true;
            var stdOutput = new StringBuilder();
            process.OutputDataReceived += (sender, args) => stdOutput.Append(args.Data);

            string stdError = null;
            try
            {
                process.Start();
                process.BeginOutputReadLine();
                stdError = process.StandardError.ReadToEnd();
                process.WaitForExit();
            }
            catch (Exception e)
            {
                throw new Exception("OS error while executing " + Format(filename, arguments) + ": " + e.Message, e);
            }

            if (process.ExitCode == 0)
            {
                return stdOutput.ToString();
            }
            else
            {
                var message = new StringBuilder();

                if (!string.IsNullOrEmpty(stdError))
                {
                    message.AppendLine(stdError);
                }

                if (stdOutput.Length != 0)
                {
                    message.AppendLine("Std output:");
                    message.AppendLine(stdOutput.ToString());
                }

                throw new Exception(Format(filename, arguments) + " finished with exit code = " + process.ExitCode + ": " + message);
            }
        }

        private string Format(string filename, string arguments)
        {
            return "'" + filename +
                ((string.IsNullOrEmpty(arguments)) ? string.Empty : " " + arguments) +
                "'";
        }
    }
}
