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

package com.globalsight.util.edit;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;

import com.globalsight.cxe.adapter.adobe.AdobeHelper;
import com.globalsight.cxe.adapter.idml.IdmlConverter;
import com.globalsight.cxe.adapter.msoffice2010.MsOffice2010Converter;
import com.globalsight.cxe.adapter.openoffice.OpenOfficeConverter;
import com.globalsight.everest.webapp.pagehandler.edit.online.EditorState;
import com.globalsight.ling.docproc.IFormatNames;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.IntHolder;
import com.sun.org.apache.regexp.internal.RE;
import com.sun.org.apache.regexp.internal.RECompiler;
import com.sun.org.apache.regexp.internal.REProgram;
import com.sun.org.apache.regexp.internal.RESyntaxException;

/**
 * This class collects some of the standard utility functions a HTML-based UI
 * needs, like URL-encoding, Javascript string encoding and the like.
 * <p>
 */
public final class EditUtil
{
    private static final Logger CATEGORY = Logger.getLogger(EditUtil.class
            .getName());

    static private REProgram rep_stripTags;

    static private REProgram rep_decodeTags;

    static private REProgram rep_checkExtension;

    static
    {
        try
        {
            RECompiler dragon = new RECompiler();
            rep_stripTags = dragon.compile("<[^>]+>");
            rep_decodeTags = dragon.compile("&[^;]+;");
            rep_checkExtension = dragon.compile("[\\/:\"<>|*?]");
        }
        catch (RESyntaxException e)
        {
            CATEGORY.error("EditUtil: pilot error in regex", e);
        }
    }

    private static final String NCR_BEGIN = "\\u";

    private static final char[] ZERO_ARRAY =
    { '0', '0', '0', '0' };

    private static final int HEX_DIGIT = 4;

    private static final Map s_mapCharToEscape = mapCharToEscape();

    private static Map mapCharToEscape()
    {
        // If you add an entry, increase the initial capacity
        Map h = new HashMap(9);
        h.put(new Character('\\'), "\\\\");
        h.put(new Character('"'), "\\\"");
        h.put(new Character('\''), "\\'");
        h.put(new Character('\b'), "\\b"); // backspace
        h.put(new Character('\u000b'), "\\v"); // vertical tab
        h.put(new Character('\u000c'), "\\f"); // form feed
        h.put(new Character('\n'), "\\n");
        h.put(new Character('\r'), "\\r");
        h.put(new Character('\t'), "\\t");
        return h;
    }

    private static final Map s_CharToXmlEntity = mapCharToXmlEntity();

    private static Map mapCharToXmlEntity()
    {
        // If you add an entry, increase the initial capacity
        Map h = new HashMap(5);
        h.put(new Character('<'), "&lt;");
        h.put(new Character('>'), "&gt;");
        h.put(new Character('&'), "&amp;");
        h.put(new Character('\''), "&apos;");
        h.put(new Character('"'), "&quot;");
        return h;
    }

    private static final Map s_xmlEntityToChar = mapXmlEntityToChar();

    private static Map mapXmlEntityToChar()
    {
        // If you add an entry, increase the initial capacity
        Map h = new HashMap(5);
        h.put("&lt;", new Character('<'));
        h.put("&gt;", new Character('>'));
        h.put("&amp;", new Character('&'));
        h.put("&apos;", new Character('\''));
        h.put("&quot;", new Character('"'));
        return h;
    }

