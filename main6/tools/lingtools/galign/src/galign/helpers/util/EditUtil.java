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

package galign.helpers.util;

import java.io.UnsupportedEncodingException;

import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * This class collects some of the standard utility functions a
 * HTML-based UI needs, like URL-encoding, Javascript string encoding
 * and the like.<p>
 */
public final class EditUtil
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

    private static final HashMap s_CharToXmlEntity = mapCharToXmlEntity();
    private static HashMap mapCharToXmlEntity()
    {
        // If you add an entry, increase the initial capacity
        HashMap h = new HashMap(5);
        h.put(new Character('<'), "&lt;");
        h.put(new Character('>'), "&gt;");
        h.put(new Character('&'), "&amp;");
        h.put(new Character('\''), "&apos;");
        h.put(new Character('"'), "&quot;");
        return h;
    }

    private static final HashMap s_xmlEntityToChar = mapXmlEntityToChar();
    private static HashMap mapXmlEntityToChar()
    {
        // If you add an entry, increase the initial capacity
        HashMap h = new HashMap(5);
        h.put("&lt;", new Character('<'));
        h.put("&gt;", new Character('>'));
        h.put("&amp;", new Character('&'));
        h.put("&apos;", new Character('\''));
        h.put("&quot;", new Character('"'));
        return h;
    }

    private static Pattern rep_decodeTags = Pattern.compile("&[^;]+;");

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
    public static String unescape(String s)
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

    /**
     * Decodes a string escaped by the MSXML.XMLHTTP builtin escape()
     * mechanism (the old url-escaping) that does map '+' to ' ' and
     * real spaces to %20.
     */
    public static String oldUnescape(String s)
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
            case ' ':
                sb.append('+');
                break;
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


    /**
     * Decodes the standard XML entities &amp;, &lt;, &gt; &quot; and
     * &apos;.  Does <string>not</strong> decode numeric entities.
     */
    public static String decodeXmlEntities(String s)
    {
        if (s == null || s.length() == 0)
        {
            return s;
        }

        Matcher re = rep_decodeTags.matcher(s);

        int last_index = 0;
        StringBuffer ret = new StringBuffer(s.length());

        while (re.find(last_index))
        {
            ret.append(s.substring(last_index, re.start()));
            last_index = re.end();

            String match = re.group();

            Character ch = (Character)s_xmlEntityToChar.get(match);
            if (ch != null)
            {
                ret.append(ch.charValue());
            }
            else
            {
                ret.append(match);
            }
        }

        ret.append(s.substring(last_index));

        return ret.toString();
    }

    /**
     * <P>Encodes special XML characters in a string (&lt;, &gt;,
     * &amp;, &apos; and &quot;).</P>
     */
    static public String encodeXmlEntities(String s)
    {
        if (s == null || s.length() == 0)
        {
            return s;
        }

        StringBuffer res = new StringBuffer(s.length());

        for (int i = 0, max = s.length(); i < max; i++)
        {
            char c = s.charAt(i);

            switch (c)
            {
            case '<':  res.append("&lt;");   break;
            case '>':  res.append("&gt;");   break;
            case '&':  res.append("&amp;");  break;
            case '\'': res.append("&apos;"); break;
            case '"':  res.append("&quot;"); break;
            default:   res.append(c);        break;
            }
        }

        return res.toString();
    }

    /**
     * <P>Encodes special XML characters in a stringbuffer (&lt;,
     * &gt;, &amp;, &apos; and &quot;).</P>
     *
     * <P>Note that duplicating the implementation is optimal since
     * String does not allow accessing its internal StringBuffer.</P>
     */
    static public String encodeXmlEntities(StringBuffer s)
    {
        StringBuffer res = new StringBuffer();

        for (int i = 0; i < s.length(); i++)
        {
            char c = s.charAt(i);

            switch (c)
            {
            case '<':  res.append("&lt;");   break;
            case '>':  res.append("&gt;");   break;
            case '&':  res.append("&amp;");  break;
            case '\'': res.append("&apos;"); break;
            case '"':  res.append("&quot;"); break;
            default:   res.append(c);        break;
            }
        }

        return res.toString();
    }

    /**
     * <P>Encodes special HTML characters in a string (&lt;, &gt;,
     * &amp;, and &quot;).</P>
     */
    static public String encodeHtmlEntities(String s)
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
            case '<':  res.append("&lt;");   break;
            case '>':  res.append("&gt;");   break;
            case '&':  res.append("&amp;");  break;
            case '"':  res.append("&quot;"); break;
            default:   res.append(c);        break;
            }
        }

        return res.toString();
    }

    /**
     * <P>Encodes special HTML characters in a stringbuffer (&lt;,
     * &gt;, &amp; and &quot;).</P>
     *
     * <P>Note that duplicating the implementation is optimal since
     * String does not allow accessing its internal StringBuffer.</P>
     */
    static public String encodeHtmlEntities(StringBuffer s)
    {
        StringBuffer res = new StringBuffer();

        for (int i = 0; i < s.length(); i++)
        {
            char c = s.charAt(i);

            switch (c)
            {
            case '<':  res.append("&lt;");   break;
            case '>':  res.append("&gt;");   break;
            case '&':  res.append("&amp;");  break;
            case '"':  res.append("&quot;"); break;
            default:   res.append(c);        break;
            }
        }

        return res.toString();
    }

    /**
     * Returns a RFC 1766-style string of a Locale, that is with dash
     * (-) as separator.
     *
     * @see http://www.ietf.org/rfc/rfc1766.txt
     */
    static public String toRFC1766(Locale p_locale)
    {
        String res = p_locale.getLanguage();

        // Work around the JDK's stupid Locale behavior
        if      (res.startsWith("iw")) res = "he";
        else if (res.startsWith("ji")) res = "yi";
        else if (res.startsWith("in")) res = "id";

        if (p_locale.getCountry() != null)
        {
            res = res + "-" + p_locale.getCountry();
        }

        return res;
    }

    /**
     * Returns a RFC 1766-style string of a string representation of a
     * Locale, that is with dash (-) as separator.
     *
     * @see http://www.ietf.org/rfc/rfc1766.txt
     */
    static public String toRFC1766(String p_locale)
    {
        String res = p_locale.substring(0, 2);

        // Work around the JDK's stupid Locale behavior
        if      (res.startsWith("iw")) res = "he";
        else if (res.startsWith("ji")) res = "yi";
        else if (res.startsWith("in")) res = "id";

        if (p_locale.length() > 3)
        {
            res = res + "-" + p_locale.substring(3);
        }

        return res;
    }

    /**
     * <P>Tests whether the writing direction of this language is
     * left-to-right or right-to-left.</P>
     *
     * @return true for Arabic and Hebrew, else false.
     */
    static public boolean isRTLLocale(Locale p_locale)
    {
        String language = p_locale.getLanguage();

        // GlobalSightLocale doesn't rewrite language name
        // (unlike java.util.Locale).
        if (language.equals("ar") || language.equals("he") ||
            language.equals("fa") || language.equals("ur"))
        {
            return true;
        }

        return false;
    }

    /**
     * <P>Tests whether the writing direction of this language is
     * left-to-right or right-to-left.</P>
     *
     * @return true for Arabic and Hebrew, else false.
     */
    static public boolean isRTLLocale(String p_language)
    {
        // GlobalSightLocale doesn't rewrite language name
        // (unlike java.util.Locale).
        if (p_language.startsWith("ar") || p_language.startsWith("he") ||
            p_language.startsWith("fa") || p_language.startsWith("ur"))
        {
            return true;
        }

        return false;
    }

    /**
     * <P>Get the HTML's DIR attribute value based on the locale's
     * language.</P>
     *
     * @return "RTL" for Arabic and Hebrew, else "LTR".
     */
    static public String getWritingDirection(Locale p_locale)
    {
        if (isRTLLocale(p_locale))
        {
            return "RTL";
        }

        return "LTR";
    }

    /**
     * Returns a string "LANG=ll-cc DIR=dir" for displaying a segment
     * in the right writing order in HTML. Strings visible in
     * documents (TRANSLATABLE) will have the native language and
     * direction. LOCALIZABLES will have en-US, standing for ASCII,
     * and ltr.
     */
    static public String getLanguageAttributes(Locale p_locale,
        boolean p_isLocalizable)
    {
        String lang = "";
        String dir = "";

        if (p_isLocalizable)
        {
            lang = "LANG='en-US'";
            dir = " DIR='ltr'";
        }
        else
        {
            lang = "LANG=" + p_locale.toString();
            dir = " DIR=" + getWritingDirection(p_locale);
        }

        return lang + dir;
    }


    /**
     * Adjusts trailing whitespace in segments: if the old segment had
     * trailing WS and the new segment does not, add a whitespace.
     *
     * Note: if the new segment is empty, it is returned unchanged.
     */
    static public String adjustWhitespace(String p_new, String p_old)
    {
        String result = p_new;

        if (p_new.length() > 0)
        {
            char chOld = p_old.charAt(p_old.length() - 1);
            char chNew = p_new.charAt(p_new.length() - 1);

            if (Character.isWhitespace(chOld) &&
                !Character.isWhitespace(chNew))
            {
                result += " ";
            }
            else if (!Character.isWhitespace(chOld) &&
                Character.isWhitespace(chNew))
            {
                do
                {
                    result = result.substring(0, result.length() - 1);
                    chNew = result.charAt(result.length() - 1);
                }
                while (Character.isWhitespace(chNew));
            }
        }

        return result;
    }

    /**
     * <p>Decodes a UTF-8 string to UCS2.</p>
     */
    static public String utf8ToUnicode(String p_string)
    {
        return p_string;
    }

    /**
     * <p>Encodes a Unicode string to UTF-8.</p>
     */
    static public String unicodeToUtf8(String p_string)
    {
        return p_string;
    }

    /**
     * <p>Decodes &amp;apos; to the single quote character.</p>
     */
    static public String xmlToHtml(String p_string)
    {
        int i;

        if ((i = p_string.indexOf("&apos;")) >= 0)
        {
            StringBuffer temp = new StringBuffer(p_string);

            do
            {
                temp = temp.replace(i, i + 6, "'");
            }
            while ((i = temp.toString().indexOf("&apos;")) >= 0);

            p_string = temp.toString();
        }

        return p_string;
    }

    /**
     * Strips (G)XML tags from a string. This is a quicker way than
     * parsing a GXML string and using getText().
     */
    static public String stripTags(String p_string)
    {
        return p_string.replaceAll("<[^>]+>", "");
    }
}
