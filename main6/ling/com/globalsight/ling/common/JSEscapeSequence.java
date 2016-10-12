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
package com.globalsight.ling.common;

import java.util.Hashtable;

import com.sun.org.apache.regexp.internal.RE;
import com.sun.org.apache.regexp.internal.RESyntaxException;

//jakarta regexp package

/**
 * <P>
 * Implementation of encoding and decoding escape sequences for JavaScript. Note
 * that this is now very specific to extracting JS strings for translation and
 * for merging them back into the skeleton. The only supported sequence of
 * events is ENcode(DEcode(s)).
 * </P>
 *
 * @see NativeEnDecoder
 */
public class JSEscapeSequence extends NativeEnDecoder
{
    //
    // Private Transient Member Variables
    //

    private static final Hashtable mCharToEscape = mapCharToEscape();
    private static final Hashtable mEscapeToChar = mapEscapeToChar();

    private static final String NCR_BEGIN = "\\u";
    private static final char[] ZERO_ARRAY =
    { '0', '0', '0', '0' };
    private static final int HEX_DIGIT = 4;

    private static RE m_pattern = null;
    static
    {
        try
        {
            m_pattern = new RE("\\\\(\\\\|u[0-9a-fA-F]{4}|x[0-9a-fA-F]{2})",
                    RE.MATCH_NORMAL);
        }
        catch (RESyntaxException e)
        {
            System.err.println("Pilot error in regexp: " + e);
        }
    }

    //
    // Implementation of Interface -- NativeEnDecoder
    //

    public String decode(String p_str) throws NativeEnDecoderException
    {
        return decodeString(p_str);
    }

    public String decode(String p_str, String p_outerQuote)
            throws NativeEnDecoderException
    {
        throw notApplicable();
    }

    public String encode(String p_str) throws NativeEnDecoderException
    {
        return encodeString(p_str);
    }

    public String encode(String p_str, String p_outerQuote)
            throws NativeEnDecoderException
    {
        throw notApplicable();
    }

    public String encodeWithEncodingCheckForSkeleton(String p_nativeString)
            throws NativeEnDecoderException
    {
        return encodingCheck(p_nativeString);
    }

    public String encodeWithEncodingCheck(String p_nativeString)
            throws NativeEnDecoderException
    {
        String s = encodeString(p_nativeString);
        return encodingCheck(s);
    }

    private String encodingCheck(String p_str) throws NativeEnDecoderException
    {
        StringBuffer sbuf = new StringBuffer();

        // If there are characters that cannot be converted to the
        // specified encoding, they will be converted to character
        // references.
        for (int i = 0; i < p_str.length(); ++i)
        {
            char c = p_str.charAt(i);
            if (encChecker.canConvert(c))
            {
                sbuf.append(c);
            }
            else
            {
                sbuf.append(NCR_BEGIN);
                String hex = Integer.toHexString(c);
                int len = hex.length();
                if (len < HEX_DIGIT)
                {
                    sbuf.append(ZERO_ARRAY, 0, HEX_DIGIT - len);
                }
                sbuf.append(hex);
            }
        }

        return sbuf.toString();
    }

    public String encodeWithEncodingCheck(String p_str, String p_outerQuote)
            throws NativeEnDecoderException
    {
        throw notApplicable();
    }

    //
    // Public Methods
    //

    /**
     * <P>
     * Decodes escape sequences in a string and converts them to unicode
     * characters. Escape sequences that will be decoded are
     * CharacterEscapeSequence, HexEscapeSequence and UnicodeEscapeSequence
     * defined in chapter 7.8.4 in Standard ECMA-262 (ECMAScript Language
     * Specification).
     * </P>
     *
     * <P>
     * The exception to this is that protected quotes inside the same type of
     * quotes are <STRONG>not</STRONG> decoded.
     * </P>
     *
     * <P>
     * Example: <CODE>s = "eval('return \'1\' == \'2\'');"</CODE>. will remain
     * the same after being subjected to this function.
     * </P>
     *
     * <P>
     * We also ignore \b\f\v since they won't make it through the XML parsers.
     * If a Unicode sequence maps to these or any other control characters,
     * tough luck, the DiplomatAPI will catch the error.
     * </P>
     *
     * @param s
     *            string to be decoded
     * @return decoded string
     */
    public static String decodeString(String s)
    {
        // Quick exit: nothing to do
        if (s.indexOf('\\') == -1)
        {
            return s;
        }

        // PASS 1: get normal escapes out of the way and don't touch
        // quotes and escaped quotes

        int last_index = 0;
        StringBuffer ret = new StringBuffer();
        // copy RE to make it thread-safe
        RE pattern = new RE(m_pattern.getProgram());

        while (pattern.match(s, last_index))
        {
            ret.append(s.substring(last_index, pattern.getParenStart(0)));
            last_index = pattern.getParenEnd(0);

            String es = pattern.getParen(1);
            Character c = (Character) mEscapeToChar.get(new Character(es
                    .charAt(0)));

            if (c == null)
            {
                if (es.charAt(0) == 'u') // UnicodeEscapeSequence
                {
                    c = decodeUnicodeSequence(es);
                }
                else if (es.charAt(0) == 'x') // HexEscapeSequence
                {
                    c = decodeHexSequence(es);
                }
                /*
                 * JS knows no Octal escapes - conflict with regexps else //
                 * OctalEscapeSequence { c = decodeOctalSequence(es); }
                 */

                if (c == null)
                {
                    c = new Character(es.charAt(0));
                    ret.append('\\');
                    ret.append(c);

                    // leave protected chars as is
                    continue;
                }

            }

            char ch = c.charValue();
            if (ch == '\\' || ch == '\'' || ch == '\"') // don't decode \,',"
            {
                ret.append('\\'); // so: output backslash again
            }

            ret.append(c);
        }

        ret.append(s.substring(last_index));

        // PASS 2: deal with the quotes
        s = ret.toString();
        ret = new StringBuffer();

        boolean b_escaped = false;
        char theQuote = '\0';
        char ch;
        for (int i = 0; i < s.length(); ++i)
        {
            ch = s.charAt(i);
            if (ch == '\\')
            {
                if (!b_escaped)
                {
                    b_escaped = true;
                    continue; // output later
                }
                else
                {
                    ret.append('\\');
                    b_escaped = false;
                }
            }
            else if (ch == '\'' || ch == '\"')
            {
                if (theQuote == '\0' && !b_escaped)
                {
                    theQuote = ch;
                }
                else if (ch == theQuote && b_escaped)
                {
                    ret.append('\\');
                    b_escaped = false;
                }
                else if (ch == theQuote && !b_escaped)
                {
                    theQuote = '\0';
                }
                else if (b_escaped)
                {
                    ret.append('\\');
                    b_escaped = false;
                }
                else
                {
                    // System.err.println("OUCH: " + ch);
                }
            }
            else if (b_escaped)
            {
                ret.append('\\');
                b_escaped = false;
            }

            ret.append(ch);
        }

        return ret.toString();
    }

