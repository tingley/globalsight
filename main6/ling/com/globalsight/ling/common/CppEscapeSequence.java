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

/**
 * <P>
 * Implementation of encoding and decoding escape sequences for C++.
 * </P>
 *
 * @see NativeEnDecoder
 */
public class CppEscapeSequence extends NativeEnDecoder
{
    //
    // Private Transient Member Variables
    //

    private static final Hashtable mCharToEscape = mapCharToEscape();
    private static final Hashtable mEscapeToChar = mapEscapeToChar();

    private static final String NCR_BEGIN = "\\x";
    private static final char[] ZERO_ARRAY =
    { '0', '0', '0', '0' };
    private static final int HEX_DIGIT = 4;

    private static RE m_pattern = null;
    static
    {
        try
        {
            m_pattern = new RE(
                    "\\\\(\\\\|t|n|r|[uU][0-9a-fA-F]{4}|[xX][0-9a-fA-F]{2,4}|0[0-7]{0,3}|.)",
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

    // other way round... later...

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

    public String encodeWithEncodingCheck(String p_str)
            throws NativeEnDecoderException
    {
        String s = encodeString(p_str);
        StringBuffer sbuf = new StringBuffer();

        // If there are characters that cannot be converted to the
        // specified encoding, they will be converted to character
        // references.
        for (int i = 0; i < s.length(); ++i)
        {
            char c = s.charAt(i);
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
     * characters. Escape sequences that will be decoded are hexadecimal
     * sequences and octal sequences.
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
                char tmp = es.charAt(0);

                // See http://www.csci.csusb.edu/dick/c++std/cd2/lex.html.
                // Unicode sequences are allowed.
                if (tmp == 'u' || tmp == 'U') // UnicodeEscapeSequence
                {
                    System.err.println(es);
                    c = decodeUnicodeSequence(es);
                }
                else if (tmp == 'x' || tmp == 'X') // HexEscapeSequence
                {
                    System.err.println(es);
                    c = decodeHexSequence(es);
                }
                else if (tmp == '0') // OctalEscapeSequence
                {
                    c = decodeOctalSequence(es);
                }

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

        // System.err.println("d " + s);

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

        // System.err.println("D " + ret.toString());

        return ret.toString();
    }

    /**
     * <P>
     * Encodes characters in strings to C++ escape sequences. Characters that
     * will always be encoded are \b,\f,\t,\n,\r,\v.
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

        // System.err.println(s);

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
                ret.append('\\'); // ret.append('\\');
                b_escaped = false;
            }

            ret.append(ch);
        }

        s = ret.toString();
        ret = new StringBuffer();

        // System.err.println("e " + s);

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
                if (c.charValue() > 255)
                {
                    String hex = Integer.toHexString(c.charValue());
                    ret.append("\\x");
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

        // System.err.println("E " + ret.toString());

        return ret.toString();
    }

    //
    // Private Methods
    //

    private static Character decodeHexSequence(String s)
    {
        Character c;
        int i = s.charAt(0) == 'x' ? 1 : (s.charAt(0) == 'X' ? 1 : 0);

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
        int i = s.charAt(0) == 'u' ? 1 : (s.charAt(0) == 'U' ? 1 : 0);

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
        h.put(new Character('\n'), "\\n");
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
        h.put(new Character('n'), new Character('\n'));
        h.put(new Character('r'), new Character('\r'));
        h.put(new Character('t'), new Character('\t'));
        // h.put(new Character('v'), new Character('\u000b'));

        return h;
    }

    //
    // Test code
    //

    /********************************************************
     * public static void main(String[] args) { String s;
     * 
     * System.out.println(CppEscapeSequence.decodeString(
     * "\\u00c0\\x21\\1000abcd\\\\\\'\\\"\\b\\f\\n\\r\\tefg\\h"));
     * System.out.println(CppEscapeSequence.encodeString(
     * "abcd\\'\"\b\f\n\r\tefgh"));
     * 
     * s = "abcd\\'\"\b\f\n\r\tefgh"; if
     * (CppEscapeSequence.decodeString(CppEscapeSequence.encodeString(s))
     * .compareTo(s) == 0) { System.out.println("roundtrip 1 ok"); } else {
     * System.out.println("roundtrip 1 failed"); }
     * 
     * s = "\\u00c0\\x21\\1000abcd\\\\\\'\\\"\\b\\f\\n\\r\\tefg\\h"; if
     * (CppEscapeSequence.decodeString(CppEscapeSequence.encodeString(s))
     * .compareTo(s) == 0) { System.out.println("roundtrip 2 ok"); } else {
     * System.out.println("roundtrip 2 failed"); } }
     * 
     * public static void main(String[] args) throws NativeEnDecoderException,
     * java.io.UnsupportedEncodingException { String s =
     * "test \" ' \u00a0 \u00a9 \u2122 \u00ae \u0367 test"; EncodingChecker
     * checker = new EncodingChecker(args[0]); CppEscapeSequence encoder = new
     * CppEscapeSequence(); encoder.setEncodingChecker(checker);
     * 
     * System.out.println(encoder.encode(s)); }
     * 
     ********************************************************
     * 
     * public static void main(String[] args) { String s, s1, s2;
     * 
     * s = "doit('if (\\'1\\'==\\'1\\') {1;} else {2;}')";
     * System.err.println("S " + s); s1 = CppEscapeSequence.decodeString(s);
     * System.err.println("D " + s1); s2 = CppEscapeSequence.encodeString(s1);
     * System.err.println("E " + s2); if (s2.compareTo(s) == 0) {
     * System.err.println("roundtrip 3 ok"); } else {
     * System.err.println("roundtrip 3 failed"); }
     * 
     * s = "doit('if (\\'1\\'==\\'1\\') {1;} else {2;}'); return \"1\"";
     * System.err.println("S " + s); s1 = CppEscapeSequence.decodeString(s);
     * System.err.println("D " + s1); s2 = CppEscapeSequence.encodeString(s1);
     * System.err.println("E " + s2); if (s2.compareTo(s) == 0) {
     * System.err.println("roundtrip 4 ok"); } else {
     * System.err.println("roundtrip 4 failed"); }
     * 
     * s = "\\u00c0\\x21\\1000abcd\\\\\\'\\\"\\b\\f\\n\\r\\tefg\\h\\y";
     * System.err.println("S " + s); s1 = CppEscapeSequence.decodeString(s);
     * System.err.println("D " + s1); s2 = CppEscapeSequence.encodeString(s1);
     * System.err.println("E " + s2); if (s2.compareTo(s) == 0) {
     * System.err.println("roundtrip 5 ok"); } else {
     * System.err.println("roundtrip 5 failed"); } }
     *******************************************************/
}
