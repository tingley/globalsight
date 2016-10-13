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
// class : com.tetrasix.majix.RtfSymbol
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

// {\field{\*\fldinst SYMBOL 244 \\f "Symbol" \\s 12}{\fldrslt\f3\fs24}}
public class RtfSymbol
    extends RtfObject
{
    private String _data;
    private RtfTextProperties _properties;

    private int _code = 0;
    private String _font = null;

    RtfSymbol(String data, RtfTextProperties properties)
    {
        _data = escapeBackslash(data);
        _properties = properties;

        // Skip "SYMBOL"
        data = data.substring(7);

        int pos = data.indexOf("\\f");
        if (pos != -1)
        {
            String scode = data.substring(0, pos);
            scode = scode.trim();
            _code = Integer.parseInt(scode);

            pos = data.indexOf("\"");
            if (pos != -1)
            {
                int pos2 = data.indexOf("\"", pos + 1);
                if (pos2 != -1)
                {
                    _font = data.substring(pos + 1, pos2);
                }
            }
        }
    }

    public String getData()
    {
        return _data;
    }

    public RtfTextProperties getProperties()
    {
        return _properties;
    }

    public String getFont()
    {
        return _font;
    }

    public int getCode()
    {
        return _code;
    }

    public String toString()
    {
        return super.toString() + "[code=" + String.valueOf(_code) +
            " font='" + _font + "']";
    }

    public String toRtf()
    {
        StringBuffer result = new StringBuffer();

        result.append("{\\field{\\*\\fldinst ");
        result.append(_data);
        result.append("}{\\fldrslt ?}}");

        return result.toString();
    }

    public void Dump(PrintWriter out)
    {
        out.print("<symbol code=" + String.valueOf(_code) +
          " font='" + _font + "'>");
    }

    static public String escapeBackslash(String p_text)
    {
        StringBuffer result = new StringBuffer();

        for (int i = 0, max = p_text.length(); i < max; i++)
        {
            char ch = p_text.charAt(i);

            if (ch == '\\')
            {
                result.append(ch);
            }

            result.append(ch);
        }

        return result.toString();
    }

    /*
    public void generate(XmlGenerator gen, XmlWriter out,
      XmlGeneratorContext context)
    {
        gen.rtfgenerate(this, out, context);
    }
    */
}

