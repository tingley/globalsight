using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;
using GlobalSight.Common;
using System.Collections;

namespace GlobalSight.Office2007Converters
{
    public class GfxdataHandler
    {
        private bool LogDebugMSG = false;
        private Logger m_log = null;
        private OfficeFormat m_format = OfficeFormat.Pptx;
        private string m_htmFile = null;
        private string phPrefix = "gs_data_ph_";
        private string gfxStart = "gfxdata=\"";
        private string gfxEnd = "\"";

        /// <summary>
        /// Currently, just handle Pptx
        /// </summary>
        /// <param name="htmFile"></param>
        /// <param name="format"></param>
        public GfxdataHandler(string htmFile, OfficeFormat format)
        {
            if (htmFile == null)
            {
                throw new ArgumentNullException();
            }

            if (format != OfficeFormat.Pptx)
            {
                throw new NotSupportedException("Format " + format + " is not supportted.");
            }

            m_htmFile = htmFile;
            m_format = format;

            m_log = Logger.GetLogger();

            try
            {
                LogDebugMSG = Boolean.Parse(AppConfig.GetAppConfig("LogDebugMSG"));
            }
            catch
            {
                LogDebugMSG = false;
            }
        }

        public void HandleConvert()
        {
            if (m_format == OfficeFormat.Pptx)
            {
                HandlePptConvert();
            }
        }

        public void HandleConvertBack()
        {
            if (m_format == OfficeFormat.Pptx)
            {
                HandlePptConvertBack();
            }
        }

        private void HandlePptConvert()
        {
            List<FileInfo> slides = GetPptSlideHtm();

            foreach (FileInfo slideHtm in slides)
            {
                HandleHtmConvert(slideHtm);
            }
        }

        private void HandlePptConvertBack()
        {
            List<FileInfo> slides = GetPptSlideHtm();

            foreach (FileInfo slideHtm in slides)
            {
                HandleHtmConvertBack(slideHtm);
            }
        }

        private void HandleHtmConvert(FileInfo htmFile)
        {
            string fullname = htmFile.FullName;
            string filecontent = File.ReadAllText(fullname, Encoding.UTF8);

            Debug("HTML File : " + fullname);
            Debug("Size : " + filecontent.Length);
            Debug("Contains " + gfxStart + " : " + filecontent.Contains(gfxStart));

            if (!filecontent.Contains(gfxStart))
            {
                return;
            }

            int index_start = filecontent.IndexOf(gfxStart);
            int index_start_0 = index_start + gfxStart.Length;
            int index_end = filecontent.IndexOf(gfxEnd, index_start_0);

            int count = 1;

            while (index_start != -1 && index_end != -1)
            {
                Debug("index_start_0 : " + index_start_0 + " index_end : " + index_end);
                string gfxdata = filecontent.Substring(index_start_0, index_end - index_start_0);
                string prefix = filecontent.Substring(0, index_start_0);
                string subfix = filecontent.Substring(index_end);

                string phname = phPrefix + count;
                string gfxfile = fullname + "." + phname;

                filecontent = prefix + phname + subfix;
                File.WriteAllText(gfxfile, gfxdata);

                count++;
                index_start = filecontent.IndexOf(gfxStart, index_start_0);
                index_start_0 = index_start + gfxStart.Length;
                index_end = filecontent.IndexOf(gfxEnd, index_start_0);
            }

            File.WriteAllText(fullname, filecontent);
        }

        private void HandleHtmConvertBack(FileInfo htmFile)
        {
            string fullname = htmFile.FullName;
            string filecontent = File.ReadAllText(fullname, Encoding.UTF8);

            if (!filecontent.Contains(phPrefix))
            {
                return;
            }

            int count = 1;
            string phname = phPrefix + count;
            string gfxfile = fullname + "." + phname;

            while (File.Exists(gfxfile))
            {
                string gfxdata = File.ReadAllText(gfxfile);
                filecontent = filecontent.Replace(phname, gfxdata);

                count++;
                phname = phPrefix + count;
                gfxfile = fullname + "." + phname;
            }

            File.WriteAllText(fullname, filecontent);
        }

        private List<FileInfo> GetPptSlideHtm()
        {
            FileInfo htmFi = new FileInfo(m_htmFile);
            DirectoryInfo slideFolder = GetHtmFileDir(htmFi);

            FileInfo[] allfiles = slideFolder.GetFiles();

            List<FileInfo> slides = new List<FileInfo>();

            if (allfiles != null)
            {
                for (int i = 0; i < allfiles.Length; i++)
                {
                    FileInfo f = allfiles[i];
                    String fname = f.Name;

                    Debug("Find file: " + fname);

                    if (fname.StartsWith("slide")
                        && (fname.EndsWith(".htm") || fname.EndsWith(".html")))
                    {
                        Debug("Add slide: " + fname);
                        slides.Add(f);
                    }
                }
            }

            return slides;
        }

        private DirectoryInfo GetHtmFileDir(FileInfo htmFi)
        {
            string fullname = htmFi.FullName;
            string ext = htmFi.Extension;

            string prefix = fullname.Substring(0, fullname.Length - ext.Length);

            string dir1 = prefix + ".files";
            string dir2 = prefix + "_files";

            return Directory.Exists(dir1) ? new DirectoryInfo(dir1) : new DirectoryInfo(dir2);
        }

        private void Debug(String msg)
        {
            if (LogDebugMSG)
            {
                m_log.Log("[Debug from GfxdataHandler] " + msg);
            }
        }
    }

    public enum OfficeFormat
    {
        Pptx,
        Xlsx,
        Docx
    }
}