    /**
     * <P>
     * Encodes characters in strings to JavaScript escape sequences. Characters
     * that will always be encoded are \b,\f,\t,\n,\r,\v, as defined in chapter
     * 7.7.4 in Standard ECMA-262 (ECMAScript Language Specification).
     * </P>
     *
     * <P>
     * The characters \,'," (backslash, apostrophe, quote) will only be encoded
     * if they produce a valid output string, one that is parseable without
     * errors.
     * </P>
     *
     * @param s
     *            string to be encoded
     * @return encoded string
     */
    public static String encodeString(String s)
    {
        StringBuffer ret = new StringBuffer();

        boolean b_escaped = false;
        // char theQuote = '\0';
        char ch;
        for (int i = 0; i < s.length(); ++i)
        {
            ch = s.charAt(i);
            if (ch == '\\')
            {
                if (!b_escaped)
                {
                    b_escaped = true;
                    continue; // output later
                }
                else
                {
                    ret.append('\\');
                    b_escaped = false;
                }
            }
            else if (ch == '\'' || ch == '\"')
            {
                ret.append('\\');
                b_escaped = false;

                // else if (ch == '\'' || ch == '\"')
                // {
                // if (theQuote == '\0' && !b_escaped)
                // {
                // theQuote = ch;
                // }
                // else if (ch == theQuote && b_escaped)
                // {
                // ret.append('\\'); b_escaped = false;
                // }
                // else if (ch == theQuote && !b_escaped)
                // {
                // theQuote = '\0';
                // }
                // else if (b_escaped)
                // {
                // ret.append('\\'); b_escaped = false;
                // }
                // else
                // {
                // // System.err.println("OUCH: " + ch);
                // }
            }
            else if (b_escaped)
            {
                ret.append('\\');
                b_escaped = false;
            }

            ret.append(ch);
        }

        s = ret.toString();
        ret = new StringBuffer();
        for (int i = 0; i < s.length(); ++i)
        {
            Character c = new Character(s.charAt(i));
            String es = (String) mCharToEscape.get(c);

            if (es != null)
            {
                ret.append(es);
            }
            else
            {
                // keep same behaviour with JPEscapeSequence
                if (c.charValue() < 20 || c.charValue() > 127)
                {
                    String hex = Integer.toHexString(c.charValue());
                    ret.append("\\u");
                    int len = hex.length();
                    if (len < HEX_DIGIT)
                    {
                        ret.append(ZERO_ARRAY, 0, HEX_DIGIT - len);
                    }
                    ret.append(hex);
                }
                else
                {
                    ret.append(c);
                }
            }
        }

        return ret.toString();
    }

    //
    // Private Methods
    //
    private static Character decodeHexSequence(String s)
    {
        Character c;
        int i = s.charAt(0) == 'x' ? 1 : 0;

        try
        {
            c = new Character((char) Integer.parseInt(s.substring(i), 16));
        }
        catch (NumberFormatException e)
        {
            c = null;
        }

        return c;
    }

    private static Character decodeOctalSequence(String s)
    {
        Character c;

        try
        {
            c = new Character((char) Integer.parseInt(s, 8));
        }
        catch (NumberFormatException e)
        {
            c = null;
        }

        return c;
    }

    private static Character decodeUnicodeSequence(String s)
    {
        Character c;
        int i = s.charAt(0) == 'u' ? 1 : 0;

        try
        {
            c = new Character((char) Integer.parseInt(s.substring(i), 16));
        }
        catch (NumberFormatException e)
        {
            c = null;
        }

        return c;
    }

    private static Hashtable mapCharToEscape()
    {
        Hashtable h = new Hashtable();

        // h.put(new Character('\\'), "\\\\");
        h.put(new Character('\b'), "\\b");
        h.put(new Character('\u000c'), "\\f");
        h.put(new Character('\r'), "\\r");
        h.put(new Character('\t'), "\\t");
        h.put(new Character('\u000b'), "\\v");

        return h;
    }

    private static Hashtable mapEscapeToChar()
    {
        Hashtable h = new Hashtable();

        // Don't map chars that are illegal in XML

        h.put(new Character('\\'), new Character('\\'));
        // h.put(new Character('b'), new Character('\b'));
        // h.put(new Character('f'), new Character('\u000c'));
        h.put(new Character('r'), new Character('\r'));
        h.put(new Character('t'), new Character('\t'));
        // h.put(new Character('v'), new Character('\u000b'));

        return h;
    }
}
