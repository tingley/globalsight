/**
 * Copyright 2009 Welocalize, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * 
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 */

package com.plug.Version_8_5_2.gs.util.edit;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.plug.Version_8_5_2.gs.util.IntHolder;

/**
 * This class collects some of the standard utility functions a HTML-based UI
 * needs, like URL-encoding, Javascript string encoding and the like.
 * <p>
 */
public final class EditUtil
{
    /**
     * Computes the length in bytes a Unicode string would have when converted
     * to UTF-8, without doing the actual conversion.
     */
    public static final int getUTF8Len(String p_string)
    {
        return getUTF8Len(p_string, p_string.length());
    }

    /**
     * Computes the length in bytes a Unicode string would have when converted
     * to UTF-8, without doing the actual conversion. Considers all characters
     * from <code>0</code> to <code>p_maxlen-1</code>
     */
    public static final int getUTF8Len(String p_string, int p_maxlen)
    {
        return calculateUTF8Len(p_string, p_maxlen, new IntHolder(), 0);
    }
    
    /**
     * Calculates the UTF8 length in bytes of the string. Also records the max
     * character index where the byte size is less than or equal to p_maxByteLen
     */
    private static final int calculateUTF8Len(String p_string, int p_strlen,
            IntHolder p_maxCharIndex, int p_maxByteLen)
    {
        int utflen = 0;
        char c;

        for (int i = 0; i < p_strlen; i++)
        {
            c = p_string.charAt(i);

            if ((c >= 0x0000) && (c <= 0x007F))
            {
                utflen++;
            }
            else if (c > 0x07FF)
            {
                utflen += 3;
            }
            else
            {
                utflen += 2;
            }

            if (utflen <= p_maxByteLen)
            {
                p_maxCharIndex.value = i;
            }
        }

        return utflen;
    }
    
    static public String encodeXmlEntities(String s)
    {
        if (s == null || s.length() == 0)
        {
            return s;
        }

        StringBuffer res = new StringBuffer(s.length());

        for (int i = 0; i < s.length(); i++)
        {
            char c = s.charAt(i);

            switch (c)
            {
                case '<':
                    res.append("&lt;");
                    break;
                case '>':
                    res.append("&gt;");
                    break;
                case '&':
                    res.append("&amp;");
                    break;
                case '\'':
                    res.append("&apos;");
                    break;
                case '"':
                    res.append("&quot;");
                    break;
                default:
                    res.append(c);
                    break;
            }
        }

        return res.toString();
    }

    static public String toRFC1766(String p_locale)
    {
        String res = p_locale.substring(0, 2);

        // Work around the JDK's stupid Locale behavior
        if (res.startsWith("iw"))
            res = "he";
        else if (res.startsWith("ji"))
            res = "yi";
        else if (res.startsWith("in"))
            res = "id";

        if (p_locale.length() > 3)
        {
            res = res + "-" + p_locale.substring(3);
        }

        return res;
    }
}
