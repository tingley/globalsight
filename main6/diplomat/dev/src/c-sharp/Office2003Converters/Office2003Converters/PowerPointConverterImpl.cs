using System;
using System.IO;
using Microsoft;
using Office = Microsoft.Office.Core;
using PowerPoint = Microsoft.Office.Interop.PowerPoint;
using GlobalSight.Common;
using System.Text;
using System.Collections.Generic;

namespace GlobalSight.Office2003Converters
{
    /// <summary>
    /// PowerPoint2003ConverterImpl is responsible for converting PowerPoint
    /// documents to and from HTML.
    /// </summary>
    public class PowerPointConverterImpl : GlobalSight.Common.Converter
    {
        private Logger m_log = null;
        private PowerPoint.Application m_pptApp = null;
        private PowerPoint._Presentation m_presentation = null;

        private string m_originalFileName = null;
        private string m_newFileName = null;
        private PowerPoint.PpSaveAsFileType m_newFormatType =
            PowerPoint.PpSaveAsFileType.ppSaveAsHTML;
        private string m_fileExtensionSearchPattern = null;
        private ConversionType m_conversionType = ConversionType.IMPORT;
        private string m_statusFileName = null;

        public const string EXPORT_FILE_EXT = "*.ex_command";
        public const string IMPORT_FILE_EXT = "*.im_command";
        //10 chars after . for both import and export
        private const int FILE_EXT_LEN = 10;
        public enum ConversionType { IMPORT, EXPORT };

        //PowerPoint file formats
        public const string HTML = "html";
        public const string PPT = "ppt";

        private bool m_handleIdInMasterXML = true;

        private const Office.MsoTriState MS_FALSE = Office.MsoTriState.msoFalse;
        private const Office.MsoTriState MS_TRUE = Office.MsoTriState.msoTrue;

        /// <summary>
        /// Creates a PowerPoint2003ConverterImpl to be used for import or export.
        /// </summary>
        /// <param name="p_conversionType"> the type of conversion (import or export)</param>
        public PowerPointConverterImpl(ConversionType p_conversionType)
        {
            m_log = PowerPointConverterUtil.GetLogger();

            m_conversionType = p_conversionType;

            if (m_conversionType == ConversionType.EXPORT)
            {
                m_fileExtensionSearchPattern = EXPORT_FILE_EXT;
            }
            else
            {
                m_fileExtensionSearchPattern = IMPORT_FILE_EXT;
            }
        }

        /// <summary>
        /// Converts the file from one format to another.
        /// </summary>
        /// <param name="p_filename">The command file (.im_command, .ex_command)</param>
        /// <param name="p_language">the language of the directory the file was written in</param>
        public void Convert(string p_fileName, string p_language)
        {
            try
            {
                ResetState();

                m_statusFileName = p_fileName.Substring(
                    0, p_fileName.Length - FILE_EXT_LEN) + "status";
                DetermineConversionValues(p_fileName);

                m_log.Log("Processing file " + m_originalFileName);

                string htmFile = m_conversionType == ConversionType.IMPORT ? m_newFileName : m_originalFileName;
                OfficeFileHandler handler = new OfficeFileHandler(htmFile, OfficeFormat.Pptx);

                if (m_handleIdInMasterXML && m_conversionType == ConversionType.EXPORT)
                {
                    handler.HandleMasterXML();
                    handler.HandleConvertBack();
                }

                CreatePowerPointAppClass();
                OpenDocument();
                SaveDocument();

                if (m_handleIdInMasterXML && m_conversionType == ConversionType.IMPORT)
                {
                    handler.HandleMasterXML();
                    handler.HandleConvert();
                }

                StatusFile.WriteSuccessStatus(m_statusFileName,
                    m_originalFileName + " was converted successfully.");

                m_log.Log("Converted successfully to " + m_newFileName);
            }
            catch (Exception e)
            {
                Logger.LogError(m_log, "PowerPoint 2003 Conversion Failed", e);
                StatusFile.WriteErrorStatus(m_statusFileName, e, (int)1);
            }
            finally
            {
                DeleteInputFile(p_fileName);
            }
        }

        /// <summary>
        /// See Converter interface for details.
        /// This either return ".im_command" or ".ex_command" depending on whether
        /// the converter is used for import or export.
        /// </summary>
        /// <returns></returns>
        public string GetFileExtensionToWatch()
        {
            return m_fileExtensionSearchPattern;
        }

