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
                    List<TranslateUnit3RD> units = Extract(comUtil);
                    String xml = XmlUtil3RD.OutputTranslateUnits(units);
                    XmlUtil3RD.WriteXml(xml, m_xmlFileName, "UTF-8");
                }
                else
                {
                    String xml = XmlUtil3RD.ReadFile(m_xmlFileName, "UTF-8");
                    List<TranslateUnit3RD> units = XmlUtil3RD.ParseTranslateUnits(xml);
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

        private void Merge(CommandUtil comUtil, List<TranslateUnit3RD> units)
        {
            string[] filenames = comUtil.Extract(m_peFileName);
            string menurc = null, stringrc = null, dialogrc = null, versionrc = null;
            string menures = null, stringres = null, dialogres = null, versionres = null;

            try
            {
                // menu.rc
                menurc = filenames[0];
                menures = menurc.Substring(0, menurc.LastIndexOf(".")) + ".res";
                MergeOneFile(comUtil, units, menurc, menures, TranslateUnitType.MenuType, m_peFileName, m_newFileName0);

                // string.rc
                stringrc = filenames[1];
                stringres = stringrc.Substring(0, stringrc.LastIndexOf(".")) + ".res";
                MergeOneFile(comUtil, units, stringrc, stringres, TranslateUnitType.StringType, m_newFileName0, m_newFileName1);

                // dialog.rc
                dialogrc = filenames[2];
                dialogres = dialogrc.Substring(0, dialogrc.LastIndexOf(".")) + ".res";
                MergeOneFile(comUtil, units, dialogrc, dialogres, TranslateUnitType.DialogType, m_newFileName1, m_newFileName2);

                // version.rc
                versionrc = filenames[3];
                versionres = versionrc.Substring(0, versionrc.LastIndexOf(".")) + ".res";
                MergeOneFile(comUtil, units, versionrc, versionres, TranslateUnitType.VersionType, m_newFileName2, m_newFileName3);

                File.Copy(m_newFileName3, m_peFileName, true);
            }
            finally
            {
                if (!AppConfig.KeepTempFiles)
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
        }

        private void MergeOneFile(CommandUtil comUtil, List<TranslateUnit3RD> units, string rcFile, string resFile, TranslateUnitType tuType, string oriFile, String newFile)
        {
            string[] menulines = ReadAllLinesForRCFile(rcFile, tuType);

            if (menulines.Length != 0)
            {
                for (int i = 0; i < menulines.Length; i++)
                {
                    string line = menulines[i];

                    while (line.StartsWith("\0"))
                    {
                        line = line.Substring(1);
                    }

                    menulines[i] = line;
                }

                DoMerge(menulines, units, tuType, false, null);

                File.WriteAllLines(rcFile, menulines, Encoding.Unicode);
                comUtil.Compile(rcFile, resFile);
                DebugCompile(Encoding.Unicode, resFile);

                if (!File.Exists(resFile) || File.ReadAllBytes(resFile).Length == 32)
                {
                    File.WriteAllLines(rcFile, menulines, Encoding.Default);
                    comUtil.Compile(rcFile, resFile);
                    DebugCompile(Encoding.Default, resFile);
                }

                if (!File.Exists(resFile) || File.ReadAllBytes(resFile).Length == 32)
                {
                    String specalChar = "\x2063";

                    switch (tuType)
                    {
                        case TranslateUnitType.StringType: specalChar = " "; break;
                        case TranslateUnitType.DialogType: specalChar = "\x2063"; break;
                        case TranslateUnitType.MenuType: specalChar = "\x2063"; break;
                        case TranslateUnitType.VersionType: specalChar = " "; break;
                    }

                    DoMerge(menulines, units, tuType, true, specalChar);

                    File.WriteAllLines(rcFile, menulines, Encoding.Unicode);
                    comUtil.Compile(rcFile, resFile);
                    DebugCompile(Encoding.Unicode, resFile);

                    if (!File.Exists(resFile) || File.ReadAllBytes(resFile).Length == 32)
                    {
                        File.WriteAllLines(rcFile, menulines, Encoding.Default);
                        comUtil.Compile(rcFile, resFile);
                        DebugCompile(Encoding.Default, resFile);
                    }
                }

                if (!File.Exists(resFile) || File.ReadAllBytes(resFile).Length == 32)
                {
                    string log = comUtil.GetLog();
                    if (log.Contains("Error compiling script at line"))
                    {
                        StringIndex si = StringUtil.GetBetween(log, "Error compiling script at line", ":", 0, 1);

                        if (si != null && si.content != null)
                        {
                            string line = si.content.Trim();
                            //TODO: fix this line
                        }
                    }

                    m_log.Debug(log);
                }

                comUtil.Modify(oriFile, newFile, resFile);
            }

            if (!File.Exists(newFile))
            {
                File.Copy(oriFile, newFile);
            }
        }

        private void DebugCompile(Encoding encoding, String resFile)
        {
            m_log.Debug("Using " + encoding + " for Compile");
            if (File.Exists(resFile))
            {
                m_log.Debug("Result file length " + File.ReadAllBytes(resFile).Length + " (32)");
            }
            else
            {
                m_log.Debug("Result file does not exist.");
            }
        }

        private void DoMerge(string[] lines, List<TranslateUnit3RD> units, TranslateUnitType type, bool addChar, string specialChar)
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
                            StringIndex si = StringUtil.GetBetween(line, startChar, endChar, true, 0, 1);

                            if (si != null)
                            {
                                TranslateUnit3RD tu = TranslateUnitUtil3RD.GetTranslateUnit(units, "menu", lineNumber);

                                if (tu != null)
                                {
                                    string line_new = line.Substring(0, si.startIndex) + tu.TargetContent + (addChar ? specialChar : "") + line.Substring(si.endIndex);
                                    lines[i] = line_new;
                                }
                            }
                        }
                        break;

                    case TranslateUnitType.StringType:
                        if (lineTrimed.Contains(startChar) && lineTrimed.Contains(endChar))
                        {
                            StringIndex si = StringUtil.GetBetween(line, startChar, endChar, true, 0, 1);

                            if (si != null)
                            {
                                TranslateUnit3RD tu = TranslateUnitUtil3RD.GetTranslateUnit(units, "string", lineNumber);

                                if (tu != null)
                                {
                                    string line_new = line.Substring(0, si.startIndex) + tu.TargetContent + (addChar ? specialChar : "") + line.Substring(si.endIndex);
                                    lines[i] = line_new;
                                }
                            }
                        }
                        break;

                    case TranslateUnitType.DialogType:
                        if (lineTrimed.StartsWith("CAPTION") || lineTrimed.StartsWith("CONTROL"))
                        {
                            StringIndex si = StringUtil.GetBetween(line, startChar, endChar, true, 0, 1);

                            if (si != null)
                            {
                                TranslateUnit3RD tu = TranslateUnitUtil3RD.GetTranslateUnit(units, "dialog", lineNumber);

                                if (tu != null)
                                {
                                    string line_new = line.Substring(0, si.startIndex) + tu.TargetContent + (addChar ? specialChar : "") + line.Substring(si.endIndex);
                                    lines[i] = line_new;
                                }
                            }
                        }
                        break;

                    case TranslateUnitType.VersionType:
                        if (lineTrimed.StartsWith("VALUE"))
                        {
                            StringIndex si = StringUtil.GetBetween(line, startChar, endChar, true, 0, 3);

                            if (si != null)
                            {
                                TranslateUnit3RD tu = TranslateUnitUtil3RD.GetTranslateUnit(units, "version", lineNumber);

                                if (tu != null)
                                {
                                    string line_new = line.Substring(0, si.startIndex) + tu.TargetContent + (addChar ? specialChar : "") + line.Substring(si.endIndex);
                                    lines[i] = line_new;
                                }
                            }
                        }
                        break;
                }
            }
        }

        private List<TranslateUnit3RD> Extract(CommandUtil comUtil)
        {
            List<TranslateUnit> resultSelf = ParsePEFile();

            string[] filenames = comUtil.Extract(m_peFileName);
            List<TranslateUnit3RD> result = new List<TranslateUnit3RD>();
            string menurc = null, stringrc = null, dialogrc = null, versionrc = null;
            try
            {
                // menu.rc
                menurc = filenames[0];
                ExtractOneFile(result, menurc, TranslateUnitType.MenuType, resultSelf);

                // string.rc
                stringrc = filenames[1];
                ExtractOneFile(result, stringrc, TranslateUnitType.StringType, resultSelf);

                // dialog.rc
                dialogrc = filenames[2];
                ExtractOneFile(result, dialogrc, TranslateUnitType.DialogType, resultSelf);

                // version.rc
                versionrc = filenames[3];
                ExtractOneFile(result, versionrc, TranslateUnitType.VersionType, resultSelf);
            }
            finally
            {
                if (!AppConfig.KeepTempFiles)
                {
                    DeleteFile(menurc);
                    DeleteFile(stringrc);
                    DeleteFile(dialogrc);
                    DeleteFile(versionrc);
                }
            }

            return result;
        }

        private void ExtractOneFile(List<TranslateUnit3RD> result, string rcFile, TranslateUnitType tuType, List<TranslateUnit> resultSelf)
        {
            string[] menulines = ReadAllLinesForRCFile(rcFile, tuType);

            DoExtract(result, menulines, tuType, resultSelf);
        }

        private static string[] ReadAllLinesForRCFile(string rcFile, TranslateUnitType tuType)
        {
            string[] menulines = File.ReadAllLines(rcFile, Encoding.Unicode);
            string all = File.ReadAllText(rcFile, Encoding.Unicode);

            if (menulines.Length == 1)
            {
                Encoding encoding = FileUtil.GetEncoding(rcFile);
                menulines = File.ReadAllLines(rcFile, encoding);
            }

            if (all != null && all.Length > 0)
            {
                string keyWord = "";

                switch (tuType)
                {
                    case TranslateUnitType.StringType: keyWord = "STRING"; break;
                    case TranslateUnitType.DialogType: keyWord = "DIALOG"; break;
                    case TranslateUnitType.MenuType: keyWord = "MENU"; break;
                    case TranslateUnitType.VersionType: keyWord = "VERSIONINFO"; break;
                }

                if (!all.Contains(keyWord))
                {
                    Encoding encoding = FileUtil.GetEncoding(rcFile);
                    menulines = File.ReadAllLines(rcFile, encoding);
                }
            }
            return menulines;
        }

        private List<TranslateUnit> ParsePEFile()
        {
            PEUtil.ResetPEResourceIndex();
            List<TranslateUnit> result = new List<TranslateUnit>();
            int number = 0;

            byte[] binary = null;
            using (FileStream fs = new FileStream(m_peFileName, FileMode.Open))
            {
                BinaryReader br = new BinaryReader(fs);
                binary = br.ReadBytes(System.Convert.ToInt32(fs.Length));
            }

            // check if this file is PE file
            string startFlag = PEUtil.ConvertByteArrayToHexString(binary, 0, 2);
            // dos MZ header
            if (!"4D5A".Equals(startFlag))
            {
                throw new Exception("This file is not a valid PE file (not start with 4D5Ah)");
            }
            // PE signature PE00
            string allHex = PEUtil.ConvertByteArrayToHexString(binary);
            if (!allHex.Contains("50450000"))
            {
                throw new Exception("This file is not a valid PE file (not contain with 50450000h)");
            }

            // get pe header information
            PEReader peReader = new PEReader(m_peFileName);
            string name1 = PEUtil.GetSectionName(peReader.ImageSectionHeader[0]);

            PEResourceDataList resDataList = new PEResourceDataList();
            PEResourceEntries[] ResourceEntriesAll = peReader.ResourceEntriesAll;
            for (int i = 0; i < ResourceEntriesAll.Length; i++)
            {
                PEResourceEntries resourceEntries = ResourceEntriesAll[i];
                // which resouce should be extracted
                // first version information : 0Eh
                if (resourceEntries.level1Entry.Name_l >= 0)
                {
                    int vCount = resourceEntries.level2Entries.Length;
                    for (int j = 0; j < vCount; j++)
                    {
                        PEReader.IMAGE_RESOURCE_DIRECTORY_ENTRY level2 = resourceEntries.level2Entries[j];
                        object obj = resourceEntries.level2Map3Entries[level2];
                        PEReader.IMAGE_RESOURCE_DIRECTORY_ENTRY[] level3Array = (PEReader.IMAGE_RESOURCE_DIRECTORY_ENTRY[])obj;

                        for (int k = 0; k < level3Array.Length; k++)
                        {
                            PEResourceData resData = new PEResourceData();
                            PEReader.IMAGE_RESOURCE_DIRECTORY_ENTRY level3 = level3Array[k];
                            PEReader.IMAGE_RESOURCE_DATA_ENTRY data = (PEReader.IMAGE_RESOURCE_DATA_ENTRY)resourceEntries.level3DATA[level3];
                            uint dataRVA = data.OffsetToData;
                            uint dataSize = data.Size;
                            uint resRVA = peReader.ResourceRVA;
                            if (dataRVA < resRVA)
                            {
                                continue;
                            }

                            uint dataOffset = peReader.ResourceOffSet + (dataRVA - resRVA);
                            if (dataOffset + dataSize > binary.Length)
                            {
                                continue;
                            }

                            byte[] resourceData = new byte[dataSize];
                            Array.Copy(binary, dataOffset, resourceData, 0, dataSize);
                            string content = Encoding.Unicode.GetString(resourceData);

                            resData.ResourceType = resourceEntries.level1Entry.Name_l;
                            resData.FileOffset = dataOffset;
                            resData.Size = dataSize;
                            resData.Data = resourceData;
                            resData.Content = content;
                            resData.PEFileName = m_peFileName;
                            resDataList.Add(resData);
                        }
                    }
                }
            }

            foreach (PEResourceData resData in resDataList)
            {
                resData.ParseData(number);
                List<TranslateUnit> tus = resData.GetTus();
                result.AddRange(tus);
                if (tus.Count != 0)
                {
                    byte[] ddd = resData.GetSrcData();
                    int lll = ddd.Length;
                }
            }

            string peOffset = PEUtil.GetPEHeaderAddress(allHex);
            int h_peOffset = PEUtil.ConvertHexToInt(peOffset);

            bool isDotNet = true;
            Assembly ass = null;

            try
            {
                ass = Assembly.Load(binary);
                isDotNet = true;
                m_log.Log("Loading " + m_peFileName + " with Microsoft .Net parser.");
            }
            catch (BadImageFormatException)
            {
                string name = peReader.Is32BitHeader ? "Win32" : "Win32+";
                isDotNet = false;
                m_log.Log("Loading " + m_peFileName + " with " + name + " parser.");
            }

            if (isDotNet)
            {
                throw new Exception("This file is a .NET file which is not support now.");
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

        private void DoExtract(List<TranslateUnit3RD> result, string[] lines, TranslateUnitType type, List<TranslateUnit> resultSelf)
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
                            StringIndex si = StringUtil.GetBetween(line, startChar, endChar, true, 0, 1);

                            if (si != null && si.content != null && si.content.Length > 0 && si.content.Trim().Length > 0)
                            {
                                TranslateUnit3RD tu = new TranslateUnit3RD(lineNumber, "menu", si.content, si.content);

                                result.Add(tu);
                            }
                        }
                        break;

                    case TranslateUnitType.StringType:
                        if (lineTrimed.Contains(startChar) && lineTrimed.Contains(endChar))
                        {
                            StringIndex si = StringUtil.GetBetween(line, startChar, endChar, true, 0, 1);

                            if (si != null && si.content != null && si.content.Length > 0 && si.content.Trim().Length > 0)
                            {
                                TranslateUnit3RD tu = new TranslateUnit3RD(lineNumber, "string", si.content, si.content);

                                result.Add(tu);
                            }
                        }
                        break;

                    case TranslateUnitType.DialogType:
                        if (lineTrimed.StartsWith("CAPTION") || lineTrimed.StartsWith("CONTROL"))
                        {
                            StringIndex si = StringUtil.GetBetween(line, startChar, endChar, true, 0, 1);

                            if (si != null && si.content != null && si.content.Length > 0 && si.content.Trim().Length > 0)
                            {
                                TranslateUnit3RD tu = new TranslateUnit3RD(lineNumber, "dialog", si.content, si.content);

                                result.Add(tu);
                            }
                        }
                        break;

                    case TranslateUnitType.VersionType:
                        if (lineTrimed.StartsWith("VALUE"))
                        {
                            StringIndex si = StringUtil.GetBetween(line, startChar, endChar, true, 0, 3);

                            if (si != null && si.content != null && si.content.Length > 0 && si.content.Trim().Length > 0)
                            {
                                TranslateUnit3RD tu = new TranslateUnit3RD(lineNumber, "version", si.content, si.content);

                                // set id for version
                                StringIndex siID = StringUtil.GetBetween(line, startChar, endChar, true, 0, 1);
                                if (siID != null && siID.content != null && siID.content.Length > 0)
                                {
                                    tu.Id = siID.content;

                                    TranslateUnit tuSelf = TranslateUnitUtil.GetTranslateUnit(resultSelf, "RT_VERSION", tu.Id);
                                    if (tuSelf != null)
                                    {
                                        tu.SourceContent = tuSelf.SourceContent;
                                        tu.TargetContent = tuSelf.SourceContent;
                                    }

                                }

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
