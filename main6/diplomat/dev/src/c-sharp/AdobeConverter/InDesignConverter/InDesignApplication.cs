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
using System.Collections;
using System.Collections.Generic;
using System.Text;
using GlobalSight.Common;
using System.Runtime.InteropServices;
using System.Threading;
using System.Xml;
using System.Text.RegularExpressions;
using System.IO;

namespace GlobalSight.InDesignConverter
{
    public class InDesignApplication
    {
        private static InDesignApplication m_InDesign = null;
        private const string INDD_COM_STRING = "InDesign.Application.CS2";
        private const string INDD_POPUP_DIALOG_CLASSNAME = "#32770";
        private const string INDD_POPUP_DIALOG_TITLE = "Adobe InDesign";
        private const string INDD_APP_CLASSNAME = "indesign";
        private const string INDD_APP_WINDOW_TITLE = "Adobe indesign cs2";
        private static bool isInddappBlocked = true;
        private static bool isDocumentOpened = false;
        private Logger m_log = null;

        // the number of opened InDesign files at the same time
        static private int m_openedFileNumber = 0;

        // File formats to be converted.
        private const string XML = "xml";
        private const string INDD = "indd";
        private const string XMP_POSTFIX = ".xmp";
        // Add INX file formats
        private const string inx = "inx";

        // XML tag name used to mark Indd file.
        private const string INDD_STORY_TAG = "Inddgsstory";
        private const string INDD_PARAGRAPH_TAG = "Inddgsparagraph";
        private const string INDD_WORD_TAG = "Inddgsword";
        private const string INDD_TABLE_TAG = "Inddgstable";
        private const string INDD_FOOTNOTE_TAG = "Inddgsfootnote";
        private const string FONT_FAMILY_ATTRIBUTE = "InddFontFamily";
        private const string FONT_STYLE_ATTRIBUTE = "InddFontStyle";
        private const string FONT_SIZE_ATTRIBUTE = "InddFontSize";

        private const string INDD_XMLCONTENT = "";

        private const string TRANSLATED_TEXT_LENGTH = "translatedtextlength";
        private const string PARAGRAPH_LAST_WHITE_SPACE_COUNT = "lastSpaceCount";

        private const string PARAGRAPH_HAS_DIFFERENT_STYLE = "hasDifferentStyle";
        private const string PARAGRAPH_DIFFERENT_VALUE_TRUE = "true";
        private const string PARAGRAPH_DIFFERENT_VALUE_FALSE = "false";

        private const string PARAGRAPH_HAS_LINEBREAK = "hasLinebreak";      
        private const string PARAGRAPH_LINEBREAK_VALUE_FALSE = "false";     

        private const string FIRST_WORD_START_WITH_INVISIBLE_CHAR = "firstwordstartwithinvisiblechar";
        private const string START_WITH_INVISIBLE_CHAR_VALUE_TRUE = "true";
        private const string START_WITH_INVISIBLE_CHAR_VALUE_FLASE = "false";

        private const string PARAGRAPH_BULLET_NUMBERING_TYPE = "bulletnumbertype";
        private const string BULLET_LIST_TYPE = "bullettype";
        private const string NO_ID_LIST_TYPE = "noidtype";
        private const string NUMBERED_LIST_TYPE = "numberedtype";

        private const string PARAGRAPH_SPACE_BEFORE = "spacebefore";
        private const string PARAGRAPH_SPACE_AFTER = "spaceafter";
        private const string PARAGRAPH_LEFT_INDENT = "leftindent";

        // Members
        private InDesign.Application m_inDesignApp = null;
        private InDesign.Document m_inDesignDoc = null;
        private Hashtable fontTable = null;
        private Hashtable inDesignUnknownFontTable = null;
        //add parameter for master layer translate swith.
        //default:true
        private bool m_masterTranslated = true;

        public static InDesignApplication getInstance()
        {
            if (m_InDesign == null)
            {
                m_InDesign = new InDesignApplication();
            }

            if (!HasInDesignAppWindow())
            {
                m_InDesign.ReopenInDesignApp();
            }

            return m_InDesign;
        }

        public static bool HasInDesignAppWindow()
        {
            IntPtr ptrIndd = IntPtr.Zero;
            ptrIndd = Win32Pinvoker.FindWindow(INDD_APP_CLASSNAME,
                INDD_APP_WINDOW_TITLE);
            if (ptrIndd != IntPtr.Zero)
            {
                return true;
            }
            else
            {
                return false;
            }
        }

        public void ReopenInDesignApp()

        {
            m_inDesignApp = null;
            fontTable = null;
            CreateInDesignAppClass();
        }

        public void CloseInDesignApp()
        {
            if (m_inDesignApp != null)
            {
                if ( HasInDesignAppWindow() )
                {
                    m_inDesignApp.Quit(InDesign.idSaveOptions.idYes);
                }
                else
                {
                    m_inDesignApp = null;
                }
            }
        }

        private InDesignApplication()

        {
            m_log = Logger.GetLogger();

            CreateInDesignAppClass();
        }

        /// <summary>
        /// Creates the InDesign Application Class and sets the default web options
        /// (UTF-8, optimize for browser, etc.)
        /// </summary>
        private void CreateInDesignAppClass()

        {
            if (m_inDesignApp == null)
            {
                object obj = COMCreateObject(INDD_COM_STRING);
                m_inDesignApp = (InDesign.Application)obj;
                if (!HasInDesignAppWindow())
                {
                    m_log.Log("[Indesign]: waiting InDesign Application to start up ... ");
                    Thread.Sleep(500);
                }
                InitializeFontTable();
                m_log.Log("[Indesign]: Create the " + INDD_COM_STRING + " successfully.");
            }
        }

        /// <summary>
        /// Creates a COM object given it's ProgID.
        /// </summary>
        /// <param name="sProgID">The ProgID to create</param>
        /// <returns>The newly created object, or null on failure.</returns>
        private object COMCreateObject(string sProgID)
        {
            // Get the type using just the ProgID
            Type oType = Type.GetTypeFromProgID(sProgID);
            if (oType != null)
            {
                return Activator.CreateInstance(oType);
            }
            else
            {
                m_log.Log("[Indesign]: Create the new COM object " + sProgID + " failure.");
            }

            return null;
        }

        /// <summary>
        /// The proper font means that the return font has same font family
        ///  if the font style is not existed.
        /// For example, to search font "Arial Black(family)" + Italic(style)
        /// if "Arial Black Italic" is existed, will return "Arial Black Italic"
        /// if "Arial Black Italic" is not existed, will return "Arial Black Regular".
        /// </summary>
        private InDesign.Font GetProperFont(string p_family, string p_style)

        {
            InDesign.Font resultFont = null;

            if (fontTable == null)
            {
                InitializeFontTable();
            }

            string key = GenerateFontKey(p_family, p_style);
            if (fontTable.ContainsKey(key))
            {
                resultFont = (InDesign.Font)fontTable[key];
            }
            else if (inDesignUnknownFontTable != null && 
                inDesignUnknownFontTable.ContainsKey(key))
            { 
                resultFont = (InDesign.Font)inDesignUnknownFontTable[key];
            }
            else
            {
                foreach (object obj in fontTable.Keys)
                {
                    string fontkey = (string)obj;
                    string familykey = GenerateFontFamilyKey(p_family);
                    if (fontkey.StartsWith(familykey))
                    {
                        resultFont = (InDesign.Font)fontTable[fontkey];
                        break;
                    }
                }
            }

            return resultFont;
        }

        /// <summary>
        /// return string: font key.
        /// </summary>
        private string GenerateFontKey(string p_family, string p_style)

        {
            // In InDesign, a font is identified uniquely by its family and style
            // such as: Arial(family) + Bold(style)
            return GenerateFontFamilyKey(p_family) + p_style;
        }