        /// <summary>
        /// Quits the PowerPoint Application.
        /// </summary>
        public void QuitPowerPoint()
        {
            if (m_pptApp == null)
            {
                return;
            }

            // closing the presentation would not close the application, so we
            // close it explicitly if there are no more presentations.  Since
            // only one instance of PowerPoint can be running at a time, we need
            // to make sure that there are no more presentations before quiting the
            // application
            if (m_pptApp.Presentations != null && m_pptApp.Presentations.Count == 0)
            {
                try
                {
                    m_pptApp.Quit();
                }
                catch (Exception e)
                {
                    Logger.LogError(m_log, "Failed to quit PowerPoint", e);
                }
            }

            m_pptApp = null;
        }

        /// <summary>
        /// Saves the document out with the appropriate new filename and format.
        /// Also does any special handling such as accepting/rejecting changes
        /// before saving.
        /// </summary>
        private void SaveDocument()
        {
            m_presentation.SaveAs(m_newFileName, m_newFormatType, MS_FALSE);

            try
            {
                m_presentation.Close();
            }
            catch (Exception e)
            {
                Logger.LogError(m_log, "Failed to close PowerPoint presentation", e);
            }

            m_presentation = null;
        }

        /// <summary>
        /// Creates the PowerPoint Application Class and sets the default web options
        /// </summary>
        private void CreatePowerPointAppClass()
        {
            if (m_pptApp == null)
            {
                m_pptApp = new PowerPoint.Application();
            }

            m_pptApp.DefaultWebOptions.Encoding = Office.MsoEncoding.msoEncodingUTF8;
            m_pptApp.DefaultWebOptions.AlwaysSaveInDefaultEncoding = MS_TRUE;
        }

        /// <summary>
        /// Opens the original word file and initializes the Word.Document object.
        /// The original file may be HTML, DOC, or RTF
        /// </summary>
        private void OpenDocument()
        {
            // Open the presentation (ppt file) based on the given file name
            // Note that the last parameter determines whether the
            // presentation should be opened within a visible window,
            // which in our case in "false".  Since in PowerPoint no
            // hiding of app is allowed ( i.e."pptApp.Visible = falseFlag;"
            //  won't work), we should not call pptApp.Activate();
            m_presentation = m_pptApp.Presentations.Open(m_originalFileName,
                MS_FALSE, MS_FALSE, MS_FALSE);
        }


        /// <summary>
        /// Sets all internal state back to null
        /// </summary>
        private void ResetState()
        {
            m_presentation = null;
            m_originalFileName = null;
            m_newFileName = null;
            m_statusFileName = null;
        }

        /// <summary>
        /// Reads the command file to figure out specific values for the 
        /// conversion such as the new filename, the format type, whether 
        // to track changes, etc.
        /// </summary>
        /// <param name="p_fileName">the command file (.im_command) or (.ex_command)</param>
        private void DetermineConversionValues(string p_fileName)
        {
            FileInfo fi = new FileInfo(p_fileName);
            StreamReader sr = fi.OpenText();
            char[] delimiters = new char[1];
            delimiters[0] = '=';

            // The ConvertTo and ConvertFrom lines can be html, rtf, or doc; 
            // for example:
            //ConvertFrom=doc
            //ConvertTo=html
            string convertFrom = sr.ReadLine().Split(delimiters, 2)[1];
            string convertTo = sr.ReadLine().Split(delimiters, 2)[1];
            setConversionType(convertFrom, convertTo, p_fileName);
            sr.Close();
        }

        /// <summary>
        /// Sets the conversion type (html,doc,rtf)
        /// </summary>
        /// <param name="p_convertFrom">ConvertFrom value from the command file</param>
        /// <param name="p_convertTo">ConverTo value from the command file</param>
        /// <param name="p_fileName">the command file</param>
        private void setConversionType(string p_convertFrom, string p_convertTo,
            string p_fileName)
        {
            string baseFileName = p_fileName.Substring(
                0, p_fileName.Length - FILE_EXT_LEN);
            m_originalFileName = baseFileName + p_convertFrom.ToLower();

            if (p_convertTo.Equals(HTML))
            {
                m_newFormatType = PowerPoint.PpSaveAsFileType.ppSaveAsHTML;
                m_newFileName = baseFileName + HTML;
            }
            else if (p_convertTo.Equals(PPT))
            {
                m_newFormatType = PowerPoint.PpSaveAsFileType.ppSaveAsPresentation;
                m_newFileName = baseFileName + PPT;
            }
        }

        /// <summary>
        /// Deletes the command file
        /// </summary>
        private void DeleteInputFile(string p_fileName)
        {
            try
            {
                FileInfo fi = new FileInfo(p_fileName);
                fi.Delete();
            }
            catch (Exception e)
            {
                Logger.LogError(m_log, "Problem deleting input file", e);
            }
        }
    }

    class OfficeFileHandler
    {
        private string m_htmFile = null;
        private OfficeFormat m_format = OfficeFormat.Pptx;

