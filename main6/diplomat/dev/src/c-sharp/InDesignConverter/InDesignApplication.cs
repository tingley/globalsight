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
        private Logger m_log = null;

        // the number of opened InDesign files at the same time
        static private int m_openedFileNumber = 0;

        // File formats to be converted.
        private const string XML = "xml";
        private const string INDD = "indd";

        // XML tag name used to mark Indd file.
        private const string INDD_STORY_TAG = "Inddgsstory";
        private const string INDD_PARAGRAPH_TAG = "Inddgsparagraph";
        private const string INDD_XMLCONTENT = "";
        private const string FONT_FAMILY_ATTRIBUTE = "InddFontFamily";
        private const string FONT_STYLE_ATTRIBUTE = "InddFontStyle";
        private const string FONT_SIZE_ATTRIBUTE = "InddFontSize";

        // Members
        private InDesign.Application m_inDesignApp = null;
        private InDesign.Document m_inDesignDoc = null;
        private Hashtable fontTable = null;

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
            // 
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
                InitializeFontTable();
                m_log.Log("Create the " + INDD_COM_STRING + " successfully.");
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
                m_log.Log("Create the new COM object " + sProgID + " failure.");
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

                string key = GenerateFontKey(font.FontFamily, font.FontStyleName);
                fontTable.Add(key, font);
            }
        }

        /// <summary>
        /// Opens the indd template file 
        /// and import the xml into the indd template file.
        /// </summary>
        public void ConvertXmlToIndd(string p_xmlFileName, string p_inddFileName)
        {
            OpenInDesignDoc(p_inddFileName);
            m_inDesignDoc.ImportXML(p_xmlFileName);
            UpdateParagraphStyle();
            UnmarkInddFile();
            SaveDocument(p_inddFileName);
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
                }
            }
        }

        /// <summary>
        /// Get each paragraph xml element's attributes to update their style.
        /// </summary>
        private void UpdateElementStyle(InDesign.XMLElement p_element)
        {
            InDesign.XMLAttribute xmlAttr = null;
            InDesign.Paragraph paragraph = null;
            InDesign.Font font = null;
            string fontfamily = null;
            string fontstylename = null;

            if (p_element.Paragraphs.Count > 0)
            {
                paragraph = (InDesign.Paragraph)p_element.Paragraphs.FirstItem();

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

                    if (FONT_FAMILY_ATTRIBUTE.Equals(xmlAttr.Name))
                    {
                        fontfamily = xmlAttr.Value;
                    }

                    if (FONT_STYLE_ATTRIBUTE.Equals(xmlAttr.Name))
                    {
                        fontstylename = xmlAttr.Value;
                    }

                    if (FONT_SIZE_ATTRIBUTE.Equals(xmlAttr.Name))
                    {
                        paragraph.PointSize = xmlAttr.Value;
                    }
                }

                font = GetProperFont(fontfamily, fontstylename);
                if (font != null)
                {
                    paragraph.AppliedFont = font;
                }
            }
        }


        /// <summary>
        /// Opens the indd template file 
        /// and import the xml into the indd template file.
        /// </summary>
        public void ConvertInddToXml(string p_inddFileName, string p_xmlFileName)
        {
            OpenInDesignDoc(p_inddFileName);
            UnmarkInddFile();
            MarkupInddFile();
            ExportToXmlFile(p_xmlFileName);
            SaveDocument(p_inddFileName);
        }

        /// <summary>
        /// Opens the original indd file and initializes the InDesign.Document object.
        /// The original file should be indd.
        /// </summary>
        private void OpenInDesignDoc(object fileNameAsObj)
        {
            // "isVisible = true" means that the opened file will be shown in Application window.
            bool isVisible = false;

            m_inDesignDoc = (InDesign.Document)m_inDesignApp.Open(fileNameAsObj, isVisible);

            m_openedFileNumber++;
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
        /// Iterate each sub elements of current xml element to untag thier contents.
        /// </summary>
        private void IterateXmlElement(InDesign.XMLElement p_element)
        {
        //    InDesign.XMLTag tag = null;

            if (p_element.XMLItems.Count > 0)
            {
                // Get all sub xml note if existing.
                InDesign.XMLElement subElm = null;
                for (int j = p_element.XMLElements.Count; j > 0; j--)
                {
                    subElm = (InDesign.XMLElement)p_element.XMLElements.FirstItem();

                    // Iterate all xml notes.
                    IterateXmlElement(subElm);
                }
            }

            p_element.Untag();
        }

        /// <summary>
        /// Create XML tags in the indd file,
        /// and mark up the text and style of indd file with special xml tags.
        /// </summary>
        private void MarkupInddFile()
        {
            // Get root element of the document.
            InDesign.XMLElements elements = (InDesign.XMLElements)m_inDesignDoc.XMLElements;
            InDesign.XMLElement rootElm = (InDesign.XMLElement)elements.FirstItem();

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

                MarkupInddStory(rootElm, story);
            }
        }

        /// <summary>
        /// Mark up the story and style of indd file with special xml tags.
        /// </summary>
        private void MarkupInddStory(InDesign.XMLElement p_parentElm, InDesign.Story p_story)
        {
            ArrayList paraList = new ArrayList();
            InDesign.Paragraph paragraph = null;
            InDesign.XMLElement xmlElement = null;
            InDesign.XMLElements subElements = null;

            subElements = p_parentElm.XMLElements;
            xmlElement = subElements.Add(INDD_STORY_TAG, INDD_XMLCONTENT);
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
            InDesign.XMLElement xmlElement = null;
            InDesign.Font font = null;

            // Add a new xml element which will be used to mark up a paragraph.
            InDesign.XMLElements subElements = p_parentElm.XMLElements;
            xmlElement = subElements.Add(INDD_PARAGRAPH_TAG, INDD_XMLCONTENT);
            
            // Get the paragraph's attributes to be translated.
            // For example
            // FontFamily:  Times New Roman
            // PointSize: 12
            font = (InDesign.Font)p_paragraph.AppliedFont;
            xmlElement.XMLAttributes.Add(FONT_FAMILY_ATTRIBUTE, font.FontFamily);
            xmlElement.XMLAttributes.Add(FONT_STYLE_ATTRIBUTE, p_paragraph.FontStyle);
            xmlElement.XMLAttributes.Add(FONT_SIZE_ATTRIBUTE, p_paragraph.PointSize.ToString());

            // Markup the paragraph with xml element 
            // so that it can be exported into xml file.
            p_paragraph.Markup(xmlElement);
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
            // By setting "isInddappBlocked = false",
            // to stop the thread which continuously check the pop up dialog. 
            isInddappBlocked = false;
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

        /// <summary>
        /// Saves the document out with the appropriate new filename and format.
        /// Also does any special handling such as accepting/rejecting changes
        /// before saving.
        /// </summary>
        private void SaveDocument(string p_inddFileName)
        {
            m_inDesignDoc.Close(InDesign.idSaveOptions.idYes, p_inddFileName);
            m_openedFileNumber--;
        }

    }
}
