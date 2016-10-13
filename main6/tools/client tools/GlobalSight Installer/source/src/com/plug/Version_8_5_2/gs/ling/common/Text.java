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
package com.plug.Version_8_5_2.gs.ling.common;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.util.ServerUtil;

/**
 * Helper methods for dealing with text strings.
 */
public final class Text
{
    // Fri Mar 28 11:56:15 2003 CvdL: need to make handling of nbsp as
    // whitespace selectable by user (HP: white; Worldbank:
    // non-white). Default in 4.4.4+ is false.
    static public boolean s_nbspIsWhite = false;

    static
    {
        try
        {
            String path = ServerUtil.getPath()
                    + "/jboss/server/standalone/deployments/globalsight.ear/lib/classes/properties/Diplomat";
            ResourceBundle res = ResourceBundle.getBundle(path, Locale.US);

            String value;

            try
            {
                value = res.getString("extractor_nbsp_is_white");
                if (value.equalsIgnoreCase("true"))
                {
                    s_nbspIsWhite = true;
                }
            }
            catch (MissingResourceException e)
            {
            }
        }
        catch (MissingResourceException e)
        {
            // Do nothing if configuration file was not found.
        }
    }

    /**
     * Returns the quote character delimiting a string, or if no quote found,
     * the empty string.
     * 
     * @return the quote character that starts the string, or the empty string.
     */
    final static public String getQuoteCharacter(String p_string)
    {
        if (p_string != null && p_string.length() > 0
                && (p_string.charAt(0) == '"' || p_string.charAt(0) == '\''))
        {
            return p_string.substring(0, 1);
        }

        return "";
    }

    /**
     * Computes the length in bytes a Unicode string would have when converted
     * to UTF-8, without doing the actual conversion.
     */
    final static public int getUTF8Len(String p_string)
    {
        return getUTF8Len(p_string, p_string.length());
    }

    /**
     * Computes the length in bytes a Unicode string would have when converted
     * to UTF-8, without doing the actual conversion. Considers all characters
     * from <code>0</code> to <code>p_maxlen-1</code>
     */
    final static public int getUTF8Len(String p_string, int p_maxlen)
    {
        int utflen = 0;
        char c;

        for (int i = 0; i < p_maxlen; i++)
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
        }