        private string shapRectStart = "<p:shaperange id=\"Rectangle_";
        private string shapRectEnd = "</p:shaperange>";

        private string idStart = "id=\"";
        private string spidStart = "o:spid=\"";
        private string attrEnd = "\"";

        private string vshapestart = "<v:shape ";
        private string vshapeend = "</v:shape>";

        // ole object
        private string ole_start = "<o:OLEObject";
        private string ole_end = "</o:OLEObject>";
        private string ole_shapeid_start = "ShapeID=\"";
        private string ole_shapeid_end = "\"";

        public OfficeFileHandler(string htmFile, OfficeFormat format)
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
        }

        public void HandleMasterXML()
        {
            // 0 check
            if (!ModifyMasterXML())
            {
                return;
            }

            // 1 get exclude id
            //List<string> excluedIds = new List<string>();
            //List<string> includeIds = new List<string>();
            //SetSpecialIdOfMasterXML(includeIds, excluedIds);

            // 1 get master xml
            List<FileInfo> masterXmls = GetMasterXmls();

            // 2 handle master xml one by one
            foreach (FileInfo masterXml in masterXmls)
            {
                HandleOneMasterXML(masterXml);
            }
        }

        private void SetSpecialIdOfMasterXML(List<string> includeId, List<string> excludeId)
        {
            List<FileInfo> slides = GetPptSlideHtm();

            foreach (FileInfo slideHtm in slides)
            {
                string filecontent = File.ReadAllText(slideHtm.FullName, Encoding.UTF8);

                if (!filecontent.Contains(vshapestart))
                {
                    continue;
                }

                int index_start = filecontent.IndexOf(vshapestart);
                int index_end = filecontent.IndexOf(vshapeend, index_start + vshapestart.Length);

                while (index_start != -1 && index_end != -1)
                {
                    string vshapedata = filecontent.Substring(index_start, index_end - index_start);

                    if (vshapedata.Contains("id=\"Rectangle_") && vshapedata.Contains("type=\"#Rectangle_"))
                    {
                        string id = GetStringBetween(vshapedata, 0, "type=\"#", attrEnd);
                        if (!excludeId.Contains(id))
                        {
                            excludeId.Add(id);
                        }
                    }

                    if (vshapedata.Contains("id=\"Rectangle_") && vshapedata.Contains("type=\"#_x"))
                    {
                        string id = GetStringBetween(vshapedata, 0, "type=\"#", attrEnd);
                        if (!includeId.Contains(id))
                        {
                            includeId.Add(id);
                        }
                    }

                    index_start = filecontent.IndexOf(vshapestart, index_end);
                    index_end = filecontent.IndexOf(vshapeend, index_start + vshapestart.Length);
                }
            }
        }

        public void HandleConvert()
        {
            if (m_format == OfficeFormat.Pptx)
            {
                //HandlePptConvert();
            }
        }

