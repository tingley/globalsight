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
using System.IO;
using System.Threading;
using Microsoft;
using GlobalSight.Common;
using System.Collections.Generic;
using System.Reflection;
using System.Resources;
using System.Collections;
using System.Text;
using System.Xml.Linq;
using System.Xml.XPath;
using System.Linq;
using System.Diagnostics;

// NOTE: WindowsPE Converter

namespace GlobalSight.WinPEConverter
{
    /// <summary>
    /// Implements the Converter interface. Can be used to convert
    /// exe dll - xml
    /// </summary>
    public class WinPEConverterImpl : GlobalSight.Common.Converter
    {
        private Logger m_log = null;

        // 
        // Constants
        //

        public const string EXPORT_FILE_EXT = "*.ex_command";
        public const string IMPORT_FILE_EXT = "*.im_command";

        //10 chars after . for both import and export
        private const int FILE_EXT_LEN = 10;
        public enum ConversionType { IMPORT, EXPORT };

        //Word file formats
        public const string EXE = "exe";
        public const string DLL = "dll";
        public const string XML = "xml";

        // rc 
        private const char startChar = '"';
        private const char endChar = '"';

        //
        // Members
        //

        private string m_originalFileName = null;
        private string m_newFileName = null;
        private string m_peFileName = null;
        private string m_xmlFileName = null;
        private string m_newFileName0 = null;
        private string m_newFileName1 = null;
        private string m_newFileName2 = null;
        private string m_newFileName3 = null;
        private string m_fileExtensionSearchPattern = null;
        private ConversionType m_conversionType = ConversionType.IMPORT;
        private string m_statusFileName = null;
        private bool m_isConvertBack = false;

