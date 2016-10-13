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
package com.globalsight.ling.docproc.extractor.xptag;

import java.util.ArrayList;
import java.util.Iterator;
import java.io.UnsupportedEncodingException;

/**
 * <P>Provides methods for mapping Quark XPress tags to Unicode
 * characters.</P>
 */
public class XPTagProperties
{
    //
    // Constructor
    //
    public XPTagProperties()
    {
    }

    //
    // Public Methods
    //

    public boolean isParagraphBreak(String p_code)
    {
        if (p_code.equals("\\b") ||               // new box
            p_code.equals("\\c") ||               // new column
            p_code.equals("\\i") ||               // indent here (?)
            p_code.equals("\\t"))                 // right indent tab (?)
        {
            return true;
        }

        return false;
    }

    /**
     * Maps special characters to Unicode code points. See
     * common/XPTagEnDecoder for the reverse mapping during merge.
     */
    public String mapSpecialCode(String p_code, String p_codeset)
    {
        if (p_code.startsWith("\\#"))
        {
            return mapCharacterCode(p_code, p_codeset);
        }

        if (p_code.equals("\\d"))
        {
            // discretionary return - discard (language-dependent)
            return "";
        }
        else if (p_code.equals("\\-"))
        {
            // hyphen
            return "-";
        }
        else if (p_code.equals("\\!-"))
        {
            // non-breaking hyphen
            return "\u2011";
        }
        else if (p_code.equals("\\h"))
        {
            // discretionary hyphen - discard (language-dependent)
            return "";
        }
        else if (p_code.equals("\\m"))
        {
            // breaking em-dash
            return "\u2014";
        }
        else if (p_code.equals("\\!m"))
        {
            // non-breaking em dash
            return "\u2014";
        }
        else if (p_code.equals("\\s"))
        {
            // standard space
            return " ";
        }
        else if (p_code.equals("\\!s"))
        {
            // non-breaking standard space
            return "\u00a0";
        }
        else if (p_code.equals("\\f"))
        {
            // figure space
            return " ";
        }
        else if (p_code.equals("\\!f"))
        {
            // non-breaking figure space
            return "\u00a0";
        }
        else if (p_code.equals("\\p"))
        {
            // punctuation space
            return " ";
        }
        else if (p_code.equals("\\!p"))
        {
            // non-breaking punctuation space
            return "\u00a0";
        }
        else if (p_code.equals("\\q"))
        {
            // flex space
            return " ";
        }
        else if (p_code.equals("\\!q"))
        {
            // non-breaking flex space
            return "\u00a0";
        }

        // \\n - sort return - output as ptag.

        return null;
    }

    // Character codes are <\#nnn> where nnn refers to a character in
    // the specified codeset (MacRoman, Cp1252 or ISO8859_1).
    private String mapCharacterCode(String p_code, String p_codeset)
    {
        String digits = p_code.substring(2);
        byte[] bite = new byte[1];

        try
        {
            bite[0] = (byte)Integer.parseInt(digits, 10);

            return new String(bite, p_codeset);
        }
        catch (Exception ex)
        {
            return "?";
        }
    }
}