        /// <summary>
        /// return string: font family key.
        /// </summary>
        private string GenerateFontFamilyKey(string p_family)

        {
            // Use number sign "#" to identify the end of FontFamily string,
            // in order to distinguish fonts with same substring, 
            // such as: Arial and Arial Black.
            return p_family + "#";
        }

        /// <summary>
        /// Get all fonts supported by InDesign and store them in a hashtable,
        /// so that the proper font can be find to update paragraph's style.
        /// </summary>
        private void InitializeFontTable()

        {
            InDesign.Font font = null;

            if (fontTable == null)
            {
                fontTable = new Hashtable();
            }

            string key = null;
            for (int i = 0; i <m_inDesignApp.Fonts.Count; i++)
            {
                if (i == 0)
                {
                    font = (InDesign.Font)m_inDesignApp.Fonts.FirstItem();
                }
                else
                {
                    font = (InDesign.Font)m_inDesignApp.Fonts.NextItem(font);
                }

                key = GenerateFontKey(font.FontFamily, font.FontStyleName);
                if (!fontTable.ContainsKey(key))
                {
                    fontTable.Add(key, font);
                }
            }
        }

        private void resetUnknownFontTable()
        {
            if (inDesignUnknownFontTable != null)
            {
                inDesignUnknownFontTable.Clear();
                inDesignUnknownFontTable = null;
            }
        }

        /// <summary>
        /// Opens the indd template file 
        /// and import the xml into the indd template file.
        /// </summary>
        public void ConvertXmlToIndd(string p_xmlFileName, string p_inddFileName, bool p_masterTranslated)

        {
        	// Set master layers translate switch
        	m_masterTranslated = p_masterTranslated;
            try
            {
                OpenInDesignDoc(p_inddFileName);
                UnmarkInddFile();              
                resetUnknownFontTable();
                MarkupInddFile();
                PreImport(p_xmlFileName);
                m_inDesignDoc.ImportXML(p_xmlFileName);
                UpdateParagraphStyle();
                ImportXMP(p_inddFileName);
            }
            catch (Exception e)
            {
                throw e;
            }
            finally
            {
                SaveDocument(p_inddFileName);
            }
        }

        private void PreImport(string p_xmlFileName)
        {           
            XmlDocument xmlDoc = new XmlDocument();
            xmlDoc.Load(p_xmlFileName);
            XmlNodeList storyList = xmlDoc.SelectSingleNode("Root").ChildNodes;
            bool shoulSaveAsNewFile = false;

            foreach (XmlNode story in storyList)
            {
                if (story.NodeType != XmlNodeType.Element)
                {
                    continue;
                }

                XmlElement storyElement = (XmlElement)story;
                XmlNodeList paraList = storyElement.ChildNodes;
                string styleIndicationTag = null;
                string innerText = null;
                int paraCount = paraList.Count;
                for (int i = 0; i < paraCount; i++)
                {
                    XmlNode para = paraList[i];
                    bool lastInStory = false;
                    if (para.NodeType != XmlNodeType.Element)
                    {
                        continue;
                    }

                    XmlElement paraElement = (XmlElement)para;

                    if (paraElement.ChildNodes.Count > 1)
                    {
                        // This paragraph may have table or footnote, here we don't 
                        // process it.
                        continue;
                    }

                    if (paraElement.ChildNodes.Count == 1 &&
                        paraElement.FirstChild.NodeType != XmlNodeType.Text)
                    {
                        // The only child is not text, it may be a table or footnote.
                        continue;                       
                    }
                    if ((paraCount == 1) || 
                        ((paraCount > 1) && (i == paraCount - 1)))
                    {
                        lastInStory = true;
                    }
                    
                    styleIndicationTag = paraElement.GetAttribute(PARAGRAPH_HAS_DIFFERENT_STYLE);
                    if (styleIndicationTag.Equals(PARAGRAPH_DIFFERENT_VALUE_TRUE))
                    {
                        // Here we know this paragraph has different fonts.
                        // extract these fonts information into attributes.
                        processParagraphFont(paraElement, lastInStory);
                        shoulSaveAsNewFile = true; 
                    }
                    else if (paraElement.GetAttribute(PARAGRAPH_HAS_LINEBREAK).Trim().Length == 0)
                    {
                        // Sometimes a paragraph's boundary char could have been deleted during translation
                        // or any other conditions.
                        // If we found this case we should append this missed paragraph's boundary char, as 
                        // InDesign will make a mistake when importing xml file in this case.
                        // int 8233 represents the paragraph's boundary char.
                        if (!lastInStory)
                        {
                            innerText = paraElement.InnerText;
                            if (((innerText.Length > 0) && (Char.ConvertToUtf32(innerText, innerText.Length - 1) != 8233)) ||
                            (innerText.Length == 0))
                            {
                                paraElement.InnerText = innerText + Char.ConvertFromUtf32(8233);
                            }

                            shoulSaveAsNewFile = true;
                        }
                        
                    }
                }
            }

            // Because of an Indesign's bug - all the text but header info of a xml file exported
            // from an indd file appears in a line, and c# xml namespace class will break them into
            // lines by each tag unit.
            // If we process a xml file exported from indd using C# namespace class, and then import
            // the processed xml into indd, some pictures will overlap.
            // So, we have to recompose the text of a xml file processed into its original format.
            if (shoulSaveAsNewFile)
            {
                string tempFileName = p_xmlFileName + ".temp";
                xmlDoc.Save(tempFileName);
                StreamReader reader = new StreamReader(tempFileName, Encoding.UTF8);
                StreamWriter writer = new StreamWriter(p_xmlFileName, false, Encoding.UTF8);
                string lineText = reader.ReadLine();
                string trimed = null;
                if (lineText != null)
                {
                    lineText = lineText + "\n";
                    writer.Write(lineText);
                }
                while ((lineText = reader.ReadLine()) != null)
                {
                    trimed = lineText.Trim();
                    if (trimed.StartsWith("<Table"))
                    {
                        writer.Write(lineText);
                    }
                    else
                    {
                        writer.Write(trimed);
                    }
                    
                }
                reader.Close();
                writer.Close();
                File.Delete(tempFileName);
            }
           
        }

        private void processParagraphFont(XmlElement p_paraElement, bool p_lastInStory)
        {
            string openFontTag = @"\[([^/-]+-[^-]+-[^\]]+)\]";
            string closeFontTag = @"\[/([^-]+-[^-]+-[^\]]+)\]";
            string oriInnerText = p_paraElement.InnerText;
            StringBuilder newInnerText = new StringBuilder();
            int indexForAttribute = 0;
            string indexPrefix = "index_back_";
            string tagName = null;
            Regex openRe = new Regex(openFontTag);
            Regex closeRe = new Regex(closeFontTag);
            Match openMatch = openRe.Match(oriInnerText);
            Match closeMatch = null;
            string insertTag = null;
            string insertValue = null;
            int endIndex = 0;
            while (openMatch.Success)
            {
                endIndex = openMatch.Index + openMatch.Length;
                tagName = openMatch.Groups[1].ToString();
                closeMatch = closeRe.Match(oriInnerText,endIndex);
                if (closeMatch.Success && 
                    tagName.Equals(closeMatch.Groups[1].ToString()))
                {
                    indexForAttribute = newInnerText.Length;
                    insertTag = indexPrefix + indexForAttribute;
                    insertValue = tagName;
                    p_paraElement.SetAttribute(insertTag, insertValue);
                    newInnerText.Append(oriInnerText.Substring(endIndex, (closeMatch.Index - endIndex)));
                }
                openMatch = openMatch.NextMatch();
            }
            if (newInnerText.Length > 0)
            {
                if (!p_lastInStory)
                {
                    int lastChar = Char.ConvertToUtf32(newInnerText.ToString(), newInnerText.ToString().Length - 1);
                    if ((p_paraElement.GetAttribute(PARAGRAPH_HAS_LINEBREAK).Trim().Length == 0) &&
                         (lastChar != 8233))
                    {
                        // Append the missed paragraph's boundary char.
                        newInnerText.Append(Char.ConvertFromUtf32(8233));
                    } 
                }
               
                p_paraElement.InnerText = newInnerText.ToString();
                string length = "" + newInnerText.Length;
                p_paraElement.SetAttribute(TRANSLATED_TEXT_LENGTH, length);
            }
        }

