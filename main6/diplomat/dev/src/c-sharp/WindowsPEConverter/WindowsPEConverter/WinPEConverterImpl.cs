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
using Mono.Cecil;
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
        private bool m_isTesting = false;

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

        //
        // Members
        //

        private string m_originalFileName = null;
        private string m_newFileName = null;
        private string m_peFileName = null;
        private string m_xmlFileName = null;
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

                m_log.Log("Processing file " + m_originalFileName);

                // process files
                if (!m_isConvertBack)
                {
                    List<TranslateUnit> units = ParsePEFile();
                    String xml = XmlUtil.OutputTranslateUnits(units);
                    XmlUtil.WriteXml(xml, m_xmlFileName, "UTF-8");
                }
                else
                {
                    String xml = XmlUtil.ReadFile(m_xmlFileName, "UTF-8");
                    List<TranslateUnit> units = XmlUtil.ParseTranslateUnits(xml);
                    int c = units.Count;
                    WritePEResource(units);
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

        private void WritePEResource(List<TranslateUnit> units)
        {
            PEUtil.ResetPEResourceIndex();
            // backup file first if none
            string backupFile = m_peFileName + ".bak";
            if (!File.Exists(backupFile))
            {
                File.Copy(m_peFileName, backupFile);
            }

            int number = 0;
            byte[] binary = null;
            using (FileStream fs = new FileStream(backupFile, FileMode.Open))
            {
                BinaryReader br = new BinaryReader(fs);
                binary = br.ReadBytes(System.Convert.ToInt32(fs.Length));
            }

            PEReader peReader = new PEReader(backupFile);
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
                            uint dataOffset = peReader.ResourceOffSet + (dataRVA - resRVA);
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
                if (tus.Count != 0)
                {
                    byte[] ddd = resData.GetSrcData();
                    byte[] nnn = resData.Merge(units);

                    uint resOffset = resData.FileOffset;
                    uint resSize = resData.Size;
                    PEUtil.UpdateBinary(binary, resOffset, resSize, nnn);
                }
            }

            // update resource - version first
            /*
            PEReader peReader = new PEReader(m_peFileName);
            PEResourceEntries[] ResourceEntriesAll = peReader.ResourceEntriesAll;
            for (int i = 0; i < ResourceEntriesAll.Length; i++)
            {
                PEResourceEntries resourceEntries = ResourceEntriesAll[i];
                // which resouce should be extracted
                // first version information : 0Eh
                if (resourceEntries.level1Entry.Name_l == PEResoourceType.RT_VERSION)
                {
                    int vCount = resourceEntries.level2Entries.Length;
                    for (int j = 0; j < vCount; j++)
                    {
                        PEReader.IMAGE_RESOURCE_DIRECTORY_ENTRY level2 = resourceEntries.level2Entries[j];
                        object obj = resourceEntries.level2Map3Entries[level2];
                        PEReader.IMAGE_RESOURCE_DIRECTORY_ENTRY[] level3Array = (PEReader.IMAGE_RESOURCE_DIRECTORY_ENTRY[])obj;

                        for (int k = 0; k < level3Array.Length; k++)
                        {
                            PEReader.IMAGE_RESOURCE_DIRECTORY_ENTRY level3 = level3Array[k];
                            PEReader.IMAGE_RESOURCE_DATA_ENTRY data = (PEReader.IMAGE_RESOURCE_DATA_ENTRY)resourceEntries.level3DATA[level3];
                            uint dataRVA = data.OffsetToData;
                            uint dataSize = data.Size;
                            uint resRVA = peReader.ResourceRVA;
                            uint dataOffset = peReader.ResourceOffSet + (dataRVA - resRVA);
                            byte[] resourceData = new byte[dataSize];
                            Array.Copy(binary, dataOffset, resourceData, 0, dataSize);

                            
                            string versionInfor = Encoding.Unicode.GetString(resourceData);
                            //string newversionInfor = versionInfor.Replace("微软中国", "童年著作");
                            PEUtil.UpdateVersionInfoWithWinAPI(m_peFileName, resourceData, units);
                            //string newversionInfor = PEUtil.UpdateVersionInfo(versionInfor, units);
                            //byte[] newResourceData = new byte[dataSize];
                            //newResourceData = Encoding.Unicode.GetBytes(newversionInfor);

                            //Array.Copy(newResourceData, 0, binary, dataOffset, dataSize);
                        }
                    }
                }
            }
            */

            // write data back to file
            using (FileStream fs = new FileStream(m_peFileName, FileMode.Open))
            {
                BinaryWriter bw = new BinaryWriter(fs);
                bw.Write(binary);
            }
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
            if (!"4D5A".Equals(startFlag))
            {
                throw new Exception("This file is not a valid PE file (not start with 4D5Ah)");
            }
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
                // mono
                AssemblyDefinition asm = MonoUtil.LoadAssembly(m_peFileName);
                ModuleDefinition module = asm.MainModule;
                foreach (Resource r in module.Resources.OrderBy(m => m.Name))
                {
                    if (r is EmbeddedResource)
                    {
                        EmbeddedResource er = r as EmbeddedResource;
                        if (er != null)
                        {
                            Stream s = er.GetResourceStream();
                            s.Position = 0;
                            ResourceReader reader;
                            try
                            {
                                reader = new ResourceReader(s);
                            }
                            catch (ArgumentException ae)
                            {
                                throw ae;
                            }
                            foreach (DictionaryEntry entry in reader.Cast<DictionaryEntry>().OrderBy(e => e.Key.ToString()))
                            {
                                var keyString = entry.Key.ToString();

                                if (entry.Value is String)
                                {
                                    TranslateUnit unit = new TranslateUnit("" + (++number), keyString, "0", "0", (string)entry.Value);
                                    unit.Category = er.Name;
                                    result.Add(unit);
                                    continue;
                                }

                                if (entry.Value is byte[])
                                {
                                    Stream ms = new MemoryStream((byte[])entry.Value);

                                }

                                if (entry.Value is Stream && keyString.ToLower().EndsWith(".baml"))
                                {
                                    Stream ps = entry.Value as Stream;
                                    ps.Position = 0;

                                    string textContent = "";
                                    string id = "";
                                    XDocument xdoc = BamlUtil.LoadIntoDocument(new MyAssemblyResolver(), asm, ps);
                                    string xxx = xdoc.ToString();

                                    IEnumerable<XElement> elements = xdoc.Elements();
                                    XElement xroot = elements.First<XElement>();
                                    
                                    // get TextBlock
                                    //XName name = XName.Get("TextBlock", baml_xmlns);
                                    elements = XmlUtil.SelectElementsByName(xroot, "TextBlock");
                                    foreach (XElement element in elements)
                                    {
                                        XAttribute xatt = XmlUtil.SelectAttributeByName(element, "Text", "Text");
                                        if (xatt != null)
                                        {
                                            textContent = xatt.Value;
                                            id = BamlUtil.GetTUID("Uid", XmlUtil.SelectAttributeByName(element, "Uid", "x:Uid").Value);
                                        }
                                        else
                                        {
                                            textContent = element.Value;

                                            XElement parent = element.Parent;
                                            if (parent.Name.LocalName.Equals("Button"))
                                            {

                                                id = BamlUtil.GetButtonId(parent) + ".TextBlock";
                                            }
                                        }

                                        TranslateUnit unit = new TranslateUnit("" + (++number), id, "0", "0", textContent);
                                        unit.Category = keyString;
                                        result.Add(unit);
                                    }

                                    // get Button and CheckBox : ContentControl.Content , Name
                                    //name = XName.Get("Button", baml_xmlns);
                                    //elements = xdoc.Descendants(name);
                                    elements = XmlUtil.SelectElementsByName(xroot, "Button");
                                    foreach (XElement element in elements)
                                    {
                                        XAttribute xatt = XmlUtil.SelectAttributeByName(element, "Content", "ContentControl.Content");

                                        if (xatt != null)
                                        {
                                            textContent = xatt.Value;
                                            id = BamlUtil.GetButtonId(element) + ".Content";

                                            TranslateUnit unit = new TranslateUnit("" + (++number), id, "0", "0", textContent);
                                            unit.Category = keyString;
                                            result.Add(unit);
                                        }
                                    }
                                    //name = XName.Get("CheckBox", baml_xmlns);
                                    //elements = xdoc.Descendants(name);
                                    elements = XmlUtil.SelectElementsByName(xroot, "CheckBox");
                                    foreach (XElement element in elements)
                                    {
                                        XAttribute xatt = XmlUtil.SelectAttributeByName(element, "Content", "ContentControl.Content");

                                        if (xatt != null)
                                        {
                                            textContent = xatt.Value;
                                            id = BamlUtil.GetButtonId(element) + ".Content"; ;

                                            TranslateUnit unit = new TranslateUnit("" + (++number), id, "0", "0", textContent);
                                            unit.Category = keyString;
                                            result.Add(unit);
                                        }

                                        XAttribute xatt2 = XmlUtil.SelectAttributeByName(element, "ToolTip", "FrameworkElement.ToolTip");

                                        if (xatt2 != null)
                                        {
                                            textContent = xatt2.Value;
                                            id = BamlUtil.GetButtonId(element) + ".ToolTip"; ;

                                            TranslateUnit unit = new TranslateUnit("" + (++number), "0", "0", id, textContent);
                                            unit.Category = keyString;
                                            result.Add(unit);
                                        }
                                    }

                                    // others, add later
                                }
                            }
                        }
                    }
                }
            }

            return result;
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
                m_peFileName = m_newFileName;
                m_xmlFileName = m_originalFileName;
                m_isConvertBack = true;
            }
            else if (p_convertTo.Equals(DLL))
            {
                m_newFileName = baseFileName + DLL;
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
