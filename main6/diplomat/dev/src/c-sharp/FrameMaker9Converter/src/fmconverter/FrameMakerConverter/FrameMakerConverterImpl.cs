using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using GlobalSight.Common;
using System.IO;
using System.Diagnostics;
using System.Threading;

namespace FrameMakerConverter
{
    class FrameMakerConverterImpl : Converter
    {
        private static int timeoutDefault = 5;

        private Logger m_log = null;

        private const int FILE_EXT_LEN = 10;
        private string m_baseFileName = null;
        private string m_originalFileName = null;
        private string m_newFileName = null;
        private string m_tpsFileName = null;
        private string m_psFileFolder = null;
        private string m_psFileName = null;
        private string m_statusFileName = null;
        private string m_fmExePath = null;
        private int timeoutMinute = timeoutDefault;

        private bool m_isConvertFinished = false;

        public const string COMMAND_FILE_EXT = "*.fm_command";

        public FrameMakerConverterImpl(String fmExePath)
        {
            m_log = Logger.GetLogger();
            m_fmExePath = fmExePath;

            try
            {
                string tm = AppConfig.GetAppConfig("TimeoutMinute");
                if (!Int32.TryParse(tm, out timeoutMinute))
                {
                    timeoutMinute = timeoutDefault;
                }
            }
            catch
            {
                timeoutMinute = timeoutDefault;
            }
        }


        /// <summary>
        /// Close the Adobe Distiller
        /// </summary>
        public static void CloseDistiller()
        {
            Process[] parray = Process.GetProcessesByName("acrodist");
            if (parray != null)
            {
                foreach (Process p in parray)
                {
                    try
                    {
                        p.CloseMainWindow();
                    }
                    catch { }
                }
            }
        }

        private void DetermineConversionValues(string p_fileName)
        {
            // Since this happens in a separate process, it should sleep
            // until the file is completely written out (to prevent the
            // file sharing exception).
            bool successful = false;
            m_isConvertFinished = false;
            for (int i = 0; (!successful && i < 3); i++)
            {
                try
                {
                    Thread.Sleep(1000);
                    _DetermineConversionValues(p_fileName);
                    successful = true;
                }
                catch (Exception e)
                {
                    m_log.Log("Failed to determine conversion values for: " +
                        p_fileName + " - " + e.ToString());
                }
            }
        }

        private void _DetermineConversionValues(string p_fileName)
        {
            FileInfo fi = new FileInfo(p_fileName);
            StreamReader sr = fi.OpenText();
            char[] delimiters = new char[1];
            delimiters[0] = '=';

            // the ConvertTo and ConvertFrom lines can be fm, mif; 
            // the ConvertToline can be pdf too; 
            // for example:
            //ConvertFrom=fm
            //ConvertTo=mif
            string convertFrom = sr.ReadLine().Split(delimiters, 2)[1];
            string convertTo = sr.ReadLine().Split(delimiters, 2)[1];
            setConversionType(convertFrom, convertTo, p_fileName);

            sr.Close();
        }

        private void setConversionType(string p_convertFrom, string p_convertTo, string p_fileName)
        {
            m_baseFileName = p_fileName.Substring(0, p_fileName.Length - FILE_EXT_LEN);
            m_originalFileName = m_baseFileName + p_convertFrom.ToLower();
            m_newFileName = m_baseFileName + p_convertTo.ToLower();
            m_tpsFileName = m_baseFileName + "tps";
            m_psFileFolder = m_baseFileName + "psdir";
            m_psFileName = m_baseFileName + "ps";
        }

        #region Converter Members

        public void Convert(string p_filename, string p_srcLanguage)
        {
            Process p = null;
            try
            {
                DetermineConversionValues(p_filename);

                m_statusFileName = m_baseFileName + "status";


                m_log.Log("Processing file " + m_originalFileName);

                CleanResultFile();
                CleanTempFiles();
                p = OpenDoc();

                if (m_newFileName.EndsWith("pdf"))
                {
                    Thread tpsThread = new Thread(new ThreadStart(CheckTps));
                    tpsThread.Start();
                }

                WaitResultFile();

                StatusFile.WriteSuccessStatus(m_statusFileName,
                    m_originalFileName + " was converted successfully.");

                m_log.Log("Converted successfully to " + m_newFileName);
            }
            catch (Exception e)
            {
                Logger.LogError("FrameMaker Conversion Failed", e);
                StatusFile.WriteErrorStatus(m_statusFileName, e, (int)1);
            }
            finally
            {
                DeleteInputFile(p_filename);

                try
                {
                    if (p != null)
                        p.Kill();
                }
                catch { }

                CloseDistiller();

                CleanTempFiles();
            }
        }