        /// <summary>
        /// Update each paragraph's style.
        /// </summary>
        private void UpdateParagraphStyle()
        {
            InDesign.XMLElement root = (InDesign.XMLElement)m_inDesignDoc.XMLElements.FirstItem();
            InDesign.XMLElement elm = null;
            InDesign.XMLElement subElement = null;

            for (int i = 0; i<root.XMLElements.Count; i++)
            {
                if (i == 0)
                {
                    elm = (InDesign.XMLElement)root.XMLElements.FirstItem();
                }
                else
                {
                    elm = (InDesign.XMLElement)root.XMLElements.NextItem(elm);
                }

                for (int j = 0; j < elm.XMLElements.Count; j++)
                {
                    if (j == 0)
                    {
                        subElement = (InDesign.XMLElement)elm.XMLElements.FirstItem();
                    }
                    else
                    {
                        subElement = (InDesign.XMLElement)elm.XMLElements.NextItem(subElement);
                    }
                    // Update the paragraph's style marked up by this element.
                    UpdateElementStyle(subElement);
                    DeleteElementAttributes(subElement);
                }
            }
        }

        /// <summary>
        /// Get each paragraph xml element's attributes to update their style.
        /// </summary>
        private void UpdateElementStyle(InDesign.XMLElement p_element)
        {
            InDesign.Paragraph paragraph = null;
            InDesign.Font font = null;
            string fontfamily = null;
            string fontstylename = null;
            if (p_element.Paragraphs.Count > 0)
            {
                paragraph = (InDesign.Paragraph)p_element.Paragraphs.FirstItem();

                Hashtable xmlAtts = ExtractAttributes(p_element);

                string styleDifferent = (string)xmlAtts[PARAGRAPH_HAS_DIFFERENT_STYLE];

                if (styleDifferent != null &&
                    styleDifferent.Equals(PARAGRAPH_DIFFERENT_VALUE_TRUE))
                {
                    Hashtable indexToStyle = new Hashtable();

                    string indexPrefix = "index_back_";

                    ArrayList xmlAttKeys = new ArrayList(xmlAtts.Keys);
                    string xmlKey = null;
                    string indexKey = null;
                    for (int i = 0; i < xmlAttKeys.Count; i++)
                    {
                        xmlKey = (string)xmlAttKeys[i];
                        if (xmlKey.StartsWith(indexPrefix))
                        {
                            indexKey = xmlKey.Substring(indexPrefix.Length);
                            indexToStyle.Add(indexKey, xmlAtts[xmlKey]);
                        }
                    }
                    // offSetBetweenOriNew represents the offset between the ori
                    // length of a string which is not yet imported into Indesign
                    // and the length of the same string but alread been imported into
                    // Indesign.
                    int offSetBetweenOriNew = CalculateOffset(paragraph, xmlAtts);
                
                    Hashtable adjustedIndexToStyle = new Hashtable();
                    ArrayList adjustedKeys = SortAndAdjustIndex(indexToStyle,adjustedIndexToStyle,offSetBetweenOriNew);

                    UpdateCharacterFont(paragraph, adjustedKeys, adjustedIndexToStyle);

                }

                else
                {
                    fontfamily = (string)xmlAtts[FONT_FAMILY_ATTRIBUTE];
                    fontstylename = (string)xmlAtts[FONT_STYLE_ATTRIBUTE];
                    paragraph.PointSize = xmlAtts[FONT_SIZE_ATTRIBUTE]; ;
                    font = GetProperFont(fontfamily, fontstylename);
                    if (font != null)
                    {
                        paragraph.AppliedFont = font;
                    }
                }
                UpdateParaGlobalFormat(paragraph,xmlAtts);
            }
        }

        private void UpdateParaGlobalFormat(InDesign.Paragraph p_paragraph, Hashtable p_attributes)
        {
            string bulletNumberList = (string)p_attributes[PARAGRAPH_BULLET_NUMBERING_TYPE];
            string spaceBefore = (string)p_attributes[PARAGRAPH_SPACE_BEFORE];
            string spaceAfter = (string)p_attributes[PARAGRAPH_SPACE_AFTER];
            string leftIndent = (string)p_attributes[PARAGRAPH_LEFT_INDENT];
            if (leftIndent != null)
            {
                p_paragraph.LeftIndent = leftIndent; 
            }
            UpdateBulletNumberingList(p_paragraph, bulletNumberList);
            UpdateParaSpace(p_paragraph, spaceBefore, spaceAfter);
        }

        private void UpdateBulletNumberingList(InDesign.Paragraph p_paragraph,
            string p_listType)
        {
            if (p_listType != null)
            {
                InDesign.idListType idList = InDesign.idListType.idNoList;
                if (p_listType.Equals(BULLET_LIST_TYPE))
                {
                    idList = InDesign.idListType.idBulletList;
                }
                else if (p_listType.Equals(NUMBERED_LIST_TYPE))
                {
                    idList = InDesign.idListType.idNumberedList;
                }
                p_paragraph.BulletsAndNumberingListType = idList;
            }
        }

        private void UpdateParaSpace(InDesign.Paragraph p_paragraph, string p_spaceBefore,
            string p_spaceAfter)
        {
            if (p_spaceBefore != null)
            {
                p_paragraph.SpaceBefore = p_spaceBefore;
            }
            if (p_spaceAfter != null)
            {
                p_paragraph.SpaceAfter = p_spaceAfter;
            }
        }

        private int CalculateOffset(InDesign.Paragraph p_paragraph, Hashtable p_attributes)
        {
            int translatedTextLength = 0;
            string hasLinebreakValue = null;
            int offSetBetweenOriNew = 0;

            if (p_attributes.ContainsKey(TRANSLATED_TEXT_LENGTH))
            {
                translatedTextLength = Convert.ToInt32((string)p_attributes[TRANSLATED_TEXT_LENGTH]);
            }
            if (p_attributes.ContainsKey(PARAGRAPH_HAS_LINEBREAK))
            {
                hasLinebreakValue = (string)p_attributes[PARAGRAPH_HAS_LINEBREAK];
            }

            if (p_paragraph.Characters.Count > translatedTextLength)
            {
                offSetBetweenOriNew = p_paragraph.Characters.Count - translatedTextLength;

                if (!p_paragraph.Contents.ToString().EndsWith("\r"))
                {
                    offSetBetweenOriNew = offSetBetweenOriNew - 1;
                    // If there are some addtional white spaces in the last of this
                    // paragraph after paragraph end char. We shoud adjust the index.
                    string contents = p_paragraph.Contents.ToString();
                    int spaceCountAfterParaEndChar = 0;
                    for (int i = contents.Length - 1; i > 0; i--)
                    {
                        if (Char.IsWhiteSpace(contents[i]))
                        {
                            spaceCountAfterParaEndChar++;
                        }
                        else
                        {
                            break;
                        }
                    }

                    if (offSetBetweenOriNew > spaceCountAfterParaEndChar)
                    {
                        offSetBetweenOriNew = offSetBetweenOriNew - spaceCountAfterParaEndChar;
                    }
                }
                else if (hasLinebreakValue != null &&
                    hasLinebreakValue.Equals(PARAGRAPH_LINEBREAK_VALUE_FALSE))
                {
                    offSetBetweenOriNew = offSetBetweenOriNew - 2;
                }
            }

            return offSetBetweenOriNew;
        }

