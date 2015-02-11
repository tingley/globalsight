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
package com.globalsight.ling.rtf;

import java.io.*;

public class RtfInfo
{
    static public final int TITLE    = 0;
    static public final int SUBJECT  = 1;
    static public final int AUTHOR   = 2;
    static public final int MANAGER  = 3;
    static public final int COMPANY  = 4;
    static public final int OPERATOR = 5;

    static public final int CATEGORY = 6;
    static public final int KEYWORDS = 7;
    static public final int COMMENT = 8;
    static public final int DOCCOMM = 9;
    static public final int HLINKBASE = 10;

    static public final int CREATIM = 11;
    static public final int REVTIM = 12;
    static public final int PRINTIM = 13;
    static public final int BUPTIM = 14;

    static public final int NOFPAGES = 15;        // numeric
    static public final int NOFWORDS = 16;        // numeric
    static public final int NOFCHARS = 17;        // numeric
    static public final int NOFCHARSWS = 18;      // numeric

    static public final int ID = 19;              // numeric
    static public final int EDMINS = 20;          // numeric
    static public final int VERSION = 21;         // numeric

    static public final int VERN = 22;            // numeric

    // UPDATE THIS VALUE
    static private final int MAXPROP = 22;

    private String[] _theProperties = new String[MAXPROP + 1];

    RtfInfo()
    {
    }

    void defineProperty(int code, String value)
    {
        if (code >= 0 && code <= MAXPROP)
        {
            _theProperties[code] = value;
        }
    }

    public String getProperty(int code)
    {
        if (code >= 0 && code <= MAXPROP)
        {
            return _theProperties[code];
        }

        return null;
    }

    public void toRtf(StringBuffer p_result, String p_name, int p_code)
    {
        if (p_code >= 0 && p_code <= MAXPROP && _theProperties[p_code] != null)
        {
            String value = _theProperties[p_code];

            switch (p_code)
            {
                // Numeric control words print without braces
            case NOFPAGES:
            case NOFWORDS:
            case NOFCHARS:
            case NOFCHARSWS:
            case ID:
            case EDMINS:
            case VERSION:
            case VERN:
                p_result.append("\\").append(p_name).append(value).append("\n");
                break;

                // All other control words use braces
            default:
                p_result.append("{\\").append(p_name).append(" ");
                p_result.append(value).append("}\n");
                break;
            }
        }
    }

    public String toRtf()
    {
        StringBuffer result = new StringBuffer();

        toRtf(result, "title", TITLE);
        toRtf(result, "subject", SUBJECT);
        toRtf(result, "author", AUTHOR);
        toRtf(result, "manager", MANAGER);
        toRtf(result, "company", COMPANY);
        toRtf(result, "operator", OPERATOR);

        toRtf(result, "category", CATEGORY);
        toRtf(result, "keywords", KEYWORDS);
        toRtf(result, "comment", COMMENT);
        toRtf(result, "doccomm", DOCCOMM);
        toRtf(result, "hlinkbase", HLINKBASE);

        toRtf(result, "creatim", CREATIM);
        toRtf(result, "revtim", REVTIM);
        toRtf(result, "printim", PRINTIM);
        toRtf(result, "buptim", BUPTIM);
        toRtf(result, "nofpages", NOFPAGES);
        toRtf(result, "nofwords", NOFWORDS);
        toRtf(result, "nofchars", NOFCHARS);
        toRtf(result, "nofcharsws", NOFCHARSWS);
        toRtf(result, "id", ID);
        toRtf(result, "edmins", EDMINS);
        toRtf(result, "version", VERSION);
        toRtf(result, "vern", VERN);

        return result.toString();
    }

    //
    // Debug methods
    //

    private void Dump(PrintWriter out, String name, int code)
    {
        if (code >= 0 && code <= MAXPROP && _theProperties[code] != null)
        {
            out.print("<");
            out.print(name);
            out.print(">");
            out.print(_theProperties[code]);
            out.print("</");
            out.print(name);
            out.println(">");
        }
    }

    public void Dump(PrintWriter out)
    {
        out.println("<info>");

        Dump(out, "title", TITLE);
        Dump(out, "subject", SUBJECT);
        Dump(out, "author", AUTHOR);
        Dump(out, "manager", MANAGER);
        Dump(out, "company", COMPANY);
        Dump(out, "operator", OPERATOR);

        Dump(out, "category", CATEGORY);
        Dump(out, "keywords", KEYWORDS);
        Dump(out, "comment", COMMENT);
        Dump(out, "doccomm", DOCCOMM);
        Dump(out, "hlinkbase", HLINKBASE);

        Dump(out, "creatim", CREATIM);
        Dump(out, "revtim", REVTIM);
        Dump(out, "printim", PRINTIM);
        Dump(out, "buptim", BUPTIM);
        Dump(out, "nofpages", NOFPAGES);
        Dump(out, "nofwords", NOFWORDS);
        Dump(out, "nofchars", NOFCHARS);
        Dump(out, "nofcharsws", NOFCHARSWS);
        Dump(out, "id", VERN);
        Dump(out, "edmins", VERN);
        Dump(out, "version", VERN);
        Dump(out, "vern", VERN);

        out.println("</info>");
    }
}
