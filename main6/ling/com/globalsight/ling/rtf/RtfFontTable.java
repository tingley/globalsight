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
// class : com.tetrasix.majix.RtfFontTable
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

import java.io.PrintWriter;

import java.util.Iterator;
import java.util.TreeMap;

public class RtfFontTable
{
    private TreeMap m_fonts;

    public RtfFontTable()
    {
        m_fonts = new TreeMap ();
    }

    public void defineFont(int code, String name,
        RtfFontProperties fontProperties)
    {
        m_fonts.put(new Integer(code),
            new FontDefinition(code, name, fontProperties));
    }

    public String getFontName(int code)
    {
        FontDefinition font =
            (FontDefinition)m_fonts.get(new Integer(code));

        if (font != null)
        {
            return font.getName();
        }
        else
        {
            return "@@unknownfont@@";
        }
    }

    public Iterator getFontDefinitions()
    {
        return m_fonts.values().iterator();
    }

    public RtfFontProperties getFontProperties(int code)
    {
        FontDefinition font =
            (FontDefinition)m_fonts.get(new Integer(code));

        if (font != null)
        {
            return font.getFontProperties();
        }
        else
        {
            return null;
        }
    }

    public void Dump(PrintWriter out)
    {
        Iterator fonts = m_fonts.values().iterator();

        out.println("<fonttable>");
        while (fonts.hasNext())
        {
            FontDefinition font = (FontDefinition)fonts.next();
            font.Dump(out);
        }
        out.println("</fonttable>");
    }

    /*
      public static RtfFontTable getCurrentFontTable()
      {
      return _currentFontTable;
      }
    */

    public class FontDefinition
    {
        int _code;
        String _fontName;
        RtfFontProperties _fontProperties;

        FontDefinition(int code, String name,
            RtfFontProperties fontProperties)
        {
            _code           = code;
            _fontName       = name;
            _fontProperties = fontProperties;
        }

        public int getCode()
        {
            return _code;
        }

        public String getName()
        {
            return _fontName;
        }

        public RtfFontProperties getFontProperties()
        {
            return _fontProperties;
        }

        public void Dump(PrintWriter out)
        {
            out.print("<font code=\"");
            out.print(_code);
            out.print("\" ");
            out.print(_fontProperties.toString());
            out.print(">");
            out.print(_fontName);
            out.println("</font>");
        }
    }
}
