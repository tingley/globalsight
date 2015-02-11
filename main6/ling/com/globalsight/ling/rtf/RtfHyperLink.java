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
// class : com.tetrasix.majix.RtfHyperLink
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

// {\field{\*\fldinst {HYPERLINK "http://www.imdb.com/" \\o "Tooltip text"}
// {{\*\datafield ....}}}
// {\fldrslt {\cs15\\ul\cf2 hyperlink}}}
public class RtfHyperLink
    extends RtfObject
{
    String _data;

    String _text;
    String _alt;
    String _url;
    String _refid;

    RtfHyperLink(String data, String text)
    {
        _data = RtfSymbol.escapeBackslash(data);
        _text = text;

        // Skip "HYPERLINK"
        data = data.substring(10);

        int pos = data.indexOf("\"");
        if (pos != -1)
        {
            int pos2 = data.indexOf("\"", pos + 1);
            if (pos2 != -1)
            {
                _url = data.substring(pos + 1, pos2);
            }
        }

        // Find \\l
        pos = data.indexOf("\\l");
        if (pos != -1)
        {
            pos = data.indexOf("\"", pos);
            if (pos != -1)
            {
                int pos2 = data.indexOf("\"", pos + 1);
                if (pos2 != -1)
                {
                    _refid = data.substring(pos + 1, pos2);
                }
            }
        }

        // Find \\o
        pos = data.indexOf("\\o");
        if (pos != -1)
        {
            pos = data.indexOf("\"", pos);
            if (pos != -1)
            {
                int pos2 = data.indexOf("\"", pos + 1);
                if (pos2 != -1)
                {
                    _alt = data.substring(pos + 1, pos2);
                }
            }
        }
    }

    public String getUrl()
    {
        return _url;
    }

    public String getRefid()
    {
        return _refid;
    }

    public String getAltText()
    {
        return _alt;
    }

    public String getText()
    {
        return _text;
    }

    public void Dump(PrintWriter out)
    {
        out.print("<hyperlink url='" + _url + "'");
        if (_refid != null)
        {
            out.print(" refid=\'" + _refid + "'");
        }
        if (_alt != null)
        {
            out.print(" alt=\'" + _alt + "'");
        }
        if (_text != null)
        {
            out.print(" text=\'" + _text + "'");
        }
        out.print("/>");
    }

    public void toText(PrintWriter out)
    {
        out.print(_text);
    }

    /*
    public void generate(XmlGenerator gen, XmlWriter out,
      XmlGeneratorContext context)
    {
        gen.rtfgenerate(this, out, context);
    }
    */
}