        private void UpdateCharacterFont(InDesign.Paragraph p_paragraph, ArrayList p_sortedIndexKeys, Hashtable p_indexFontMap)
        {
            InDesign.Characters characters = p_paragraph.Characters;
            InDesign.Character currentChar = null;
            InDesign.Font font = null;
            string fontFamily = null;
            int indexInKeys = 0;
            int styleEnd = 0;
            string styleInfo = null;
            char[] splitwith = new char[] { '-' };
            string[] styles = null;
            string pointSize = null;

            for (int i = 0; i < characters.Count; i++)
            {

                if (i == 0)
                {
                    currentChar = (InDesign.Character)characters.FirstItem();
                }
                else
                {
                    currentChar = (InDesign.Character)characters.NextItem(currentChar);
                }

                if (p_sortedIndexKeys.Contains(i))
                {
                    indexInKeys = p_sortedIndexKeys.IndexOf(i);
                    if (indexInKeys != (p_sortedIndexKeys.Count - 1))
                    {
                        styleEnd = (int)p_sortedIndexKeys[indexInKeys + 1];
                    }
                    else
                    {
                        styleEnd = characters.Count;
                    }

                    styleInfo = (string)p_indexFontMap[i];
                    styles = styleInfo.Split(splitwith);
                    if (styles.Length > 3)
                    {
                        //FontFamily string contains "-"
                        fontFamily = "";
                        for (int k = 2; k < styles.Length; k++)
                        {
                            fontFamily = fontFamily + styles[k];
                            if (k != styles.Length - 1)
                            {
                                fontFamily = fontFamily + "-";
                            }
                        }
                    }
                    else
                    { 
                        fontFamily = styles[2];
                    }
                    font = GetProperFont(fontFamily, styles[0]);
                    pointSize = styles[1];
                    int j = i;
                    for (; j < styleEnd && j < characters.Count; j++)
                    {

                        if (font != null)
                        {
                            currentChar.AppliedFont = font;
                        }
                        currentChar.PointSize = pointSize;

                        if (j == characters.Count - 1 ||
                            j == styleEnd - 1)
                        {
                            break;
                        }
                        else
                        {
                            currentChar = (InDesign.Character)characters.NextItem(currentChar);
                        }

                    }
                    i = j;
                }
            }
        }

        private Hashtable ExtractAttributes(InDesign.XMLElement p_element)
        {
            InDesign.XMLAttribute xmlAttr = null;
            Hashtable xmlAttributes = new Hashtable();
            for (int i = 0; i < p_element.XMLAttributes.Count; i++)
            {
                if (i == 0)
                {
                    xmlAttr = (InDesign.XMLAttribute)p_element.XMLAttributes.FirstItem();
                }
                else
                {
                    xmlAttr = (InDesign.XMLAttribute)p_element.XMLAttributes.NextItem(xmlAttr);
                }
                xmlAttributes.Add(xmlAttr.Name, xmlAttr.Value);
            }
            return xmlAttributes;
        }

        private void DeleteElementAttributes(InDesign.XMLElement p_element)
        {
            InDesign.XMLAttribute xmlAttr = null;
            ArrayList atts = new ArrayList();
            for (int i = 0; i < p_element.XMLAttributes.Count; i++)
            {
                if (i == 0)
                {
                    xmlAttr = (InDesign.XMLAttribute)p_element.XMLAttributes.FirstItem();
                }
                else
                {
                    xmlAttr = (InDesign.XMLAttribute)p_element.XMLAttributes.NextItem(xmlAttr);
                }
                atts.Add(xmlAttr);
            }
            for (int j = 0; j < atts.Count; j++)
            {
                ((InDesign.XMLAttribute)atts[j]).Delete();
            }
        }

        /// <summary>
        /// When imports a xml into InDesign, sometimes the length
        /// of a paragraph's characters may not equal the length
        /// of the original string imported into this paragraph.
        /// we should adjust the font information indices which represent boudary
        /// of different character font styles.
        /// </summary>
        /// <param name="p_oriIndexToStyle"></param>
        /// <param name="p_newIndexToStyle"></param>
        /// <param name="p_offSet"></param>
        /// <returns></returns>
        private ArrayList SortAndAdjustIndex(Hashtable p_oriIndexToStyle, 
            Hashtable p_newIndexToStyle, int p_offSet)
        {
            ArrayList oriKeys = new ArrayList();
            ArrayList newKeys = new ArrayList();            
            string oriKey = null;

            if (p_newIndexToStyle == null)
            {
                p_newIndexToStyle = new Hashtable();
            }

            ArrayList temp = new ArrayList(p_oriIndexToStyle.Keys);
            for (int i = 0; i < temp.Count; i++)
            {
                oriKeys.Add(Convert.ToInt32(temp[i]));
            }
            oriKeys.Sort();

            for (int i = 0; i < oriKeys.Count; i++)
            {
                oriKey = "" + oriKeys[i];
                if (i == 0)
                {
                    newKeys.Add(oriKeys[i]);
                    p_newIndexToStyle.Add(oriKeys[i], p_oriIndexToStyle[oriKey]);
                }
                else
                {
                    newKeys.Add((int)oriKeys[i] + p_offSet);
                    p_newIndexToStyle.Add((int)oriKeys[i] + p_offSet, p_oriIndexToStyle[oriKey]);                  
                }
            }

            return newKeys;
        }

        /// <summary>
        /// Opens the indd template file 
        /// and import the xml into the indd template file.
        /// </summary>
        public void ConvertInddToXml(string p_inddFileName, string p_xmlFileName, bool p_masterTranslated)
        {
        	// Set master layers translate switch
        	m_masterTranslated = p_masterTranslated;

            try
            {
                OpenInDesignDoc(p_inddFileName);
                ExportXMP(p_inddFileName);
                UnmarkInddFile();
                MarkupInddFile();
                ExportToXmlFile(p_xmlFileName);
            }
            catch (Exception e)
            {
                throw e;
            }
            finally
            {
                SaveDocument(p_inddFileName); 
            }  
        }

        /// <summary>
        /// Opens the indd template file 
        /// and import the xml into the indd template file, 
        /// and then convert Indd file to PDF file.
        /// </summary>
        public void ConvertInddToPDF(string p_xmlFileName, string p_inddFileName, bool p_masterTranslated)
        {
            m_masterTranslated = p_masterTranslated;
            int index = p_xmlFileName.LastIndexOf(".");
            String pdfFileName = p_xmlFileName.Substring(0, index + 1) + "pdf";
            try
            {
                OpenInDesignDoc(p_inddFileName);
                UnmarkInddFile();
                resetUnknownFontTable();
                MarkupInddFile();
                PreImport(p_xmlFileName);
                m_inDesignDoc.ImportXML(p_xmlFileName);
                UpdateParagraphStyle();
                ImportXMP(p_inddFileName);

                //convert to pdf
                ExportToPDFFile(pdfFileName);

            }
            catch (Exception e)
            {
                Logger.LogError("[Indesign]: InDesign Conversion Failed", e);
                throw e;
            }
            finally
            {
                SaveDocument(p_inddFileName);
            }
        }

        /// <summary>
        /// Exports the Indd file to PDF file.
        /// The original file should be indd.
        /// </summary>
        private void ExportToPDFFile(string p_PDFFileName)
        {
            InDesign.PDFExportPresets presets = m_inDesignApp.PDFExportPresets;
            InDesign.PDFExportPreset preset = (InDesign.PDFExportPreset)presets.FirstItem();

            // By setting "isInddappBlocked = true",
            // make sure another thread continuously check the pop up dialog
            // which will block InDesign Application when exporting a tagged indd file
            // into xml file if the indd file contain any content which can not be encoded.
            isInddappBlocked = true;
            Thread t = new Thread(new ThreadStart(CheckPopupDialog));
            t.Start();
            m_inDesignDoc.Export(InDesign.idExportFormat.idPDFType, p_PDFFileName, false, preset);
            // By setting "isInddappBlocked = false",
            // to stop the thread which continuously check the pop up dialog. 
            isInddappBlocked = false;
        }