        /// <summary>
        /// Creates WindowsPE Converter to be used for import or export.
        /// </summary>
        /// <param name="p_conversionType"> the type of conversion 
        /// (import or export)</param>
        public WinPEConverterImpl(ConversionType p_conversionType)
        {
            m_log = WinPEConverterUtil.GetLogger();
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
        /// <param name="p_filename">The command file 
        /// (.im_command, .ex_command)</param>
        /// <param name="p_language">the language of the directory 
        /// the file was written in</param>
        public void Convert(string p_fileName, string p_language)
        {
            try
            {
                ResetState();
                m_statusFileName = p_fileName.Substring(
                    0, p_fileName.Length - FILE_EXT_LEN) + "status";
                DetermineConversionValues(p_fileName);
                FileInfo file = new FileInfo(p_fileName);
                DirectoryInfo dir = file.Directory;

                m_log.Log("Processing file " + m_originalFileName);

                CommandUtil comUtil = new CommandUtil();

                // process files
                if (!m_isConvertBack)
                {
                    List<TranslateUnit> units = Extract(comUtil);
                    String xml = XmlUtil.OutputTranslateUnits(units);
                    XmlUtil.WriteXml(xml, m_xmlFileName, "UTF-8");
                }
                else
                {
                    String xml = XmlUtil.ReadFile(m_xmlFileName, "UTF-8");
                    List<TranslateUnit> units = XmlUtil.ParseTranslateUnits(xml);
                    int c = units.Count;
                    Merge(comUtil, units);
                }

                StatusFile.WriteSuccessStatus(m_statusFileName,
                    m_originalFileName + " was converted successfully.");

                m_log.Log("Converted successfully to " + m_newFileName);
            }
            catch (Exception e)
            {
                Logger.LogError(m_log, "Windows Portable Executable Conversion Failed", e);
                StatusFile.WriteErrorStatus(m_statusFileName, e, (int)1);
            }
            finally
            {
                DeleteInputFile(p_fileName);
            }
        }

        private void Merge(CommandUtil comUtil, List<TranslateUnit> units)
        {
            string[] filenames = comUtil.Extract(m_peFileName);
            string menurc = null, stringrc = null, dialogrc = null, versionrc = null;
            string menures = null, stringres = null, dialogres = null, versionres = null;

            try
            {
                // menu.rc
                menurc = filenames[0];
                menures = menurc.Substring(0, menurc.LastIndexOf(".")) + ".res";
                string[] menulines = File.ReadAllLines(menurc);
                DoMerge(menulines, units, TranslateUnitType.MenuType, menurc, Encoding.Default);
                comUtil.CompileAndModify(m_peFileName, m_newFileName0, menurc, menures);

                if (!File.Exists(m_newFileName0))
                {
                    File.Copy(m_peFileName, m_newFileName0);
                }

                // string.rc
                stringrc = filenames[1];
                stringres = stringrc.Substring(0, stringrc.LastIndexOf(".")) + ".res";
                string[] stringlines = File.ReadAllLines(stringrc);
                DoMerge(stringlines, units, TranslateUnitType.StringType, stringrc, Encoding.Default);
                comUtil.CompileAndModify(m_newFileName0, m_newFileName1, stringrc, stringres);

                if (!File.Exists(m_newFileName1))
                {
                    File.Copy(m_newFileName0, m_newFileName1);
                }

                // dialog.rc
                dialogrc = filenames[2];
                dialogres = dialogrc.Substring(0, dialogrc.LastIndexOf(".")) + ".res";
                string[] dialoglines = File.ReadAllLines(dialogrc, Encoding.Unicode);
                DoMerge(dialoglines, units, TranslateUnitType.DialogType, dialogrc, Encoding.Unicode);
                comUtil.CompileAndModify(m_newFileName1, m_newFileName2, dialogrc, dialogres);

                if (!File.Exists(m_newFileName2))
                {
                    File.Copy(m_newFileName1, m_newFileName2);
                }

                // version.rc
                versionrc = filenames[3];
                versionres = versionrc.Substring(0, versionrc.LastIndexOf(".")) + ".res";
                string[] versionlines = File.ReadAllLines(versionrc);
                DoMerge(versionlines, units, TranslateUnitType.VersionType, versionrc, Encoding.Default);
                comUtil.CompileAndModify(m_newFileName2, m_newFileName3, versionrc, versionres);

                if (!File.Exists(m_newFileName3))
                {
                    File.Copy(m_newFileName2, m_newFileName3);
                }

                File.Copy(m_newFileName3, m_peFileName, true);
            }
            finally
            {
                DeleteFile(menurc);
                DeleteFile(stringrc);
                DeleteFile(dialogrc);
                DeleteFile(versionrc);

                DeleteFile(menures);
                DeleteFile(stringres);
                DeleteFile(dialogres);
                DeleteFile(versionres);

                DeleteFile(m_newFileName0);
                DeleteFile(m_newFileName1);
                DeleteFile(m_newFileName2);
                DeleteFile(m_newFileName3);
            }
        }

        private void DoMerge(string[] lines, List<TranslateUnit> units, TranslateUnitType type, string filepath, Encoding encoding)
        {
            if (lines == null || lines.Length == 0 || units == null || units.Count == 0)
            {
                return;
            }

            for (int i = 0; i < lines.Length; i++)
            {
                string line = lines[i];
                int lineNumber = i + 1;
                string lineTrimed = line.Trim();

                switch (type)
                {
                    case TranslateUnitType.MenuType:
                        if (lineTrimed.StartsWith("POPUP") || lineTrimed.StartsWith("MENUITEM"))
                        {
                            StringIndex si = StringUtil.GetBetween(line, startChar, endChar, 0, 1);

                            if (si != null)
                            {
                                TranslateUnit tu = TranslateUnitUtil.GetTranslateUnit(units, "menu", lineNumber);

                                if (tu != null)
                                {
                                    string line_new = line.Substring(0, si.startIndex) + tu.TargetContent + line.Substring(si.endIndex);
                                    lines[i] = line_new;
                                }
                            }
                        }
                        break;

                    case TranslateUnitType.StringType:
                        if (lineTrimed.Contains(startChar) && lineTrimed.Contains(endChar))
                        {
                            StringIndex si = StringUtil.GetBetween(line, startChar, endChar, 0, 1);

                            if (si != null)
                            {
                                TranslateUnit tu = TranslateUnitUtil.GetTranslateUnit(units, "string", lineNumber);

                                if (tu != null)
                                {
                                    string line_new = line.Substring(0, si.startIndex) + tu.TargetContent + line.Substring(si.endIndex);
                                    lines[i] = line_new;
                                }
                            }
                        }
                        break;

                    case TranslateUnitType.DialogType:
                        if (lineTrimed.StartsWith("CAPTION") || lineTrimed.StartsWith("CONTROL"))
                        {
                            StringIndex si = StringUtil.GetBetween(line, startChar, endChar, 0, 1);

                            if (si != null)
                            {
                                TranslateUnit tu = TranslateUnitUtil.GetTranslateUnit(units, "dialog", lineNumber);

                                if (tu != null)
                                {
                                    string line_new = line.Substring(0, si.startIndex) + tu.TargetContent + line.Substring(si.endIndex);
                                    lines[i] = line_new;
                                }
                            }
                        }
                        break;

                    case TranslateUnitType.VersionType:
                        if (lineTrimed.StartsWith("VALUE"))
                        {
                            StringIndex si = StringUtil.GetBetween(line, startChar, endChar, 0, 3);

                            if (si != null)
                            {
                                TranslateUnit tu = TranslateUnitUtil.GetTranslateUnit(units, "version", lineNumber);

                                if (tu != null)
                                {
                                    string line_new = line.Substring(0, si.startIndex) + tu.TargetContent + line.Substring(si.endIndex);
                                    lines[i] = line_new;
                                }
                            }
                        }
                        break;
                }
            }

            File.WriteAllLines(filepath, lines, encoding);
        }

        private List<TranslateUnit> Extract(CommandUtil comUtil)
        {
            string[] filenames = comUtil.Extract(m_peFileName);
            List<TranslateUnit> result = new List<TranslateUnit>();
            string menurc = null, stringrc = null, dialogrc = null, versionrc = null;
            try
            {
                // menu.rc
                menurc = filenames[0];
                string[] menulines = File.ReadAllLines(menurc);
                DoExtract(result, menulines, TranslateUnitType.MenuType);

                // string.rc
                stringrc = filenames[1];
                string[] stringlines = File.ReadAllLines(stringrc);
                DoExtract(result, stringlines, TranslateUnitType.StringType);

                // dialog.rc
                dialogrc = filenames[2];
                string[] dialoglines = File.ReadAllLines(dialogrc, Encoding.Unicode);
                DoExtract(result, dialoglines, TranslateUnitType.DialogType);

                // version.rc
                versionrc = filenames[3];
                string[] versionlines = File.ReadAllLines(versionrc);
                DoExtract(result, versionlines, TranslateUnitType.VersionType);
            }
            finally
            {
                DeleteFile(menurc);
                DeleteFile(stringrc);
                DeleteFile(dialogrc);
                DeleteFile(versionrc);
            }

            return result;
        }

        private void DeleteFile(string file)
        {
            try
            {
                File.Delete(file);
            }
            catch { }
        }

        private void DoExtract(List<TranslateUnit> result, string[] lines, TranslateUnitType type)
        {
            if (lines == null || lines.Length == 0)
            {
                return;
            }

            for (int i = 0; i < lines.Length; i++)
            {
                string line = lines[i];
                int lineNumber = i + 1;
                string lineTrimed = line.Trim();

                switch (type)
                {
                    case TranslateUnitType.MenuType:
                        if (lineTrimed.StartsWith("POPUP") || lineTrimed.StartsWith("MENUITEM"))
                        {
                            StringIndex si = StringUtil.GetBetween(line, startChar, endChar, 0, 1);

                            if (si != null && si.content != null && si.content.Length > 0 && si.content.Trim().Length > 0)
                            {
                                TranslateUnit tu = new TranslateUnit(lineNumber, "menu", si.content, si.content);

                                result.Add(tu);
                            }
                        }
                        break;

                    case TranslateUnitType.StringType:
                        if (lineTrimed.Contains(startChar) && lineTrimed.Contains(endChar))
                        {
                            StringIndex si = StringUtil.GetBetween(line, startChar, endChar, 0, 1);

                            if (si != null && si.content != null && si.content.Length > 0 && si.content.Trim().Length > 0)
                            {
                                TranslateUnit tu = new TranslateUnit(lineNumber, "string", si.content, si.content);

                                result.Add(tu);
                            }
                        }
                        break;

                    case TranslateUnitType.DialogType:
                        if (lineTrimed.StartsWith("CAPTION") || lineTrimed.StartsWith("CONTROL"))
                        {
                            StringIndex si = StringUtil.GetBetween(line, startChar, endChar, 0, 1);

                            if (si != null && si.content != null && si.content.Length > 0 && si.content.Trim().Length > 0)
                            {
                                TranslateUnit tu = new TranslateUnit(lineNumber, "dialog", si.content, si.content);

                                result.Add(tu);
                            }
                        }
                        break;

                    case TranslateUnitType.VersionType:
                        if (lineTrimed.StartsWith("VALUE"))
                        {
                            StringIndex si = StringUtil.GetBetween(line, startChar, endChar, 0, 3);

                            if (si != null && si.content != null && si.content.Length > 0 && si.content.Trim().Length > 0)
                            {
                                TranslateUnit tu = new TranslateUnit(lineNumber, "version", si.content, si.content);

                                result.Add(tu);
                            }
                        }
                        break;
                }
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
        /// Sets all internal state back to null
        /// </summary>
        private void ResetState()
        {
            m_originalFileName = null;
            m_newFileName = null;
            m_newFileName0 = null;
            m_newFileName1 = null;
            m_newFileName2 = null;
            m_newFileName3 = null;
            m_peFileName = null;
            m_xmlFileName = null;
            m_statusFileName = null;
            m_isConvertBack = false;
        }

        /// <summary>
        /// Reads the command file to figure out specific values for
        /// the conversion such as the new filename, / the format type,
        /// whether to track changes, etc.
        /// </summary>
        /// <param name="p_fileName">the command file (.im_command) or (.ex_command)</param>
        private void DetermineConversionValues(string p_fileName)
        {
            // Since this happens in a separate process, it should sleep
            // until the file is completely written out (to prevent the
            // file sharing exception).
            bool successful = false;
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

        /// <summary>

        /// Reads the command file to figure out specific values for
        /// the conversion such as the new filename, / the format type,
        /// whether to track changes, etc.
        /// </summary>
        /// <param name="p_fileName">the command file (.im_command) or (.ex_command)</param>
        private void _DetermineConversionValues(string p_fileName)
        {
            FileInfo fi = new FileInfo(p_fileName);
            StreamReader sr = fi.OpenText();
            char[] delimiters = new char[1];
            delimiters[0] = '=';

            // the ConvertTo and ConvertFrom lines can be exe or dll; 
            // for example:
            //ConvertFrom=exe
            //ConvertTo=xml
            string convertFrom = sr.ReadLine().Split(delimiters, 2)[1];
            string convertTo = sr.ReadLine().Split(delimiters, 2)[1];
            setConversionType(convertFrom, convertTo, p_fileName);

            sr.Close();
        }

        /// <summary>
        /// Sets the conversion type (exe, dll)
        /// </summary>
        /// <param name="p_convertFrom">ConvertFrom value from the command file</param>
        /// <param name="p_convertTo">ConverTo value from the command file</param>
        /// <param name="p_fileName">the command file</param>
        private void setConversionType(string p_convertFrom, string p_convertTo, string p_fileName)
        {
            string baseFileName = p_fileName.Substring(
                0, p_fileName.Length - FILE_EXT_LEN);
            m_originalFileName = baseFileName + p_convertFrom.ToLower();

            if (p_convertTo.Equals(XML))
            {
                m_newFileName = baseFileName + XML;
                m_peFileName = m_originalFileName;
                m_xmlFileName = m_newFileName;
                m_isConvertBack = false;
            }
            else if (p_convertTo.Equals(EXE))
            {
                m_newFileName = baseFileName + EXE;
                m_newFileName0 = baseFileName + "0."+ EXE;
                m_newFileName1 = baseFileName + "1."+ EXE;
                m_newFileName2 = baseFileName + "2." + EXE;
                m_newFileName3 = baseFileName + "3." + EXE;
                m_peFileName = m_newFileName;
                m_xmlFileName = m_originalFileName;
                m_isConvertBack = true;
            }
            else if (p_convertTo.Equals(DLL))
            {
                m_newFileName = baseFileName + DLL;
                m_newFileName0 = baseFileName + "0." + DLL;
                m_newFileName1 = baseFileName + "1." + DLL;
                m_newFileName2 = baseFileName + "2." + DLL;
                m_newFileName3 = baseFileName + "3." + DLL;
                m_peFileName = m_newFileName;
                m_xmlFileName = m_originalFileName;
                m_isConvertBack = true;
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
}
