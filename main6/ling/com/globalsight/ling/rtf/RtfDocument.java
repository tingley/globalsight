/**
 *  Copyright 2009 Welocalize, Inc. 
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */
//===============================================================
// package : com.tetrasix.majix.rtf
// class : com.tetrasix.majix.RtfDocument
//===============================================================
// The contents of this file are subject to the Mozilla Public License
// Version 1.1 (the "License"); you may not use this file except in
// compliance with the License. You may obtain a copy of the License at
// http://www.mozilla.org/MPL/
//
// Software distributed under the License is distributed on an "AS IS"
// basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
// License for the specific language governing rights and limitations
// under the License.
//
// The Original Code is TetraSys code.
//
// The Initial Developer of the Original Code is TetraSys..
// Portions created by TetraSys are
// Copyright (C) 1998-2000 TetraSys. All Rights Reserved.
//
// Contributor(s):
//===============================================================
package com.globalsight.ling.rtf;

import java.io.*;

public class RtfDocument
    extends RtfCompoundObject
    implements Version
{
    RtfFontTable m_fonttable = null;
    RtfStyleSheet m_stylesheet = null;
    RtfColorTable m_colortable = null;
    RtfExternalEntities m_entities = null;
    RtfVariables m_variables = null;
    RtfInfo m_info = null;
    String m_filename;

    int m_ansiCodePage = 1252;                    // document codepage \ansicpg
    int m_defaultLanguage = 1033;                 // default language \deflang
    int m_defaultFeLanguage = 1033;               // default FE language
    int m_defaultColor = 0;                       // default color = 0
    int m_defaultBgColor = 0;                     // default bg color = 0
    int m_defaultFont = 0;                        // default font \deff
    int m_defaultFontSize = 24;                   // default fontsize
    int m_skipCount = 1;                          // default skip count \\ucN
    boolean m_skipCountSet = false;               // could be a TriState obj

    public RtfDocument(String p_filename)
    {
        m_filename = p_filename;
    }

    public void setCodepage(int p_codepage)
    {
        m_ansiCodePage = p_codepage;
    }

    public int getCodepage()
    {
        return m_ansiCodePage;
    }

    public void setFontTable(RtfFontTable p_fonts)
    {
        m_fonttable = p_fonts;
    }

    public RtfFontTable getFontTable()
    {
        return m_fonttable;
    }

    public void setStyleSheet(RtfStyleSheet p_styles)
    {
        m_stylesheet = p_styles;
    }

    public RtfStyleSheet getStyleSheet()
    {
        return m_stylesheet;
    }

    public void setColorTable(RtfColorTable p_colors)
    {
        m_colortable = p_colors;
    }

    public RtfColorTable getColorTable()
    {
        return m_colortable;
    }

    public void setExternalEntities(RtfExternalEntities p_entities)
    {
        m_entities = p_entities;
    }

    public RtfExternalEntities getExternalEntities()
    {
        return m_entities;
    }

    public void setVariables(RtfVariables p_variables)
    {
        m_variables = p_variables;
    }

    public RtfVariables getVariables()
    {
        return m_variables;
    }

    /**
     * Can be called multiple times by the analyser but must set
     * document default only once.
     */
    public void setSkipCount(int count)
    {
        if (! m_skipCountSet)
        {
            m_skipCount = count;
            m_skipCountSet = true;
        }
    }

    public int getSkipCount()
    {
        return m_skipCount;
    }

    public void setLang(int lang)
    {
        m_defaultLanguage = lang;
    }

    public int getLang()
    {
        return m_defaultLanguage;
    }

    public void setFeLang(int lang)
    {
        m_defaultFeLanguage = lang;
    }

    public int getFeLang()
    {
        return m_defaultFeLanguage;
    }

    public int getColor()
    {
        return m_defaultColor;
    }

    public int getBgColor()
    {
        return m_defaultBgColor;
    }

    public void setFont(int font)
    {
        m_defaultFont = font;
    }

    public int getFont()
    {
        return m_defaultFont;
    }

    public void setFontSize(int fontSize)
    {
        m_defaultFontSize = fontSize;
    }

    public int getFontSize()
    {
        return m_defaultFontSize;
    }

    public void setInfo(RtfInfo p_info)
    {
        m_info = p_info;
    }

    public RtfInfo getInfo()
    {
        return m_info;
    }

    public String getFilePath()
    {
        return m_filename;
    }

    public String getFileName()
    {
        String filename = m_filename;

        int pos = filename.lastIndexOf(File.separatorChar);
        if (pos >= 0)
        {
            return filename.substring(pos + 1);
        }
        else
        {
            return filename;
        }
    }

    public void Dump(PrintWriter out)
    {
        out.println("<rtfdoc lang=\"" + m_defaultLanguage + "\">");

        m_fonttable.Dump(out);
        m_stylesheet.Dump(out);

        if (m_colortable != null)
        {
            m_colortable.Dump(out);
        }

        m_entities.Dump(out);

        if (m_info != null)
        {
            m_info.Dump(out);
        }

        super.Dump(out);

        out.println("</rtfdoc>");
    }

    public void toText(PrintWriter out)
    {
        super.toText(out);
    }

    /*
    public void generate(XmlGenerator gen, XmlWriter out,
      XmlGeneratorContext context)
    {
        gen.rtfgenerate(this, out, context);
    }
    */
}