        /// <summary>
        /// Opens the original indd file and initializes the InDesign.Document object.
        /// The original file should be indd.
        /// </summary>
        private void OpenInDesignDoc(object fileNameAsObj)
        {
            // "isVisible = true" means that the opened file will be shown in Application window.
            bool isVisible = false;
            isDocumentOpened = false;
            Thread t = new Thread(new ThreadStart(CheckOpenBlockDialog));
            t.Start();
            m_inDesignDoc = (InDesign.Document)m_inDesignApp.Open(fileNameAsObj, isVisible);
            isDocumentOpened = true;
            m_openedFileNumber++;
        }

        /// <summary>
        /// Export the file information (xmp) into a xml file
        /// </summary>
        private void ExportXMP(object fileNameAsObj)
        {
            String baseName = fileNameAsObj.ToString().Substring(0, fileNameAsObj.ToString().LastIndexOf("."));
            String xmpFile = baseName + XMP_POSTFIX;
            try
            {
                InDesign.MetadataPreference meta = m_inDesignDoc.MetadataPreferences;
                meta.Save(xmpFile);
            }
            catch (Exception e)
            {
                m_log.Log("Export xmp file " + xmpFile + " into " + fileNameAsObj
                    + " failed with Exception: \n" + e);
            }
        }

        /// <summary>
        /// Unmark up the special xml tags with the text and style of indd file .
        /// </summary>
        private void UnmarkInddFile()
        {
            InDesign.XMLElement root = (InDesign.XMLElement)m_inDesignDoc.XMLElements.FirstItem();

            InDesign.XMLElements elmts = null;
            InDesign.XMLElement elm = null;
            InDesign.XMLTag tag = null;

            tag = (InDesign.XMLTag)root.MarkupTag;

            elmts = root.XMLElements;
            for (int i = elmts.Count; i > 0; i--)
            {
          
                elm = (InDesign.XMLElement)elmts.FirstItem();
              
                IterateXmlElement(elm);
            }
        }

        /// <summary>
        /// Iterate each sub elements of current xml element to untag their contents.
        /// </summary>
        private void IterateXmlElement(InDesign.XMLElement p_element)

        {
            // If the XML note marks up the Table, it will be unmarked up directly.
            // all its sub notes will be skipped.
            if (p_element.XMLItems.Count > 0 && p_element.Tables.Count < 1)
            {
                // Get all sub xml note if existing.
                InDesign.XMLElement subElm = null;
                for (int j = p_element.XMLElements.Count; j > 0; j--)
                {
                    // The current xml element is always the first item of the xml element list
                    // because the previous one is removed from the list after it was untaged.
                    subElm = (InDesign.XMLElement)p_element.XMLElements.FirstItem();

                    // Iterate all xml notes.
                    IterateXmlElement(subElm);
                }
            }

            // The text contents which are marked up with "Inddgsfootnote" xml note,
            // should be converted into the InDesign Footnote.
            if (INDD_FOOTNOTE_TAG.Equals(((InDesign.XMLTag)p_element.MarkupTag).Name))
            {
                ConvertTextToFootnote(p_element);
            }

            p_element.Untag();
        }

        /// <summary>
        /// Convert the tagged text contents into the InDesign Footnote.
        /// </summary>
        private void ConvertTextToFootnote(InDesign.XMLElement p_element)
        {
            StringBuilder footnoteTextBuilder = new StringBuilder();
            InDesign.Text text = null;
            InDesign.Footnote footnote = null;

            // 1. Abstract all text contents from the "Inddgsfootnote" xml note.
            for (int t = 0; t < p_element.Texts.Count; t++)
            {
                if (t == 0)
                {
                    text = (InDesign.Text)p_element.Texts.FirstItem();
                }
                else
                {
                    text = (InDesign.Text)p_element.Texts.NextItem(text);
                }

                footnoteTextBuilder.Append(text.Contents.ToString());
                text.Delete();
            }

            // 2. Find the right place to insert the footnote.
            InDesign.InsertionPoint insertPoint = null;
            InDesign.Story story = p_element.ParentStory;
            int pos = p_element.StoryOffset;
            for (int i = 0; i < pos + 1; i++)
            {
                if (i == 0)
                {
                    insertPoint = (InDesign.InsertionPoint)(story.InsertionPoints.FirstItem());
                }
                else
                {
                    insertPoint = (InDesign.InsertionPoint)(story.InsertionPoints.NextItem(insertPoint));
                }
            }

            // 3. Insert an footnote.
            footnote = story.Footnotes.Add(InDesign.idLocationOptions.idAfter, insertPoint);
            InDesign.InsertionPoint footInsert = (InDesign.InsertionPoint)footnote.InsertionPoints.LastItem();
            InDesign.Text addedTxt = (InDesign.Text)footInsert.Texts.FirstItem();
            addedTxt.Contents = footnoteTextBuilder.ToString();
        }

        /// <summary>
        /// Create XML tags in the indd file,
        /// and mark up the text and style of indd file with special xml tags.
        /// </summary>
        private void MarkupInddFile()
        {
            m_log.Log("m_masterTranslated: " + m_masterTranslated);
            // Get root element of the document.
            InDesign.XMLElements elements = (InDesign.XMLElements)m_inDesignDoc.XMLElements;
            InDesign.XMLElement rootElm = (InDesign.XMLElement)elements.FirstItem();

            // Get ids of all stories who locates at MasterSpread.
            ArrayList masterStoryList = new ArrayList();
            foreach (InDesign.MasterSpread master in m_inDesignDoc.MasterSpreads)
            {
                foreach (InDesign.TextFrame t in master.TextFrames)
                {
                    masterStoryList.Add(t.ParentStory.Id);
                }
            }

            // Get all stories in current document.
            InDesign.Stories stories = m_inDesignDoc.Stories;
            int storyCount = stories.Count;
            InDesign.Story story = null;

            for (int i = 0; i < storyCount; i++)
            {
                if (i == 0)
                {
                    story = (InDesign.Story)stories.FirstItem();
                }
                else
                {
                    story = (InDesign.Story)stories.NextItem(story);
                }
                bool isMasterStory = false;
                for (int j = 0; j < masterStoryList.Count; j++)
                {
                    if (masterStoryList.Contains(story.Id))
                    {
                        isMasterStory = true;
                        break;
                    }
                }
				if (!m_masterTranslated && isMasterStory)
				{
					continue;
				} 
				MarkupInddStory(rootElm, story, INDD_STORY_TAG);
            }
        }

        /// <summary>
        /// Mark up the story and style of indd file with special xml tags.
        /// </summary>
        private void MarkupInddStory(InDesign.XMLElement p_parentElm, InDesign.Story p_story, string p_storyTagName)
        {
            ArrayList paraList = new ArrayList();
            InDesign.Paragraph paragraph = null;
            InDesign.XMLElement xmlElement = null;
            InDesign.XMLElements subElements = null;

            subElements = p_parentElm.XMLElements;
            xmlElement = subElements.Add(p_storyTagName, INDD_XMLCONTENT);
            p_story.Markup(xmlElement);

            // Get all paragraphes.
            for (int i=0; i<p_story.Paragraphs.Count; i ++)
            {
                if (i == 0)
                {
                    paragraph = (InDesign.Paragraph)p_story.Paragraphs.FirstItem();
                }
                else
                {
                    paragraph = (InDesign.Paragraph)p_story.Paragraphs.NextItem(paragraph);
                }

                paraList.Add(paragraph);

            }

            // Mark up each paragraph.
            foreach(InDesign.Paragraph eachparagraph in paraList)
            {
                if (eachparagraph.Contents.ToString().Trim().Length > 0)
                {
                    MarkupInddParagraph(xmlElement, eachparagraph);
                }
            }
        }

