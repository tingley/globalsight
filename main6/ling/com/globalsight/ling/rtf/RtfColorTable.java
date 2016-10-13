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
//
// Copyright (c) 2003 GlobalSight Corporation. All rights reserved.
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
package com.globalsight.ling.rtf;


import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.Iterator;

public class RtfColorTable
{
    private ArrayList m_colors;

    public RtfColorTable()
    {
        m_colors = new ArrayList();
    }

    public void defineColor(String r, String g, String b)
    {
        int code = m_colors.size();
        m_colors.add(new ColorDefinition(code, r, g, b));
    }

    public Iterator getColorDefinitions()
    {
        return m_colors.iterator();
    }

    public void Dump(PrintWriter out)
    {
        out.println("<colortable>");

        for (int i = 0, max = m_colors.size(); i < max; i++)
        {
            ColorDefinition d = (ColorDefinition)m_colors.get(i);

            d.Dump(out);
        }

        out.println("</colortable>");
    }

    public class ColorDefinition
    {
        int _code;
        String _r, _g, _b;

        ColorDefinition(int code, String r, String g, String b)
        {
            _code = code;
            _r = r; _g = g; _b = b;
        }

        public void Dump(PrintWriter out)
        {
            if (_r == null && _g == null && _b == null)
            {
                out.print("<color code=\"");
                out.print(_code);
                out.println("\" default=\"true\" />");
            }
            else
            {
                out.print("<color code=\"");
                out.print(_code);
                out.print("\" red=\"");
                out.print(_r);
                out.print("\" green=\"");
                out.print(_g);
                out.print("\" blue=\"");
                out.print(_b);
                out.println("\" />");
            }
        }

        public String toRtf()
        {
            StringBuffer result = new StringBuffer();

            if (_r != null)
            {
                result.append("\\red");
                result.append(_r);
            }
            if (_g != null)
            {
                result.append("\\green");
                result.append(_g);
            }
            if (_b != null)
            {
                result.append("\\blue");
                result.append(_b);
            }

            result.append(";");

            return result.toString();
        }
    }
}