        return utflen;
    }

    /**
     * Returns a substring that is less than or equal to the specified length in
     * byte when the string is converted to UTF-8.
     * 
     * Note: supplementary characters (non BMP characters) are encoded as a
     * surrogate pair in Java string. This method sees surrogates as if real
     * characters. That means supplementary characters are conted as 6 bytes
     * instead of 4 bytes as it is in real UTF-8 string.
     */
    final static public String substrUtf8LimitLen(String p_string, int p_limit)
    {
        int strLen = p_string.length();
        int utflen = 0;
        char c;

        for (int i = 0; i < strLen; i++)
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

            if (utflen == p_limit)
            {
                return p_string.substring(0, i + 1);
            }
            else if (utflen > p_limit)
            {
                return p_string.substring(0, i);
            }
        }

        return p_string;
    }

    /**
     * Tests if the string contains only white space as defined by the Unicode
     * standard. See isSpace(String) for ASCII white space.
     * 
     * Wed Sep 25 17:00:07 2002 CvdL: Added nbsp as a blank character. Tue Dec
     * 10 19:20:07 2002 CvdL: Removed nbsp. Fri Mar 28 11:59:29 2003 CvdL: Made
     * configurable (default false).
     * 
     * This function is and should be used only by the Extractors to test for
     * empty segments (to decide whether to extract it or not).
     */
    final static public boolean isBlank(String p_string)
    {
        for (int i = 0, max = p_string.length(); i < max; i++)
        {
            char ch = p_string.charAt(i);

            if (!Character.isWhitespace(ch))
            {
                if (s_nbspIsWhite && ch == '\u00a0')
                {
                    continue;
                }

                return false;
            }
        }

        return true;
    }

    /**
     * Add just another method for the WordExtractor that includes nbsp so we
     * can detect segments that are really empty if they contain only nbsp as
     * "text".
     * 
     * Fri Jun 06 20:59:11 2003 CvdL
     */
    final static public boolean isBlankOrNbsp(String p_string)
    {
        for (int i = 0, max = p_string.length(); i < max; i++)
        {
            char ch = p_string.charAt(i);

            if (!Character.isWhitespace(ch))
            {
                if (ch == '\u00a0')
                {
                    continue;
                }

                return false;
            }
        }

        return true;
    }

    /**
     * <p>
     * Determines if the specified character is ASCII white space. This method
     * returns <code>true</code> for the following five characters only:
     * 
     * <table>
     * <tr>
     * <td>'\t'</td>
     * <td>&#92;u0009</td>
     * <td><code>HORIZONTAL TABULATION</code></td>
     * </tr>
     * <tr>
     * <td>'\n'</td>
     * <td>&#92;u000A</td>
     * <td><code>NEW LINE</code></td>
     * </tr>
     * <tr>
     * <td>'\f'</td>
     * <td>&#92;u000C</td>
     * <td><code>FORM FEED</code></td>
     * </tr>
     * <tr>
     * <td>'\r'</td>
     * <td>&#92;u000D</td>
     * <td><code>CARRIAGE RETURN</code></td>
     * </tr>
     * <tr>
     * <td>'&nbsp;&nbsp;'</td>
     * <td>&#92;u0020</td>
     * <td><code>SPACE</code></td>
     * </tr>
     * </table>
     * 
     * <p>
     * This method is taken from java.lang.Character, as is. It has been marked
     * as deprecated for reasons that are beyond my limited understanding, but
     * is exactly what is needed.
     * 
     * @param ch
     *            the character to be tested.
     * @return <code>true</code> if the character is ASCII white space;
     *         <code>false</code> otherwise.
     */
    final static public boolean isSpace(char ch)
    {
        return (ch <= 0x0020)
                && (((((1L << 0x0009) | (1L << 0x000A) | (1L << 0x000C) | (1L << 0x000D) | (1L << 0x0020)) >> ch) & 1L) != 0);
    }

    /**
     * <p>
     * Determines if the specified character is an ASCII hexadecimal digit.
     * 
     * @param ch
     *            the character to be tested.
     * @return <code>true</code> if the character is one of '0' - '9', 'a' -
     *         'f', 'A' - 'F'; <code>false</code> otherwise.
     */
    final static public boolean isHexDigit(char ch)
    {
        if ((ch >= '0' && ch <= '9'))
        {
            return true;
        }

        if ((ch >= 'a' && ch <= 'f') || (ch >= 'A' && ch <= 'F'))
        {
            return true;
        }

        return false;
    }

    /**
     * Replaces multiple white spaces and end of lines by a single white space.
     * Replaced characters are "\r\n\t " as determined by
     * Character.isWhitespace().
     */
    final static public String normalizeWhiteSpaces(String p_string)
    {
        final int len = p_string.length();
        StringBuffer result = new StringBuffer(len);

        boolean inWhite = false;

        for (int i = 0; i < len; i++)
        {
            char ch = p_string.charAt(i);

            if (inWhite)
            {
                if (Character.isWhitespace(ch))
                {
                    continue;
                }

                inWhite = false;
            }
            else
            {
                if (Character.isWhitespace(ch))
                {
                    inWhite = true;
                    ch = ' ';
                }
            }

            result.append(ch);
        }

        return result.toString();

        /*
         * Regular expressions kill performance in inner loops. String ret =
         * p_string;
         * 
         * try { ret = RegEx.substituteAll(ret, "[\\n\\r\\s]+", " "); } catch
         * (RegExException ignore) {
         * System.err.println("Syntax error in regular expression."); }
         * 
         * return ret;
         */
    }

    /**
     * Replaces multiple white spaces and end of lines by a single white space.
     */
    final static public String normalizeWhiteSpaceForTm(String p_string)
    {
        final int len = p_string.length();
        StringBuffer result = new StringBuffer(len);

        boolean inWhite = false;

        for (int i = 0; i < len; i++)
        {
            char ch = p_string.charAt(i);

            if (inWhite)
            {
                if (ch == '\u00a0' || Character.isWhitespace(ch))
                {
                    continue;
                }

                inWhite = false;
            }
            else
            {
                if (ch == '\u00a0' || Character.isWhitespace(ch))
                {
                    inWhite = true;
                    ch = ' ';
                }
            }

            result.append(ch);
        }

        return result.toString();

        /*
         * Regular expressions kill performance in inner loops. String ret =
         * p_string;
         * 
         * try { ret = RegEx.substituteAll(ret, "[\\n\\r\\s\\u00a0]+", " "); }
         * catch (RegExException ignore) {
         * System.err.println("Syntax error in regular expression."); }
         * 
         * return ret;
         */
    }

    /**
     * For whitespace handling in the segmenter, word counter and TM: tests the
     * type attribute of a TMX node (PH, BPT etc) if that node represents
     * whitespace and should be replaced with white space temporarily during
     * processing.
     * 
     * Example: 'Segment1.<PH type="x-mso-spacerun">&nbsp;</PH>Segment 2.'
     * 
     * This method is for 4.5 and MS Office formats were segmentation should be
     * whitespace-aware even though segmentation_preserve_whitespace is false.
     * 
     * @see isTmxWhitespaceNode()
     */
    final static public boolean isTmxMsoWhitespaceNode(String p_type)
    {
        if (p_type == null || p_type.length() == 0)
        {
            return false;
        }

        if (p_type.equals("x-mso-tab") || p_type.equals("x-mso-spacerun")
                || p_type.equals("x-mso-paragraph"))
        {
            return true;
        }

        return false;
    }

    /**
     * For whitespace handling in the segmenter, word counter and TM: tests the
     * type attribute of a TMX node (PH, BPT etc) if that node represents
     * whitespace and should be replaced with white space temporarily during
     * processing.
     * 
     * Example: "Segment 1.<BR>
     * Segment 2." --> BR is white for segmentation and word counting.
     * 
     * This method is for backward-compatibility with 4.4.x TMs where whitespace
     * was not recognized during segmentation.
     * 
     * @see isTmxMsoWhitespaceNode()
     */
    final static public boolean isTmxWhitespaceNode(String p_type)
    {
        if (p_type == null || p_type.length() == 0)
        {
            return false;
        }

        // Treat <PH type=nbsp> and HTML <BR> as white.
        if (p_type.equals("x-nbspace") || p_type.equals("x-br"))
        {
            return true;
        }

        return false;
    }

    /**
     * Removes CR/NL from a string.
     */
    final static public String removeCRNL(String s)
    {
        int i_pos;

        if ((i_pos = s.indexOf('\n')) > 0 || (i_pos = s.indexOf('\r')) > 0)
        {
            StringBuffer temp = new StringBuffer(s);
            int i_at = i_pos - 1;
            int i_max = temp.length();

            while (i_at < i_max)
            {
                if (temp.charAt(i_at) == '\n' || temp.charAt(i_at) == '\r')
                {
                    temp.deleteCharAt(i_at);
                    --i_max;
                    continue;
                }
                ++i_at;
            }

            return temp.toString();
        }

        return s;
    }

    /**
     * Returns the string without surrounding single or double quotes, if it had
     * any.
     * 
     * @param p_string
     *            the string that may have quotes.
     */
    final static public String removeQuotes(String p_string)
    {
        int len;

        if (p_string != null && (len = p_string.length()) >= 2)
        {
            if ((p_string.charAt(0) == '"' && p_string.charAt(len - 1) == '"')
                    || (p_string.charAt(0) == '\'' && p_string.charAt(len - 1) == '\''))
            {
                return p_string.substring(1, len - 1);
            }
        }

        return p_string;
    }

    /**
     * Test if the string contains bidi characters.
     * 
     * @param p_string
     *            String to be tested.
     * @return <code>true</code> if the string contains bidi characters,
     *         <code>false</code> otherwise.
     */
    final static public boolean containsBidiChar(String p_string)
    {
        for (int i = 0, max = p_string.length(); i < max; i++)
        {
            char ch = p_string.charAt(i);

            if (isBidiChar(ch))
            {
                return true;
            }
        }

        return false;
    }

    final static public boolean isBidiChar(char p_ch)
    {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(p_ch);
        if (block == null)
        {
            return false;
        }
        else if (block.equals(Character.UnicodeBlock.ARABIC)
                || block.equals(Character.UnicodeBlock.HEBREW)
                || block.equals(Character.UnicodeBlock.ALPHABETIC_PRESENTATION_FORMS)
                || block.equals(Character.UnicodeBlock.ARABIC_PRESENTATION_FORMS_A)
                || block.equals(Character.UnicodeBlock.ARABIC_PRESENTATION_FORMS_B))
        {
            return true;
        }

        return false;
    }

    /**
     * Replaces all occurences of p_old with p_new in p_string.
     * 
     * @param p_string
     *            original String
     * @param p_old
     *            the string to be replaced
     * @param p_new
     *            the string replacing p_old
     * @return a processed String
     */
    final static public String replaceString(String p_string, String p_old, String p_new)
    {
        return replaceString(p_string, p_string, p_old, p_new);
    }

    /**
     * Replaces all occurences of p_old with p_new in p_string by ignoring case
     * difference when searching for p_old in p_string.
     * 
     * @param p_string
     *            original String
     * @param p_old
     *            the string to be replaced
     * @param p_new
     *            the string replacing p_old
     * @param p_locale
     *            locale of which rule is used when ignoring case
     * @return a processed String
     */
    final static public String replaceStringIgnoreCase(String p_string, String p_old, String p_new,
            Locale p_locale)
    {
        return replaceString(p_string, p_string.toLowerCase(p_locale), p_old.toLowerCase(p_locale),
                p_new);
    }

    /**
     * p_caseNormalized is a case normalized p_original. p_old string is also
     * case normalized. Find occurences of p_old in p_caseNormalized and replace
     * it with p_new, but copy non replaced part from p_original to a resulting
     * String.
     */
    final static private String replaceString(String p_original, String p_caseNormalized,
            String p_old, String p_new)
    {
        StringBuffer buff = new StringBuffer();
        int oldStrLength = p_old.length();
        int index = 0;
        int prevIndex = 0;

        while ((index = p_caseNormalized.indexOf(p_old, prevIndex)) != -1)
        {
            buff.append(p_original.substring(prevIndex, index));
            buff.append(p_new);
            prevIndex = index + oldStrLength;
        }

        buff.append(p_original.substring(prevIndex, p_original.length()));

        return buff.toString();
    }

    /**
     * replace the first occurance of p_old in p_original with p_new
     */
    final static public String replaceStringFirst(String p_original, String p_old, String p_new)
    {
        int index = 0;
        String result = null;

        if ((index = p_original.indexOf(p_old)) != -1)
        {
            StringBuffer buff = new StringBuffer();

            buff.append(p_original.substring(0, index));
            buff.append(p_new);
            buff.append(p_original.substring(index + p_old.length(), p_original.length()));
            result = buff.toString();
        }
        else
        {
            result = p_original;
        }

        return result;
    }

    /**
     * Replaces characters in a given string. This method replaces all
     * occurences of p_old in p_string with p_new and returns the resulting
     * string. If p_old is followed by p_escape, the two characters are replaced
     * with one p_old (e.g. '\*' -> '*').
     * 
     * @param p_string
     *            original string
     * @param p_old
     *            the character to be replaced
     * @param p_new
     *            the character to replace old one
     * @param p_escape
     *            the character that escapes the old character
     * @return a processed String
     */
    final static public String replaceChar(String p_string, char p_old, char p_new, char p_escape)
    {
        StringBuffer buff = new StringBuffer();

        for (int i = 0, max = p_string.length(); i < max; i++)
        {
            char current = p_string.charAt(i);
            char toBeAdded;

            if (current == p_escape && i + 1 < max && p_string.charAt(i + 1) == p_old)
            {
                toBeAdded = p_old;
                i++;
            }
            else if (current == p_old)
            {
                toBeAdded = p_new;
            }
            else
            {
                toBeAdded = current;
            }

            buff.append(toBeAdded);
        }

        return buff.toString();
    }

    /**
     * Remove leading whitespaces from a given string. To determine if
     * characters are whitespace, Character.isWhitespace() method is used.
     * 
     * @param p_string
     *            a string leading spaces are removed from.
     * @return String object of which the leading spaces are trimed.
     */
    final static public String removeLeadingSpaces(String p_string)
    {
        String removed = null;

        for (int i = 0, max = p_string.length(); i < max; i++)
        {
            char ch = p_string.charAt(i);

            if (!Character.isWhitespace(ch))
            {
                removed = p_string.substring(i);
                break;
            }
        }

        if (removed == null)
        {
            removed = new String();
        }

        return removed;
    }

    /**
     * Remove trailing whitespaces from a given string. To determine if
     * characters are whitespace, Character.isWhitespace() method is used.
     * 
     * @param p_string
     *            a string trailing spaces are removed from.
     * @return String object of which the trailing spaces are trimed.
     */
    final static public String removeTrailingSpaces(String p_string)
    {
        String removed = null;
        int strLen = p_string.length();

        for (int i = 0; i < strLen; i++)
        {
            char ch = p_string.charAt(strLen - i - 1);

            if (!Character.isWhitespace(ch))
            {
                removed = p_string.substring(0, strLen - i);
                break;
            }
        }

        if (removed == null)
        {
            removed = new String();
        }

        return removed;
    }

}