    /**
     * Converts a Unicode string to a Javascript string with all non ISO-8859-1
     * characters encoded by Javascript \&zwnj;u escapes.
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
                String es = (String) s_mapCharToEscape.get(c);

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
     * Decodes a string escaped by the Internet Explorer's Javascript escape()
     * method to a Unicode String. IE's version encodes chars above 255 as
     * %uXXXX and does not map '+' to ' '.
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
                            sb.append((char) Integer.parseInt(
                                    s.substring(i + 2, i + 6), 16));
                            i += 5;
                        }
                        else
                        {
                            sb.append((char) Integer.parseInt(
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
     * Decodes a string escaped by the MSXML.XMLHTTP builtin escape() mechanism
     * (the old url-escaping) that does map '+' to ' ' and real spaces to %20.
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
                            sb.append((char) Integer.parseInt(
                                    s.substring(i + 2, i + 6), 16));
                            i += 5;
                        }
                        else
                        {
                            sb.append((char) Integer.parseInt(
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
     * Decodes the standard XML entities &amp;, &lt;, &gt; &quot; and &apos;.
     * Does <string>not</strong> decode numeric entities.
     */
    public static String decodeXmlEntities(String s)
    {
        if (s == null || s.length() == 0)
        {
            return s;
        }

        RE re = new RE(rep_decodeTags, RE.MATCH_NORMAL);

        int last_index = 0;
        StringBuffer ret = new StringBuffer(s.length());

        while (re.match(s, last_index))
        {
            ret.append(s.substring(last_index, re.getParenStart(0)));
            last_index = re.getParenEnd(0);

            String match = re.getParen(0);

            Character ch = (Character) s_xmlEntityToChar.get(match);
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
     * <P>
     * Encodes special XML characters in a string (&lt;, &gt;, &amp;, &apos; and
     * &quot;).
     * </P>
     */
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

    /**
     * <P>
     * Encodes special html characters in a string (&lt;, &gt;, &amp;, &nbsp;
     * and &quot;).
     * </P>
     */
    static public String encodeTohtml(String s)
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

    /**
     * <P>
     * Encodes special XML characters in a stringbuffer (&lt;, &gt;, &amp;,
     * &apos;, &quot; and \n \r).
     * 
     * Note that duplicating the implementation is optimal since String does not
     * allow accessing its internal StringBuffer.
     * </P>
     */
    static public String encodeXmlEntities(StringBuffer s)
    {
        StringBuffer res = new StringBuffer();

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

    /**
     * <P>
     * Encodes special XML characters in a stringbuffer (\n \r \t).
     * 
     * Note that duplicating the implementation is optimal since String does not
     * allow accessing its internal StringBuffer.
     * 
     * Note that java regards \n and \r the same.
     * </P>
     */
    static public String encodeNTREntities(StringBuffer s)
    {
        // GBS-3882, donot specially handle \n, \r, \t any more
        return s.toString();
    }

    /**
     * <P>
     * Encodes special HTML characters in a string (&lt;, &gt;, &amp;, and
     * &quot;).
     * </P>
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
                case '<':
                    res.append("&lt;");
                    break;
                case '>':
                    res.append("&gt;");
                    break;
                case '&':
                    res.append("&amp;");
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

    /**
     * <P>
     * Encodes special HTML characters in a stringbuffer (&lt;, &gt;, &amp; and
     * &quot;).
     * </P>
     * 
     * <P>
     * Note that duplicating the implementation is optimal since String does not
     * allow accessing its internal StringBuffer.
     * </P>
     */
    static public String encodeHtmlEntities(StringBuffer s)
    {
        StringBuffer res = new StringBuffer();

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

    /**
     * Returns a RFC 1766-style string of a GlobalSightLocale, that is with dash
     * (-) as separator.
     * 
     * @see http://www.ietf.org/rfc/rfc1766.txt
     */
    static public String toRFC1766(GlobalSightLocale p_locale)
    {
        String res = p_locale.getLanguageCode();

        // Work around the JDK's stupid Locale behavior
        if (res.startsWith("iw"))
            res = "he";
        else if (res.startsWith("ji"))
            res = "yi";
        else if (res.startsWith("in"))
            res = "id";

        if (p_locale.getCountryCode() != null)
        {
            res = res + "-" + p_locale.getCountryCode();
        }

        return res;
    }

    /**
     * Returns a RFC 1766-style string of a Locale, that is with dash (-) as
     * separator.
     * 
     * @see http://www.ietf.org/rfc/rfc1766.txt
     */
    static public String toRFC1766(Locale p_locale)
    {
        String res = p_locale.getLanguage();

        // Work around the JDK's stupid Locale behavior
        if (res.startsWith("iw"))
            res = "he";
        else if (res.startsWith("ji"))
            res = "yi";
        else if (res.startsWith("in"))
            res = "id";

        if (p_locale.getCountry() != null)
        {
            res = res + "-" + p_locale.getCountry();
        }

        return res;
    }

    /**
     * Returns a RFC 1766-style string of a string representation of a Locale,
     * that is with dash (-) as separator.
     * 
     * @see http://www.ietf.org/rfc/rfc1766.txt
     */
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

    /**
     * <P>
     * Tests whether the writing direction of this language is left-to-right or
     * right-to-left.
     * </P>
     * 
     * @return true for Arabic and Hebrew, else false.
     */
    static public boolean isRTLLocale(GlobalSightLocale p_locale)
    {
        return isRTLLocale(p_locale.getLanguageCode());
    }

    /**
     * <P>
     * Tests whether the writing direction of this language is left-to-right or
     * right-to-left.
     * </P>
     * 
     * @return true for Arabic and Hebrew, else false.
     */
    static public boolean isRTLLocale(String language)
    {
        // GlobalSightLocale doesn't rewrite language name
        // (unlike java.util.Locale).
        return (language.startsWith("ar") || language.startsWith("he")
                || language.startsWith("iw") || language.startsWith("fa") || language
                    .startsWith("ur"));
    }

    public static String toRtlString(String s)
    {
        if (s == null)
            s = "";

        return '\u200F' + s;
    }

    public static String removeU200F(String s)
    {
        if (s == null)
            s = "";

        return s.replace("" + '\u200F', "");
    }

    /**
     * <P>
     * Get the HTML's DIR attribute value based on the locale's language.
     * </P>
     * 
     * @return "RTL" for Arabic and Hebrew, else "LTR".
     */
    static public String getWritingDirection(GlobalSightLocale p_locale)
    {
        if (isRTLLocale(p_locale))
        {
            return "RTL";
        }

        return "LTR";
    }

    /**
     * Returns a string "LANG=ll-cc DIR=dir" for displaying a segment in the
     * right writing order in HTML. Strings visible in documents (TRANSLATABLE)
     * will have the native language and direction. LOCALIZABLES will have
     * en-US, standing for ASCII, and ltr.
     */
    static public String getLanguageAttributes(GlobalSightLocale p_locale,
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
     * <p>
     * Returns true when a file of the given datatype is derived from an HTML
     * format.
     * </p>
     * 
     * <p>
     * Derived Formats: HTML, JHTML, ASP/JSP, CFM.
     * </p>
     * 
     * <p>
     * Formats not derived: JHTML, ColdFusion, ASP/JSP, JavaScript, CSS,
     * PlainText.
     * </p>
     */
    static public boolean isHtmlDerivedFormat(String p_dataType)
    {
        if (p_dataType == null)
        {
            CATEGORY.warn("FIXME isHtmlDerivedFormat(): datatype is null");
            return false;
        }

        if (p_dataType.equals(IFormatNames.FORMAT_HTML)
                || p_dataType.equals(IFormatNames.FORMAT_WORD_HTML)
                || p_dataType.equals(IFormatNames.FORMAT_EXCEL_HTML)
                || p_dataType.equals(IFormatNames.FORMAT_POWERPOINT_HTML)
                || p_dataType.equals(IFormatNames.FORMAT_CF)
                || p_dataType.equals(IFormatNames.FORMAT_ASP)
                || p_dataType.equals(IFormatNames.FORMAT_JSP)
                || p_dataType.equals(IFormatNames.FORMAT_JHTML))
        {
            return true;
        }

        return false;
    }

    /**
     * <p>
     * Returns true when a file of the given datatype has a preview mode or not.
     * </p>
     * 
     * <p>
     * Format with preview: HTML and MS-Office (Word/Excel/PPT).
     * </p>
     * 
     * <p>
     * Format without preview: JHTML, ColdFusion, ASP/JSP, CFM, JavaScript, CSS,
     * PlainText.
     * </p>
     */
    static public boolean hasPreviewMode(String p_dataType)
    {
        if (p_dataType == null)
        {
            CATEGORY.warn("FIXME hasPreviewMode(): datatype is null");
            return false;
        }

        if (p_dataType.equals(IFormatNames.FORMAT_HTML)
                || p_dataType.equals(IFormatNames.FORMAT_WORD_HTML)
                || p_dataType.equals(IFormatNames.FORMAT_EXCEL_HTML))
        {
            return true;
        }

        if (p_dataType.equals(IFormatNames.FORMAT_OPENOFFICE_XML))
        {
            return OpenOfficeConverter.isOpenOfficeInstalled();
        }

        if (p_dataType.equals(IFormatNames.FORMAT_OFFICE_XML))
        {
            return MsOffice2010Converter.isInstalled();
        }

        return false;
    }

    static public String warnPreviewNotInstalled(String p_dataType)
    {
        if (p_dataType.equals(IFormatNames.FORMAT_OFFICE_XML)
                && !MsOffice2010Converter.isInstalled())
        {
            return "lb_preivew_not_set_office2007";
        }

        return "";
    }

    static public String warnPdfPreviewNotInstalled(EditorState p_state)
    {
        if (p_state == null)
        {
            CATEGORY.warn("FIXME hasPDFPreviewMode(): p_state is null");
            return "";
        }

        String filename = p_state.getSourcePageName();

        filename = filename.toLowerCase();
        if (filename.endsWith("idml") && !IdmlConverter.isInstalled())
        {
            return "lb_preivew_not_set_cs5";
        }

        return "";
    }

    static public boolean hasPDFPreviewMode(EditorState p_state)
    {
        if (p_state == null)
        {
            CATEGORY.warn("FIXME hasPDFPreviewMode(): p_state is null");
            return false;
        }

        String filename = p_state.getSourcePageName().toLowerCase();

        if ((filename.endsWith(".indd") || filename.endsWith(".inx"))
                && !filename.startsWith(AdobeHelper.XMP_DISPLAY_NAME_PREFIX))
        {
            return true;
        }

        if (filename.endsWith(".fm") && "mif".equals(p_state.getPageFormat()))
        {
            return true;
        }

        if (filename.endsWith(".docx")
                && "office-xml".equals(p_state.getPageFormat()))
        {
            return true;
        }

        filename = filename.toLowerCase();
        if (filename.endsWith("idml"))
        {
            return IdmlConverter.isInstalled();
        }

        return false;
    }

    /**
     * Returns true for document formats that are whitespace preserving (like
     * Java, JS, CPP, CSS) and whose segments need to be shown in the
     * WS-preserving segment editor.
     */
    static public boolean isWhitePreservingFormat(String p_dataType,
            String p_itemType)
    {
        if (p_dataType == null)
        {
            CATEGORY.warn("FIXME isWhitePreservingFormat(): datatype is null");
            return false;
        }

        // Tue May 27 23:43:57 2003 CvdL: made CSS* whitespace preserving
        // so the fixed-with font text editor comes up.
        if (p_dataType.equals(IFormatNames.FORMAT_PLAINTEXT)
                || p_dataType.equals(IFormatNames.FORMAT_CSS)
                || p_dataType.equals(IFormatNames.FORMAT_CSS_STYLE)
                || p_dataType.equals(IFormatNames.FORMAT_JAVAPROP)
                || p_dataType.equals(IFormatNames.FORMAT_JAVAPROP_MSG)
                || p_dataType.equals(IFormatNames.FORMAT_JAVA)
                || p_dataType.equals(IFormatNames.FORMAT_JAVASCRIPT)
                || p_dataType.equals(IFormatNames.FORMAT_VBSCRIPT)
                || p_dataType.equals(IFormatNames.FORMAT_CFSCRIPT)
                || p_dataType.equals(IFormatNames.FORMAT_CPP)
                || p_dataType.equals(IFormatNames.FORMAT_RC))
        {
            return true;
        }

        return false;
    }

    /**
     * Returns true for segments that contain text (item type "text" or
     * "string", as opposed to tokens like URLs or CSS styles). This method gets
     * used to fix leading and trailing whitespace in text segments (PMI, see
     * gsdef 10058).
     */
    static public boolean isTextSegment(String p_itemType)
    {
        if (p_itemType.equals("text") || p_itemType.equals("string"))
        {
            return true;
        }

        return false;
    }

    /**
     * Adjusts trailing whitespace in segments: if the old segment had trailing
     * WS and the new segment does not, add a whitespace.
     * 
     * Note: if the new segment is empty, it is returned unchanged.
     */
    static public String adjustWhitespace(String p_new, String p_old)
    {
        String result = p_new;

        if (p_new != null && p_old != null && p_new.length() > 0
                && p_old.length() > 0)
        {
            char chOld = p_old.charAt(p_old.length() - 1);
            char chNew = p_new.charAt(p_new.length() - 1);

            if (Character.isWhitespace(chOld) && !Character.isWhitespace(chNew))
            {
                result += " ";
            }
            else if (!Character.isWhitespace(chOld)
                    && Character.isWhitespace(chNew))
            {
                do
                {
                    result = result.substring(0, result.length() - 1);
                    if (result.length() <= 0)
                    {
                        break;
                    }
                    else
                    {
                        chNew = result.charAt(result.length() - 1);
                    }
                } while (Character.isWhitespace(chNew));
            }
        }

        return result;
    }

    /**
     * <p>
     * Decodes a UTF-8 string to UCS2.
     * </p>
     */
    static public String utf8ToUnicode(String p_string)
    {
        return p_string;
    }

    /**
     * <p>
     * Encodes a Unicode string to UTF-8.
     * </p>
     */
    static public String unicodeToUtf8(String p_string)
    {
        return p_string;
    }

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
     * Truncates the given string to the specified byte size (or less) based on
     * its UTF8 len
     */
    public static final String truncateUTF8Len(String p_string, int p_maxByteLen)
    {
        if (p_string.length() == 0)
        {
            return p_string;
        }

        IntHolder maxByteIndex = new IntHolder();

        calculateUTF8Len(p_string, p_string.length(), maxByteIndex,
                p_maxByteLen);

        return p_string.substring(0, maxByteIndex.value + 1);
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

    /**
     * <p>
     * Decodes &amp;apos; to the single quote character.
     * </p>
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
            } while ((i = temp.toString().indexOf("&apos;")) >= 0);

            p_string = temp.toString();
        }

        return p_string;
    }

    /**
     * Strips (G)XML tags from a string. This is a quicker way than parsing a
     * GXML string and using getText().
     */
    static public String stripTags(String p_string)
    {
        RE re = new RE(rep_stripTags);

        return re.subst(p_string, "");
    }

    /**
     * Checks if a file extension contains certain invalid characters (\\ / :
     * &quot; &lt; &gt; | * ?).
     * 
     * @return 'true' if the FileExtension is valid, 'false' if it contains
     *         invalid characters
     */
    static public boolean validateFileExtension(String p_fileExtension)
    {
        RE re = new RE(rep_checkExtension);

        if (re.match(p_fileExtension))
        {
            return false;
        }

        return true;
    }

    /**
     * Remove CRLF from string, and change "\t" to one space only.
     * 
     * @param str
     *            -- String to be changed
     * @return -- String
     */
    public static String removeCRLF(String str)
    {
        if (str == null)
            return null;

        str = str.trim();
        StringBuffer res = new StringBuffer();
        for (int i = 0; i < str.length(); i++)
        {
            char c = str.charAt(i);

            switch (c)
            {
                case '\t':
                    res.append(" ");
                    break;
                case '\n':
                case '\r':
                    res.append("");
                    break;
                default:
                    res.append(c);
                    break;
            }
        }

        return res.toString();
    }
}