        /// <summary>
        /// Mark up the paragraph and style of indd file with special xml tags.
        /// </summary>
        private void MarkupInddParagraph(InDesign.XMLElement p_parentElm, InDesign.Paragraph p_paragraph)
        {
            InDesign.Table table = null;
            InDesign.Footnote footnote = null;

            // Add a new xml element which will be used to mark up a paragraph.
            InDesign.XMLElements subElements = p_parentElm.XMLElements;
            InDesign.XMLElement paraElement = subElements.Add(INDD_PARAGRAPH_TAG, INDD_XMLCONTENT);

            DetermineParaSpace(paraElement, p_paragraph);

            DetermineBulletAndNumberingType(paraElement, p_paragraph);

            bool isStyleSame = IsStyleSame(p_paragraph);
            bool startWithInvisibleChar = isParagraphStartWithCtrlChar(p_paragraph);

            if (!isStyleSame)
            {
                if (startWithInvisibleChar)
                {
                    paraElement.XMLAttributes.Add(FIRST_WORD_START_WITH_INVISIBLE_CHAR, START_WITH_INVISIBLE_CHAR_VALUE_TRUE);
                }
                if (!p_paragraph.Contents.ToString().EndsWith("\r"))
                {
                    // Usually a paragraph has '\r' as its end character. 
                    // But if this paragraph is the last one of a story, it is not
                    // the case. It can has not '\r' as its end character.
                    // We record this attribute for adjusting index when updating
                    // font styles.
                    paraElement.XMLAttributes.Add(PARAGRAPH_HAS_LINEBREAK,PARAGRAPH_LINEBREAK_VALUE_FALSE);
                }

                InDesign.Characters paraChars = p_paragraph.Characters;
                processParagraphCharStyles(paraElement,paraChars);

            }

            addStyleAttributes(paraElement, p_paragraph, isStyleSame);

           // Markup tables first if paragraph has tables.
           for (int t = 0; t < p_paragraph.Tables.Count; t++)
           {
               if (t == 0)
               {
                   table = (InDesign.Table)p_paragraph.Tables.FirstItem();
               }
               else
               {
                   table = (InDesign.Table)p_paragraph.Tables.NextItem(table);
               }

               MarkupInddTable(paraElement, table);
           }

           // Markup Footnote if paragraph includes Footnotes.
           for (int f = 0; f < p_paragraph.Footnotes.Count; f++)
           {
               if (f == 0)
               {
                   footnote = (InDesign.Footnote)p_paragraph.Footnotes.FirstItem();
               }
               else
               {
                   footnote = (InDesign.Footnote)p_paragraph.Footnotes.NextItem(footnote);
               }

               MarkupInddFootnote(paraElement, footnote);
           }

           p_paragraph.Markup(paraElement);
           
        }

        private void DetermineParaSpace(InDesign.XMLElement p_paraElement,
            InDesign.Paragraph p_paragraph)
        {
            string spaceBefore = p_paragraph.SpaceBefore.ToString();
            string spaceAfter = p_paragraph.SpaceAfter.ToString();
            string leftIndent = p_paragraph.LeftIndent.ToString();
            p_paraElement.XMLAttributes.Add(PARAGRAPH_SPACE_BEFORE, spaceBefore);
            p_paraElement.XMLAttributes.Add(PARAGRAPH_SPACE_AFTER, spaceAfter);
            p_paraElement.XMLAttributes.Add(PARAGRAPH_LEFT_INDENT, leftIndent);
        }

        private void DetermineBulletAndNumberingType(InDesign.XMLElement p_paraElement,
            InDesign.Paragraph p_paragraph)
        {
            InDesign.idListType bulletNumberType = p_paragraph.BulletsAndNumberingListType;
            string bulletNumberValue = null;
            switch (bulletNumberType)
            {
                case InDesign.idListType.idBulletList:
                    bulletNumberValue = BULLET_LIST_TYPE;
                    break;
                case InDesign.idListType.idNoList:
                    bulletNumberValue = NO_ID_LIST_TYPE;
                    break;
                case InDesign.idListType.idNumberedList:
                    bulletNumberValue = NUMBERED_LIST_TYPE;
                    break;
            }
            if (bulletNumberValue != null)
            {
                p_paraElement.XMLAttributes.Add(PARAGRAPH_BULLET_NUMBERING_TYPE, bulletNumberValue);
            }
        }

        private void processParagraphCharStyles(InDesign.XMLElement p_paraElement, 
            InDesign.Characters p_paraChars)
        {
            InDesign.Character currentChar = null;
            InDesign.Character nextChar = null;
            InDesign.Font fontA = null;
            InDesign.Font fontB = null;
            for (int i = 0; i < p_paraChars.Count; i++)
            {
                if (i == 0)
                {
                    currentChar = (InDesign.Character)p_paraChars.FirstItem();
                }
                else
                {
                    currentChar = (InDesign.Character)p_paraChars.NextItem(currentChar);
                }

                addStyleAttributes(p_paraElement, currentChar, i);
                // Thanks to Indesign's bug (sometimes a font is not available in
                // InDesign Application). 
                // if we found a font not available in Indesign, we record it here for
                // updating font styles after import xml into inDesign.
                addIndesignUnknownFont(currentChar);

                fontA = (InDesign.Font)currentChar.AppliedFont;
                for (int j = i; j < p_paraChars.Count; j++)
                {
                    if (j == (p_paraChars.Count - 1))
                    {
                        i = j;
                        break;
                    }
                    else
                    {
                        nextChar = (InDesign.Character)p_paraChars.NextItem(currentChar);
                        fontB = (InDesign.Font)nextChar.AppliedFont;
                        if (!(currentChar.FontStyle.Equals(nextChar.FontStyle)
                                && fontA.FontFamily.Equals(fontB.FontFamily)
                                && currentChar.PointSize.Equals(nextChar.PointSize)))
                        {
                            i = j;
                            break;
                        }
                        else
                        {
                            currentChar = nextChar;
                        }
                    }
                }
            }
        }

        private void addIndesignUnknownFont(InDesign.Character p_char)
        {
            InDesign.Font font = (InDesign.Font)p_char.AppliedFont;
            string fontFamily = font.FontFamily;
            string fontStyle = p_char.FontStyle;
            string fontKey = GenerateFontKey(fontFamily,fontStyle);
            if (inDesignUnknownFontTable == null)
            {
                inDesignUnknownFontTable = new Hashtable();
            }
            if (fontTable != null && !fontTable.ContainsKey(fontKey))
            {
                if (!inDesignUnknownFontTable.ContainsKey(fontKey))
                {
                    inDesignUnknownFontTable.Add(fontKey, font);
                }                
            }
        }

        private String makeInlineTag(string p_content, bool p_start)
        {
            StringBuilder inlineTag = new StringBuilder();
            inlineTag.Append("[");
            if (!p_start)
            {
                inlineTag.Append("/");
            }
            inlineTag.Append(p_content);          
            inlineTag.Append("]");
            return inlineTag.ToString();
        }

        private String makeStyleValue(InDesign.Character p_char)
        {
            StringBuilder inlineTag = new StringBuilder();
            inlineTag.Append(p_char.FontStyle);
            inlineTag.Append("-");
            inlineTag.Append(p_char.PointSize.ToString());
            inlineTag.Append("-");
            inlineTag.Append(((InDesign.Font)p_char.AppliedFont).FontFamily);
            return inlineTag.ToString();
        }
        