        public void HandleConvertBack()
        {
            if (m_format == OfficeFormat.Pptx)
            {
                //HandlePptConvertBack();
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
            // do nothing
        }

        private void HandleHtmConvertBack(FileInfo htmFile)
        {
            string fullname = htmFile.FullName;
            string filecontent = File.ReadAllText(fullname, Encoding.UTF8);
            int oriLength = filecontent.Length;
            int nowLength = oriLength;

            if (filecontent.Contains(ole_end))
            {
                filecontent = HandleOLEObject(filecontent);
                nowLength = filecontent.Length;
            }

            if (nowLength != oriLength)
            {
                File.WriteAllText(fullname, filecontent, Encoding.UTF8);
            }

        }

        private string HandleOLEObject(string filecontent)
        {
            if (!filecontent.Contains(ole_end))
            {
                return filecontent;
            }

            int index_from = 0;
            int index_ole_start = filecontent.IndexOf(ole_start, index_from);
            int index_ole_end = filecontent.IndexOf(ole_end, index_ole_start);

            while (index_ole_start > 0 && index_ole_end > 0)
            {
                string ole_data = filecontent.Substring(index_ole_start, index_ole_end - index_ole_start);
                string shapeId = GetStringBetween(ole_data, 0, ole_shapeid_start, ole_shapeid_end);

                string shapeIdAtt = "id=\"" + shapeId + "\"";
                int shapeIdCount = CountString(filecontent, shapeIdAtt);

                if (shapeIdCount == 0)
                {
                    int length = ole_data.Length + ole_end.Length;
                    filecontent = filecontent.Remove(index_ole_start, length);
                }
                else
                {
                    index_from = index_ole_end;
                }

                index_ole_start = filecontent.IndexOf(ole_start, index_from);

                if (index_ole_start != -1)
                {
                    index_ole_end = filecontent.IndexOf(ole_end, index_ole_start);
                }
            }

            return filecontent;
        }

        private bool ModifyMasterXML()
        {
            List<FileInfo> slides = GetPptSlideHtm();

            foreach (FileInfo slideHtm in slides)
            {
                string filecontent = File.ReadAllText(slideHtm.FullName, Encoding.UTF8);

                if (filecontent.Contains("type=\"#_") && IsShapeType(filecontent))
                {
                    return true;
                }

                string htmlfullname = slideHtm.FullName;
                string slideprefix = htmlfullname.Substring(0, htmlfullname.LastIndexOf("."));
                FileInfo slideXml = new FileInfo(slideprefix + ".xml");
                if (!slideXml.Exists)
                {
                    continue;
                }

                filecontent = File.ReadAllText(slideXml.FullName, Encoding.UTF8);

                if (filecontent.Contains("type=\"#_") && IsShapeType(filecontent))
                {
                    return true;
                }
            }

            return false;
        }

        private bool IsShapeType(string filecontent)
        {
            if (!filecontent.Contains(vshapestart))
            {
                return false;
            }

            int index_start = filecontent.IndexOf(vshapestart);
            int index_end = filecontent.IndexOf(vshapeend, index_start + vshapestart.Length);

            while (index_start != -1 && index_end != -1)
            {
                string vshapedata = filecontent.Substring(index_start, index_end - index_start);

                if (vshapedata.Contains("type=\"#_x"))
                {
                    return true;
                }

                index_start = filecontent.IndexOf(vshapestart, index_end);
                index_end = filecontent.IndexOf(vshapeend, index_start + vshapestart.Length);
            }

            return false;
        }

        private void HandleOneMasterXML(FileInfo masterXml)
        {
            string fullname = masterXml.FullName;
            string filecontent = File.ReadAllText(fullname, Encoding.UTF8);

            if (!filecontent.Contains(shapRectStart))
            {
                return;
            }

            int index_start = filecontent.IndexOf(shapRectStart);
            int index_end = filecontent.IndexOf(shapRectEnd, index_start);

            while (index_start != -1 && index_end != -1)
            {
                string shapdata = filecontent.Substring(index_start, index_end - index_start);
                string prefix = filecontent.Substring(0, index_start);
                string subfix = filecontent.Substring(index_end);

                string oldId = GetStringBetween(shapdata, 0, idStart, attrEnd);
                string newId = GetStringBetween(shapdata, 0, spidStart, attrEnd);
                string newShapId = "<v:shapetype id=\"" + newId + "\"";
                if (oldId != null && newId != null && !filecontent.Contains(newShapId))
                {
                    filecontent = prefix + shapdata + "</p:shaperange>\r\n ";
                    string newshapdata = shapdata.Replace(oldId, newId);
                    filecontent = filecontent + newshapdata + subfix;
                    index_end = index_end + newshapdata.Length;
                }

                index_start = filecontent.IndexOf(shapRectStart, index_end);
                index_end = index_start == -1 ? -1 : filecontent.IndexOf(shapRectEnd, index_start);
            }

            File.WriteAllText(fullname, filecontent);
        }

        private string GetStringBetween(string text, int startIndex, string start, string end)
        {
            int index_start = text.IndexOf(start, startIndex);
            int index_start_0 = index_start + start.Length;
            int index_end = text.IndexOf(end, index_start_0);

            if (index_start != -1 && index_end != -1)
            {
                string data = text.Substring(index_start_0, index_end - index_start_0);
                return data;
            }
            else
            {
                return null;
            }
        }

        private int CountString(string text, string subText)
        {
            int count = 0;
            int index = text.IndexOf(subText);

            while (index != -1)
            {
                count++;

                index = text.IndexOf(subText, index + subText.Length);
            }


            return count;
        }

        private List<FileInfo> GetMasterXmls()
        {
            FileInfo htmFi = new FileInfo(m_htmFile);
            DirectoryInfo slideFolder = GetHtmFileDir(htmFi);

            FileInfo[] allfiles = slideFolder.GetFiles();

            List<FileInfo> xmls = new List<FileInfo>();

            if (allfiles != null)
            {
                for (int i = 0; i < allfiles.Length; i++)
                {
                    FileInfo f = allfiles[i];
                    String fname = f.Name;

                    if (fname.StartsWith("master")
                        && fname.EndsWith(".xml"))
                    {
                        xmls.Add(f);
                    }
                }
            }

            return xmls;
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

                    if (fname.StartsWith("slide")
                        && (fname.EndsWith(".htm") || fname.EndsWith(".html")))
                    {
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
    }

    public enum OfficeFormat
    {
        Pptx,
        Xlsx,
        Docx
    }
}
