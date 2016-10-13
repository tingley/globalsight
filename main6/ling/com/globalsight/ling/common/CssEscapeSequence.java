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

import com.globalsight.ling.common.NativeEnDecoder;
import com.globalsight.ling.common.NativeEnDecoderException;

import com.globalsight.ling.common.Text;


/**
 * <P>Implementation of escape sequence encoding and decoding for CSS.
 * See http://www.w3.org/TR/REC-CSS2/syndata.html.</P>
 *
 * @see NativeEnDecoder
 */
public class CssEscapeSequence
    extends NativeEnDecoder
{
    //
    // Implementation of Interface -- NativeEnDecoder
    //

    public String decode (String p_str)
        throws NativeEnDecoderException
    {
        return decodeString(p_str);
    }

    public String decode(String p_str, String p_outerQuote)
        throws NativeEnDecoderException
    {
        throw notApplicable();
    }

    public String encode(String p_str)
        throws NativeEnDecoderException
    {
        return encodeString(p_str);
    }

    public String encode(String p_str, String p_outerQuote)
        throws NativeEnDecoderException
    {
        throw notApplicable();
    }

    public String encodeWithEncodingCheck(String p_NativeString)
        throws NativeEnDecoderException
    {
        // CSS is an ASCII format, so we encode regardless of target charset
        return encodeString(p_NativeString);
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
     * <P>Converts escaped unicode (\xxxxxx) to unicode chars.  String
     * quoting characters (" and ') are left alone, quoted quotes are
     * output as is (\" and \'), and unicode escapes that would
     * produce quotes are not decoded either.</P>
     *
     * <P>For more information about the ambiguity of CSS escape rules
     * and the slick CSS1/CSS2 differences, see the CSS2 spec,
     * Appendix D.</P>
     *
     * @see NativeEnDecoder#decode(String)
     */
    public static String decodeString (String p_str)
    {
        int len;
        StringBuffer result;

        if (p_str.indexOf('\\') == -1)
        {
            return p_str;
        }

        len = p_str.length();
        result = new StringBuffer (len);

        for (int x = 0; x < len; )
        {
            char aChar = p_str.charAt(x++);

            if (aChar == '\\')
            {
                aChar = p_str.charAt(x++);

                switch (aChar)
                {
                    case '0': case '1': case '2': case '3': case '4':
                    case '5': case '6': case '7': case '8': case '9':
                    case 'a': case 'A': case 'b': case 'B':
                    case 'c': case 'C': case 'd': case 'D':
                    case 'e': case 'E': case 'f': case 'F':
                    {
                        int start = x - 1;
                        int cnt = 1;

                        // CSS allows a maximum of 6 hex digits
                        while (x < len && cnt < 6 &&
                          (Character.digit(p_str.charAt(x), 16) != -1))
                        {
                            ++x;
                            ++cnt;
                        }

                        // since Java chars are only 16 bit values, the
                        // string is carefully converted and truncated when
                        // necessary
                        int num = Integer.parseInt(
                          p_str.substring(x - cnt, x), 16);

                        // TODO: remove this line when Java chars are
                        // 32 bit wide
                        num = num & Character.MAX_VALUE;
                        aChar = (char)num;

                        // CSS2 unicode escapes can be terminated by 1
                        // whitespace character, which we eat now. Yumyum.
                        if (x < len && Text.isSpace(p_str.charAt(x)))
                        {
                            ++x;
                        }

                        // Check if this creates a quote character
                        if (aChar == '\'' || aChar == '"')
                        {
                            // if so, output original text
                            result.append('\\');
                            result.append(p_str.substring(start, x));
                            continue;
                        }

                        result.append(aChar);
                        break;
                    }
                    default:
                        // the \ just escaped this character, leave untouched
                        result.append('\\');
                        result.append(aChar);
                        break;
                }
            }
            else
            {
                result.append(aChar);
            }
        }

        return result.toString();
    }


    /**
     * <P>Encodes standard character escapes and converts characters
     * below 32 and above 127 into unicode escapes (\\xxxx).</P>
     *
     * <p>If the input string is a css token that is actually itself
     * a string, i.e. starts and ends with a " or ' character, those
     * characters are output as is.</p>
     *
     * @see NativeEnDecoder#encode(String)
     */
    public static String encodeString(String p_str)
    {
        int len = p_str.length();
        int start = 0;
        StringBuffer result = new StringBuffer(len*2);
        boolean isString = false;

        for (int x = start; x < len; )
        {
            char aChar = p_str.charAt(x++);

            switch (aChar)
            {
//              case '\\': result.append('\\'); result.append('\\');
//                  break;
                case '\t': result.append('\\'); result.append('t');
                    break;
                case '\n': result.append('\\'); result.append('n');
                    break;
                case '\r': result.append('\\'); result.append('r');
                    break;
                case '\f': result.append('\\'); result.append('f');
                    break;
                default:
                    if ((aChar < 32) || (aChar > 127))
                    {
                        result.append('\\');
                        result.append(Integer.toHexString(aChar));

                        // add a blank to disambiguate the end
                        if (x < len && Text.isHexDigit(p_str.charAt(x)))
                        {
                            result.append(' ');
                        }
                    }
                    else
                    {
                        result.append(aChar);
                    }
            }
        }

        return result.toString();
    }


    /**
     * <p>Removes embedded "\\n" sequences in CSS strings.  This is
     * according to the CSS standard and allows long strings to span
     * multiple lines, very much like shell line continuations.</p>
     */
    public static String cleanupStringToken(String p_str)
    {
        StringBuffer result;
        int len = p_str.length();
        int start = 0;
        boolean isString = false;

        if (len >= 2)
        {
            isString =
              (p_str.charAt(0) == '"' && p_str.charAt(len-1) == '"') ||
              (p_str.charAt(0) == '\'' && p_str.charAt(len-1) == '\'');
        }

        if (!isString)
        {
            // not a string, nothing to do
            return p_str;
        }

        result = new StringBuffer(len);
        result.append(p_str.charAt(0));

        len = len -1;
        for (int x = 1; x < len; ++x)
        {
            char aChar = p_str.charAt(x);

            switch (aChar)
            {
                case '\\':
                    if ((x + 2 < len) && (p_str.charAt(x+1) == '\r') &&
                      (p_str.charAt(x+2) == '\n'))
                    {
                        // skip \ followed by Windows EOL \r\n
                        ++x;
                        ++x;
                        continue;
                    }
                    if ((x + 1 < len) && (p_str.charAt(x+1) == '\n' ||
                      (p_str.charAt(x+1) == '\r')))
                    {
                        // skip \ followed by Unix EOL \n or Mac EOL \r
                        ++x;
                        continue;
                    }

                    result.append(aChar);
                    break;
                default:
                    result.append(aChar);
            }
        }

        result.append(p_str.charAt(0));

        return result.toString();
    }

}