        private void addStyleAttributes(InDesign.XMLElement p_element, InDesign.Word p_word)
        {
             p_element.XMLAttributes.Add(FONT_FAMILY_ATTRIBUTE, ((InDesign.Font)p_word.AppliedFont).FontFamily);
             p_element.XMLAttributes.Add(FONT_STYLE_ATTRIBUTE, p_word.FontStyle);
             p_element.XMLAttributes.Add(FONT_SIZE_ATTRIBUTE, p_word.PointSize.ToString ());
                
        }

        private void addStyleAttributes(InDesign.XMLElement p_element, InDesign.Character p_char, int p_index)
        {
            string name = "index_" + p_index;
            string value = makeStyleValue(p_char);
            p_element.XMLAttributes.Add(name, value);
        }

        private void addStyleAttributes(InDesign.XMLElement p_element, InDesign.Paragraph p_paragraph, bool p_isStyleSame)
        {
            if (!p_isStyleSame)
            {
                p_element.XMLAttributes.Add(PARAGRAPH_HAS_DIFFERENT_STYLE, PARAGRAPH_DIFFERENT_VALUE_TRUE);
            }
            else
            {
                p_element.XMLAttributes.Add(PARAGRAPH_HAS_DIFFERENT_STYLE, PARAGRAPH_DIFFERENT_VALUE_FALSE);
                p_element.XMLAttributes.Add(FONT_FAMILY_ATTRIBUTE, ((InDesign.Font)p_paragraph.AppliedFont).FontFamily);
                p_element.XMLAttributes.Add(FONT_STYLE_ATTRIBUTE, p_paragraph.FontStyle);
                p_element.XMLAttributes.Add(FONT_SIZE_ATTRIBUTE, p_paragraph.PointSize.ToString());
            }          
            
        }

        private bool IsStyleSame(InDesign.Paragraph p_paragraph)
        {
            bool value = true;
            InDesign.Characters chars = p_paragraph.Characters;
            InDesign.Character c = null;
            InDesign.Character first = null;
            for (int i = 0; i < chars.Count; i++)
            {
                if (i == 0)
                {
                    c = (InDesign.Character)chars.FirstItem();
                    if (Char.IsWhiteSpace(c.Contents.ToString(), 0))
                    {
                        first = c;
                    }
                    else
                    {
                        break;
                    }
                }
                else
                {
                    c = (InDesign.Character)chars.NextItem(c);
                    if (!IsStyleSame(first, c))
                    {
                        value = false;
                        break;
                    }
                    if (!Char.IsWhiteSpace(c.Contents.ToString(), 0))
                    { 
                        break;
                    }
                }

            }
            if (value)
            {
                InDesign.Words words = p_paragraph.Words;
                value = IsStyleSame(words);
            }

            return value;
        }

        private bool IsStyleSame(InDesign.Words p_words)
        {
            bool value = true;
            if (p_words.Count > 1)
            {
                InDesign.Word word = null;
                InDesign.Word firstWord = null;
                InDesign.Font fontA = null;
                InDesign.Font fontB = null;
                for (int i = 0; i < p_words.Count; i++)
                {
                    if (i == 0)
                    {
                        word = (InDesign.Word)p_words.FirstItem();
                        firstWord = word;
                        fontA = (InDesign.Font)firstWord.AppliedFont;
                    }
                    else
                    {
                        word = (InDesign.Word)p_words.NextItem(word);
                        fontB = (InDesign.Font)word.AppliedFont;
                        if (!(firstWord.FontStyle.Equals(word.FontStyle)
                            && fontA.FontFamily.Equals(fontB.FontFamily)
                            && firstWord.PointSize.Equals(word.PointSize)))
                        {
                            value = false;
                            break;
                        }
                    }
                }
            }
            return value;
        }

        private bool IsStyleSame(InDesign.Word p_wordA, InDesign.Word p_wordB)
        {
            bool value = false;
            InDesign.Font fontA = (InDesign.Font)p_wordA.AppliedFont;
            InDesign.Font fontB = (InDesign.Font)p_wordB.AppliedFont;
            if(p_wordA.FontStyle.Equals(p_wordB.FontStyle)
                && fontA.FontFamily.Equals(fontB.FontFamily)
                && p_wordA.PointSize.Equals(p_wordB.PointSize))
            {
                value = true;
            }
            return value;
        }

        private bool IsStyleSame(InDesign.Character p_charA, InDesign.Character p_charB)
        {
            bool value = false;
            InDesign.Font fontA = (InDesign.Font)p_charA.AppliedFont;
            InDesign.Font fontB = (InDesign.Font)p_charB.AppliedFont;
            if (p_charA.FontStyle.Equals(p_charB.FontStyle)
                && fontA.FontFamily.Equals(fontB.FontFamily)
                && p_charA.PointSize.Equals(p_charB.PointSize))
            {
                value = true;
            }
            return value;
        }

        private bool isWordPreattachedByCtrlChar(InDesign.Word p_word)
        {
            bool value = false;
            if (p_word != null)
            { 
                // So far, we just found '\a' is a Ctrl char in InDesign.
                // If any others be found in future, we should record it here,
                // and change this method.
                value = p_word.Contents.ToString().StartsWith("\a");
            }
            return value;
        }

        private bool isParagraphStartWithCtrlChar(InDesign.Paragraph p_paragraph)
        {
            bool value = false;
            InDesign.Words words = p_paragraph.Words;
            InDesign.Word firstWord = null;
            // We just to check the first three words if there be more than three.
            for (int i = 0; (i < 3) && (i < words.Count); i++)
            {
                if (i == 0)
                {
                    firstWord = (InDesign.Word)words.FirstItem();
                }
                else
                {
                    firstWord = (InDesign.Word)words.NextItem(firstWord);
                }
                if (isWordPreattachedByCtrlChar(firstWord))
                {
                    value = true;
                    break;
                }
            }
              
            return value;
        }

        /// <summary>
        /// Mark up the table and style of indd file with special xml tags.
        /// </summary>
        private void MarkupInddTable(InDesign.XMLElement p_parentElm, InDesign.Table p_table)
        {
            // Table in indd file will be markup automatically with "Table" and "Cell" xml notes,
            // here "Inddgstable" will not be used to markup the Table in indd file.
            p_parentElm.XMLElements.Add(INDD_TABLE_TAG, p_table);
        }

        /// <summary>
        /// Mark up the Footnote and style of indd file with special xml tags.
        /// </summary>
        private void MarkupInddFootnote(InDesign.XMLElement p_parentElm, InDesign.Footnote p_footnote)
        {

            InDesign.Text tempText = null;
            InDesign.XMLElement elm = null;
            InDesign.InsertionPoint insertpoint = null;

            // Get the InsertionPoint where the Footnote locates in its story,
            // to insert the text contents converted from the Footnote.
            InDesign.Story story = p_parentElm.ParentStory;
            int pos = p_footnote.StoryOffset;
            for (int i = 0; i < pos + 1; i++)
            {
                if (i == 0)
                {
                    insertpoint = (InDesign.InsertionPoint)story.InsertionPoints.FirstItem();
                }
                else
                {
                    insertpoint = (InDesign.InsertionPoint)story.InsertionPoints.NextItem(insertpoint);
                }
            }

            // Insert the text contents into the special InsertionPoint.
            tempText = (InDesign.Text)insertpoint.Texts.FirstItem();
            tempText.Contents = p_footnote.Contents.ToString();

            // Markup the text contents converted from Footnote with tag name: Inddgsfootnote
            elm = p_parentElm.XMLElements.Add(INDD_FOOTNOTE_TAG, INDD_XMLCONTENT);
            tempText.Markup(elm);

            // Delete the Footnote because the XML tag doesn't recognise the Footnote.
            p_footnote.Delete();            
        }

