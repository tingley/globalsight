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
package spell;

import java.util.*;

public class EditUtil
{
    private static final String NCR_BEGIN = "\\u";
    private static final char[] ZERO_ARRAY = {'0', '0', '0', '0'};
    private static final int HEX_DIGIT = 4;

    private static final HashMap s_mapCharToEscape = mapCharToEscape();
    private static HashMap mapCharToEscape()
    {
        // If you add an entry, increase the initial capacity
        HashMap h = new HashMap(9);
        h.put(new Character('\\'), "\\\\");
        h.put(new Character('"'),  "\\\"");
        h.put(new Character('\''), "\\'");
        h.put(new Character('\b'), "\\b");        // backspace
        h.put(new Character('\u000b'), "\\v");    // vertical tab
        h.put(new Character('\u000c'), "\\f");    // form feed
        h.put(new Character('\n'), "\\n");
        h.put(new Character('\r'), "\\r");
        h.put(new Character('\t'), "\\t");
        return h;
    }

    /**
     * Converts a Unicode string to a Javascript string with all non
     * ISO-8859-1 characters encoded by Javascript \&zwnj;u escapes.
     */
    public static String toJavascript(String s)
    {
        if (s == null || s.length() == 0)
        {
            return s;
        }

        StringBuffer ret = new StringBuffer(s.length());

        for (int i = 0; i < s.length(); ++i)
        {
            char ch = s.charAt(i);
            if (ch > 255)
            {
                String hex = Integer.toHexString(ch);
                ret.append(NCR_BEGIN);
                int len = hex.length();
                if (len < HEX_DIGIT)
                {
                    ret.append(ZERO_ARRAY, 0, HEX_DIGIT - len);
                }
                ret.append(hex);
            }
            else
            {
                Character c = new Character(s.charAt(i));
                String es = (String)s_mapCharToEscape.get(c);

                if (es != null)
                {
                    ret.append(es);
                }
                else
                {
                    ret.append(c);
                }
            }
        }

        return ret.toString();
    }

    /**
     * Decodes a string escaped by the Internet Explorer's Javascript
     * escape() method to a Unicode String.  IE's version encodes
     * chars above 255 as %uXXXX and does not map '+' to ' '.
     */
    static public String unescape(String s)
    {
        if (s == null || s.length() == 0)
        {
            return s;
        }

        StringBuffer sb = new StringBuffer(s.length());

        for (int i = 0; i < s.length(); ++i)
        {
            char c = s.charAt(i);

            switch (c)
            {
            case '%':
                try
                {
                    if (s.charAt(i + 1) == 'u')
                    {
                        sb.append((char)Integer.parseInt(
                            s.substring(i + 2, i + 6), 16));
                        i += 5;
                    }
                    else
                    {
                        sb.append((char)Integer.parseInt(
                            s.substring(i + 1, i + 3), 16));
                        i += 2;
                    }
                }
                catch (Exception e)
                {
                    sb.append(c);
                }
                break;
            default:
                sb.append(c);
                break;
            }
        }

        return sb.toString();
    }
}
