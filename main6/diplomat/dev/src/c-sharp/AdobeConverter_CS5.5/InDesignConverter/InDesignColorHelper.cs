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
    public class InDesignColorHelper
    {
        public static string S_COLOR_PRE = "globalsight_color_";
        private Hashtable colorTable = null;
        private InDesign.Document m_inDesignDoc = null;
        

        public InDesignColorHelper(InDesign.Document doc)
        {
            m_inDesignDoc = doc;
            InitializeColorTable();
        }

        /// <summary>
        /// Update each paragraph's style.
        /// </summary>
        public void UpdateColors()
        {
            InDesign.XMLElement root = (InDesign.XMLElement)m_inDesignDoc.XMLElements.FirstItem();
            InDesign.XMLElement elm = null;
            InDesign.XMLElement subElement = null;

            for (int i = 0; i < root.XMLElements.Count; i++)
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
                    UpdateElementColor(subElement);
                }
            }
        }

        /// <summary>
        /// Get each paragraph xml element's attributes to update their style.
        /// </summary>
        private void UpdateElementColor(InDesign.XMLElement p_element)
        {
            InDesign.Paragraph paragraph = null;
            InDesign.XMLElement element = null;

            if (p_element.Paragraphs.Count > 0)
            {
                element = p_element;
            }
            else if (((InDesign.XMLElement) p_element.Parent).Paragraphs.Count > 0)
            {
                element = (InDesign.XMLElement) p_element.Parent;
            }

            if (element != null)
            {
                InDesign.Color color = null;

                for (int ii = 0; ii < element.Paragraphs.Count; ii++)
                {
                    if (ii == 0)
                    {
                        paragraph = (InDesign.Paragraph)element.Paragraphs.FirstItem();
                    }
                    else
                    {
                        paragraph = (InDesign.Paragraph)element.Paragraphs.NextItem(paragraph);
                    }

                    color = UpdateParagraphColor(paragraph, color);
                }
            }
        }

        private InDesign.Color UpdateParagraphColor(InDesign.Paragraph paragraph, InDesign.Color color)
        {
            int wordsCount = paragraph.Words.Count;
            InDesign.Word lastWord = null;
            ContentColor lastcc = null;
            for (int i = 0; i < wordsCount; i++)
            {
                InDesign.Word word = null;
                if (i == 0)
                {
                    word = (InDesign.Word)paragraph.Words.FirstItem();
                }
                else
                {
                    word = (InDesign.Word)paragraph.Words.NextItem(lastWord);
                }

                if (lastWord != null && lastcc != null)
                {
                    lastWord.Contents = lastcc.Content;
                    lastWord.FillColor = lastcc.Color;
                }

                string content = word.Contents.ToString();
                ContentColor cc = DetermineColor(content, color);
                color = cc.Color;
                lastWord = word;
                lastcc = cc;
            }

            if (lastWord != null && lastcc != null)
            {
                lastWord.Contents = lastcc.Content;
                lastWord.FillColor = lastcc.Color;
            }

            //CheckByChar(paragraph);

            UpdateTableStyle(paragraph);

            return color;
        }

        private void UpdateTableStyle(InDesign.Paragraph p_paragraph)
        {
            InDesign.Table table = null;
            InDesign.Cell cell = null;
            InDesign.Paragraph p = null;
            // update tables styles if paragraph has tables.
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

                // update cells one by one
                for (int i = 0; i < table.Cells.Count; i++)
                {
                    if (i == 0)
                    {
                        cell = (InDesign.Cell)table.Cells.FirstItem();
                    }
                    else
                    {
                        cell = (InDesign.Cell)table.Cells.NextItem(cell);
                    }

                    InDesign.Color color = null;
                    InDesign.Paragraphs ps = cell.Paragraphs;

                    for (int j = 0; j < ps.Count; j++)
                    {
                        if (j == 0)
                        {
                            p = (InDesign.Paragraph)ps.FirstItem();
                        }
                        else
                        {
                            p = (InDesign.Paragraph)ps.NextItem(p);
                        }

                        color = UpdateParagraphColor(p, color);
                    }
                }
            }
        }

        private void CheckByChar(InDesign.Paragraph paragraph)
        {
            int count = paragraph.Characters.Count;
            InDesign.Character cha = null;

            for (int i = 0; i < count; i++)
            {
                if (i == 0)
                {
                    cha = (InDesign.Character)paragraph.Characters.FirstItem();
                }
                else
                {
                    cha = (InDesign.Character)paragraph.Characters.NextItem(cha);
                }

                InDesign.Color color = (InDesign.Color)cha.FillColor;

                if (color != null && !color.Name.StartsWith(S_COLOR_PRE))
                {
                    InDesign.Character nextcha = null;
                    InDesign.Character precha = null;
                    try
                    {
                        nextcha = (InDesign.Character)paragraph.Characters.NextItem(cha);
                        while (nextcha != null && nextcha.FillColor != null
                        && !((InDesign.Color)nextcha.FillColor).Name.StartsWith(S_COLOR_PRE))
                        {
                            nextcha = (InDesign.Character)paragraph.Characters.NextItem(nextcha);
                        }
                    }
                    catch { }
                    try
                    {
                        precha = (InDesign.Character)paragraph.Characters.PreviousItem(cha);
                        while (precha != null && precha.FillColor != null
                        && !((InDesign.Color)precha.FillColor).Name.StartsWith(S_COLOR_PRE))
                        {
                            precha = (InDesign.Character)paragraph.Characters.PreviousItem(precha);
                        }
                    }
                    catch { }

                    if (nextcha != null && nextcha.FillColor != null
                        && ((InDesign.Color)nextcha.FillColor).Name.StartsWith(S_COLOR_PRE))
                    {
                        cha.FillColor = nextcha.FillColor;
                    }
                    else if (precha != null && precha.FillColor != null
                        && ((InDesign.Color)precha.FillColor).Name.StartsWith(S_COLOR_PRE))
                    {
                        cha.FillColor = precha.FillColor;
                    }
                }
            }
        }
        public static String RE_GS_COLOR_S = "\\(\\(GS_COLOR_START\\)\\((.+?)\\)\\)";
        public static String RE_GS_COLOR_E = "\\(\\(GS_COLOR_END\\)\\((.+?)\\)\\)";
        private static Regex regS = new Regex(RE_GS_COLOR_S);
        private static Regex regE = new Regex(RE_GS_COLOR_E);

        private ContentColor DetermineColor(string content, InDesign.Color lastColor)
        {
            InDesign.Color defaultColor = (InDesign.Color)colorTable[S_COLOR_PRE + "black"];

            /*
             ((GS_COLOR_START)(Black))This is content 1 fr. 
             ((GS_COLOR_END)(Black))((GS_COLOR_START)(Blue))This 
             is content 2 fr. 
             ((GS_COLOR_END)(Blue))((GS_COLOR_START)(Black))
             This is content 3 fr.((GS_COLOR_END)(Black)) */

            InDesign.Color color = lastColor == null ? defaultColor : lastColor;
            Match m = regS.Match(content);
            if (m != null && m.Success)
            {
                string colorHere = m.Groups[1].Value;
                color = (InDesign.Color)colorTable[S_COLOR_PRE + colorHere.ToLower()];

                if (color == null)
                {
                    color = lastColor == null ? defaultColor : lastColor;
                }
            }

            string newContent = regS.Replace(content, "");
            newContent = regE.Replace(newContent, "");

            ContentColor cc = new ContentColor();
            cc.Color = color;
            cc.Content = newContent;

            return cc;
        }

        private void InitializeColorTable()
        {
            if (colorTable == null)
            {
                colorTable = new Hashtable();
            }

            addOneColor("black", new double[4] { 0.0, 0.0, 0.0, 100.0 });

            addOneColor("white", new double[4] { 0.0, 0.0, 0.0, 0.0 });

            addOneColor("red", new double[4] { 0.0, 100.0, 100.0, 0.0 });

            addOneColor("green", new double[4] { 100.0, 0.0, 100.0, 0.0 });

            addOneColor("blue", new double[4] { 35.0, 0.0, 20.0, 0.0 });

            addOneColor("cyan", new double[4] { 100.0, 0.0, 0.0, 0.0 });

            addOneColor("magenta", new double[4] { 15.0, 100.0, 20.0, 0.0 });

            addOneColor("yellow", new double[4] { 0.0, 0.0, 100.0, 0.0 });

            addOneColor("mauve", new double[4] { 60.0, 75.0, 0.0, 0.0 });

            addOneColor("olive", new double[4] { 45.0, 40.0, 100.0, 50.0 });
        }

        private void addOneColor(string colorName, double[] colorValue)
        {
            string colorKey = S_COLOR_PRE + colorName;
            InDesign.Color color = null;
            InDesign.Color existColor = null;
            InDesign.Color oriColor = null;

            for (int i = 0; i < m_inDesignDoc.Colors.Count; i++)
            {
                if (i == 0)
                {
                    existColor = (InDesign.Color)m_inDesignDoc.Colors.FirstItem();
                }
                else
                {
                    existColor = (InDesign.Color)m_inDesignDoc.Colors.NextItem(existColor);
                }

                if (oriColor == null && existColor.Name.Length > 0)
                {
                    oriColor = existColor;
                }

                if (existColor.Name.Equals(colorKey))
                {
                    color = existColor;
                    break;
                }
            }

            if (color == null)
            {
                color = oriColor.Duplicate();
                color.Name = colorKey;
                color.ColorValue = colorValue;
            }

            colorTable.Add(colorKey, color);
        }
    }

    class ContentColor
    {
        private string c = null;
        private InDesign.Color color = null;

        public string Content
        {
            set
            {
                c = value;
            }
            get
            {
                return c;
            }
        }

        public InDesign.Color Color
        {
            set
            {
                color = value;
            }
            get
            {
                return color;
            }
        }
    }
}