        private void CleanTempFiles()
        {
            try
            {
                string lockFile = m_baseFileName + "fm.lck";
                string pdfLog = m_baseFileName + "log";

                if (File.Exists(lockFile))
                {
                    File.Delete(lockFile);
                }
                if (File.Exists(pdfLog))
                {
                    File.Delete(pdfLog);
                }
                if (File.Exists(m_tpsFileName))
                {
                    File.Delete(m_tpsFileName);
                }
                if (File.Exists(m_psFileName))
                {
                    File.Delete(m_psFileName);
                }
                if (Directory.Exists(m_psFileFolder))
                {
                    Directory.Delete(m_psFileFolder, true);
                }
            }
            catch (Exception e)
            {
                Logger.LogError("Problem clean temp file", e);
            }
        }

        /// <summary>
        /// Deletes the command file
        /// </summary>
        private void DeleteInputFile(string p_fileName)
        {
            try
            {
                lock (new Object())
                {
                    FileInfo fi = new FileInfo(p_fileName);
                    fi.Delete();
                }
            }
            catch (Exception e)
            {
                Logger.LogError("Problem deleting input file", e);
            }
        }

        private void CleanResultFile()
        {
            if (File.Exists(m_newFileName))
            {
                File.Delete(m_newFileName);
            }
        }

        /// <summary>
        /// Fix some PostScrit issue in generated TPS file by FrameMaker
        /// </summary>
        private void CheckTps()
        {
            while (!m_isConvertFinished)
            {
                if (File.Exists(m_tpsFileName))
                {
                    Directory.CreateDirectory(m_psFileFolder);
                    // sleep a little time for writing completed tps file by FrameMaker
                    Thread.Sleep(300);
                    string argments = "\"" + m_tpsFileName + "\" \"" + m_psFileFolder + "\" /Y";
                    ProcessStartInfo psi = createProcessStartInfo("xcopy", argments);
                    Process.Start(psi);
                    Thread.Sleep(300);
                    string psFile = m_psFileFolder + "\\" + (new FileInfo(m_tpsFileName)).Name;

                    // copy muti times to make sure the copied tps file is completed one
                    string argments2 = "\"" + m_tpsFileName + "\" \"" + m_psFileFolder + "\" /D /Y";
                    int count = 0;
                    while (File.Exists(m_tpsFileName))
                    {
                        ProcessStartInfo psi2 = createProcessStartInfo("xcopy", argments2);
                        Process.Start(psi2);
                        Thread.Sleep(300);

                        if (1000 == count)
                        {
                            break;
                        }
                    }

                    // can not use this method as this file is using
                    // File.Copy(m_tpsFileName, m_psFileName);
                    // sleep 5 seconds for copy
                    Thread.Sleep(5000);
                    if (!File.Exists(psFile))
                    {
                        break;
                    }

                    string tps = File.ReadAllText(psFile);
                    string newps = tps;
                    string[] oriString = { " putEncoding", " getexec", " sqrtdef", " andAGMCORE_producing_seps" };
                    string[] newString = { " put Encoding", " get exec", " sqrt def", " and AGMCORE_producing_seps" };

                    newps = replaceIfContain(tps, oriString, newString);

                    File.WriteAllText(m_psFileName, newps);

                    // sleep for first generate pdf
                    Thread.Sleep(10000);
                    if (!File.Exists(m_newFileName))
                    {
                        Process.Start(m_psFileName);
                    }

                    break;
                }
                else
                {
                    Thread.Sleep(50);
                }
            }
        }

        private static long getLastWriteTimeOfFile(string filepath)
        {
            try
            {
                return (File.Exists(filepath)) ? File.GetLastWriteTime(filepath).Ticks : -1;
            }
            catch
            {
                return 0;
            }
        }

        private static ProcessStartInfo createProcessStartInfo(string process, string arguments)
        {
            ProcessStartInfo ps = new ProcessStartInfo(process, arguments);
            ps.CreateNoWindow = true;
            ps.UseShellExecute = false;

            return ps;
        }

        private static string replaceIfContain(string src, string[] oriString, string[] newString)
        {
            string temp = src;

            for (int i = 0; i < oriString.Length; i++)
            {
                string ori = oriString[i];
                string news = newString[i];

                if (temp.Contains(ori))
                {
                    temp = temp.Replace(ori, news);
                }
            }


            return temp;
        }

        private void WaitResultFile()
        {
            for (int i = 0; i < timeoutMinute * 2; i++)
            {
                Thread.Sleep(500 * 60);

                if (File.Exists(m_newFileName))
                {
                    break;
                }
            }

            m_isConvertFinished = true;

            if (!File.Exists(m_newFileName))
            {
                throw new Exception("Timeout when waiting file: " + m_newFileName);
            }
        }

        /// <summary>
        /// Open FM document with FrameMaker, /2 argument is to open new FrameMaker exe
        /// </summary>
        /// <returns></returns>
        private Process OpenDoc()
        {
            //Process.Start(m_fmExePath);
            //Thread.Sleep(1000 * 60);
            //Process p = Process.Start(m_fmExePath, "\"" + m_originalFileName + "\"");
            Process p = Process.Start(m_fmExePath, "/2 \"" + m_originalFileName + "\"");
            return p;
        }

        public string GetFileExtensionToWatch()
        {
            return COMMAND_FILE_EXT;
        }

        #endregion
    }
}