        /// <summary>
        /// Opens the original indd file and initializes the InDesign.Document object.
        /// The original file should be indd.
        /// </summary>
        private void ExportToXmlFile(string p_xmlFileName)
        {
            InDesign.PDFExportPresets presets = m_inDesignApp.PDFExportPresets;
            InDesign.PDFExportPreset preset = (InDesign.PDFExportPreset)presets.FirstItem();

            // By setting "isInddappBlocked = true",
            // make sure another thread continuously check the pop up dialog
            // which will block InDesign Application when exporting a tagged indd file
            // into xml file if the indd file contain any content which can not be encoded.
            isInddappBlocked = true;
            Thread t = new Thread(new ThreadStart(CheckPopupDialog));
            t.Start();

            m_inDesignDoc.Export(XML, p_xmlFileName, false, preset);

            PostExport(p_xmlFileName);
            //Convert Indd file to PDF file
            int index = p_xmlFileName.LastIndexOf(".");
            String pdfFileName = p_xmlFileName.Substring(0, index + 1) + "pdf";
            m_inDesignDoc.Export(InDesign.idExportFormat.idPDFType, pdfFileName, false, preset);

            // By setting "isInddappBlocked = false",
            // to stop the thread which continuously check the pop up dialog. 
            isInddappBlocked = false;
        }

        private void PostExport(string p_xmlFileName)
        {
            XmlDocument xmlDoc = new XmlDocument();
            xmlDoc.Load(p_xmlFileName);
            XmlNodeList storyList = xmlDoc.SelectSingleNode("Root").ChildNodes;
            bool shouldSaveAsNewFile = false;

            foreach (XmlNode story in storyList)
            {
                if (story.NodeType != XmlNodeType.Element)
                {
                    continue;
                }
                XmlElement storyElement = (XmlElement)story;
                XmlNodeList paraList = storyElement.ChildNodes;
                foreach (XmlNode para in paraList)
                {
                    if (para.NodeType != XmlNodeType.Element)
                    {
                        continue;
                    }
                    XmlElement paraElement = (XmlElement)para;

                    if (paraElement.ChildNodes.Count > 1)
                    {
                        continue;
                    }

                    if (paraElement.ChildNodes.Count == 1 &&
                        paraElement.FirstChild.NodeType != XmlNodeType.Text)
                    {
                        continue;
                    }

                    if (paraElement.GetAttribute(PARAGRAPH_HAS_DIFFERENT_STYLE).Equals(PARAGRAPH_DIFFERENT_VALUE_TRUE))
                    {
                        shouldSaveAsNewFile = true;
                        string value = paraElement.GetAttribute(FIRST_WORD_START_WITH_INVISIBLE_CHAR);
                        bool startWithInvisibleChar = false;
                        if (value.Equals(START_WITH_INVISIBLE_CHAR_VALUE_TRUE))
                        {
                            startWithInvisibleChar = true;
                        }
                        string oriInnerText = paraElement.InnerText;
                        StringBuilder newInnerText = new StringBuilder();
                        ArrayList indexList = new ArrayList();
                        string indexAttribute = null;
                        for (int i = 0; i < oriInnerText.Length; i++)
                        {
                            indexAttribute = "index_" + i;
                            if (paraElement.GetAttribute(indexAttribute).Trim().Length > 0)
                            {
                                indexList.Add(i);
                            }
                        }

                        for (int i = 0; i < indexList.Count; i++)
                        {
                            // If a paragraph's first word starts with Ctrl char,
                            // we shoud adjust all the break index by 1 except index 0;
                            indexAttribute = "index_" + indexList[i];
                            newInnerText.Append(makeInlineTag(paraElement.GetAttribute(indexAttribute), true));
                            int startIndex = (int)indexList[i];
                            if (i == (indexList.Count - 1))
                            {
                                if (startWithInvisibleChar && (indexList.Count > 1))
                                {
                                    startIndex = startIndex - 1;
                                }

                                newInnerText.Append(oriInnerText.Substring(startIndex));
                            }
                            else
                            {
                                if ((i != 0) && startWithInvisibleChar)
                                {
                                    startIndex = startIndex - 1;
                                }
                                int length = (int)indexList[i + 1] - startIndex;
                                if (startWithInvisibleChar)
                                {
                                    length = length - 1;
                                }
                                newInnerText.Append(oriInnerText.Substring(startIndex, length));

                            }

                            newInnerText.Append(makeInlineTag(paraElement.GetAttribute(indexAttribute), false));
                            // Delete the index attribute
                            paraElement.RemoveAttribute(indexAttribute);
                        }
                        paraElement.InnerText = newInnerText.ToString();
                    }
                }
            }
            if (shouldSaveAsNewFile)
            {
                xmlDoc.Save(p_xmlFileName);
            }
        }
        /// <summary>
        /// Check the pop up dialog of InDesign Application when exporting,
        /// and close it automactically to make sure the process of exporting
        /// will not be blocked.
        /// </summary>
        private void CheckPopupDialog()
        {
            while (isInddappBlocked)
            {
                IntPtr hwnd = Win32Pinvoker.FindWindow(
                    INDD_POPUP_DIALOG_CLASSNAME, 
                    INDD_POPUP_DIALOG_TITLE);

                if (hwnd == IntPtr.Zero)
                {
                    // sleep a short time because the procedure of exporting 
                    // maybe need a while. 
                    Thread.Sleep(100);
                }
                else
                {
                    Win32Pinvoker.ClosePopupDialog(hwnd);
                }
            }
        }

        private static void CheckOpenBlockDialog()
        {
            while (!isDocumentOpened)
            {
                IntPtr hwnd1 = Win32Pinvoker.FindWindow(null,
                   "Missing Plug-ins");
                IntPtr hwnd2 = Win32Pinvoker.FindWindow(null,
                    "Cannot Open File");
                if (hwnd1 == IntPtr.Zero && hwnd2 == IntPtr.Zero)
                {
                    Thread.Sleep(500);
                }
                else
                {
                    IntPtr h1 = Win32Pinvoker.FindWindowEx(hwnd1, IntPtr.Zero, null, "OK");
                    IntPtr h2 = Win32Pinvoker.FindWindowEx(hwnd2, IntPtr.Zero, null, "OK");
                    if (h1 != IntPtr.Zero)
                    {
                        Win32Pinvoker.ClickButtonAndClose(h1);
                    }
                    else if (h2 != IntPtr.Zero)
                    {
                        Win32Pinvoker.ClickButtonAndClose(h2);
                    }
                }
            }
        }

        /// <summary>
        /// Saves the document out with the appropriate new filename and format.
        /// Also does any special handling such as accepting/rejecting changes
        /// before saving.
        /// </summary>
        private void SaveDocument(string p_inddFileName)
        {
            if (p_inddFileName.EndsWith(".inx"))
            {
                InDesign.PDFExportPresets presets = m_inDesignApp.PDFExportPresets;
                InDesign.PDFExportPreset preset = (InDesign.PDFExportPreset)presets.FirstItem();
                m_inDesignDoc.Export("InDesign Interchange", p_inddFileName, false, preset);
            }
            m_inDesignDoc.Close(InDesign.idSaveOptions.idYes, p_inddFileName);
            m_openedFileNumber--;
        }

        /// <summary>
        /// Import the file information (xmp) into indd file
        /// </summary>
        private void ImportXMP(object fileNameAsObj)
        {
            String baseName = fileNameAsObj.ToString().Substring(0, fileNameAsObj.ToString().LastIndexOf("."));
            String xmpFile = baseName + XMP_POSTFIX;
            try
            {
                bool isAll = false;
                InDesign.MetadataPreference meta = m_inDesignDoc.MetadataPreferences;
                meta.Replace(xmpFile, isAll);
            }
            catch (Exception e)
            {
                m_log.Log("Import xmp file " + xmpFile + " into " 
                    + fileNameAsObj + " failed with Exception: \n" + e);
            }
        }
    }
}
