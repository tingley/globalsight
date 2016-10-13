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

/**
 * Encodes and decodes Java Properties escape sequences.
 */
public class JPEscapeSequence extends NativeEnDecoder
{
    private boolean isJavaProperty = false;

    /*
     * Converts escaped unicode (\\uxxxx) to unicode chars, and standard escape
     * sequences to their native forms.
     */
    public String decode(String p_str) throws NativeEnDecoderException
    {
        char aChar;
        Character c;

        if (p_str.indexOf('\\') < 0)
        {
            return p_str;
        }

        int len = p_str.length();
        StringBuffer result = new StringBuffer(len);

        for (int x = 0; x < len;)
        {
            aChar = p_str.charAt(x++);
            if (aChar == '\\')
            {
                aChar = p_str.charAt(x++);
                if (aChar == 'u') // unicode escapes
                {
                    try
                    {
                        c = new Character((char) Integer.parseInt(
                                p_str.substring(x, x + 4), 16));
                        result.append(c);
                        x += 4;
                    }
                    catch (Exception e)
                    {
                        throw new NativeEnDecoderException(e.getMessage());
                    }
                }
                else
                {
                    if (aChar == 't' || aChar == 'r' || aChar == 'n'
                            || isJavaProperty)
                    {
                        result.append("\\");
                        result.append(aChar);
                    }
                    else if (aChar == 'f')
                    {
                        result.append('\f');// same as before
                    }
                }
            }
            else
            {
                result.append(aChar);
            }
        }

        return result.toString();
    }

    public String decode(String p_str, String p_outerQuote)
            throws NativeEnDecoderException
    {
        throw notApplicable();
    }

    /**
     * Encodes standard character escapes and converts characters below 20 and
     * above 127 into unicode escapes (\\uxxxx). Note: The generic Merger should
     * have removed all TMX and XML escapes.
     */
    public String encode(String p_str)
    {
        char aChar;
        int len = p_str.length();
        boolean b_leading = true;
        StringBuffer result = new StringBuffer(len * 2);

        for (int x = 0; x < len;)
        {
            aChar = p_str.charAt(x++);

            if (b_leading && (aChar != ' '))
            {
                b_leading = false;
            }

            switch (aChar)
            {
            // note jdk 1.2.2 interprets \b as literal char 'b'

            // Leading spaces need to be escaped
                case ' ':
                    if (b_leading && getLastChar() != null
                            && "=:".contains(getLastChar()))
                    {
                        result.append('\\');
                        b_leading = false;
                    }
                    result.append(' ');
                    continue;
                case '\t':
                case '\n':
                case '\r':
                case '\f':
                    result.append(aChar);
                    continue;
                case '\\':
                    result.append('\\');

                    boolean thisIsEscapeChar = false;
                    if (x < len)
                    {
                        char nextc = p_str.charAt(x);

                        if (":=\\".contains("" + nextc))
                        {
                            thisIsEscapeChar = true;
                        }
                    }
                    if (x >= 2)
                    {
                        char prec = p_str.charAt(x - 2);

                        if ("\\".contains("" + prec))
                        {
                            thisIsEscapeChar = true;
                        }
                    }
                    if (thisIsEscapeChar && !isJavaProperty)
                    {
                        result.append('\\');
                    }
                    continue;

                    /*
                     * case '#': result.append('\\'); result.append('#');
                     * continue; case '!': result.append('\\');
                     * result.append('!'); continue; case '=':
                     * result.append('\\'); result.append('='); continue; case
                     * ':': result.append('\\'); result.append(':'); continue;
                     * case '\"':result.append('\\'); result.append('\"');
                     * continue; case '\'':result.append('\\');
                     * result.append('\''); continue;
                     */
                default:
                    if ((aChar < 20) || (aChar > 127))
                    {
                        String s = new String(Integer.toHexString(aChar));
                        switch (s.length())
                        {
                            case 1:
                                s = "\\u000" + s;
                                break;
                            case 2:
                                s = "\\u00" + s;
                                break;
                            case 3:
                                s = "\\u0" + s;
                                break;
                            case 4:
                                s = "\\u" + s;
                                break;
                            default:
                        }
                        result.append(s);
                    }
                    else
                    {
                        result.append(aChar);
                    }
            }
        }

        return result.toString();
    }

    public String encode(String p_str, String p_outerQuote)
            throws NativeEnDecoderException
    {
        throw notApplicable();
    }

    public String encodeWithEncodingCheck(String p_NativeString)
            throws NativeEnDecoderException
    {
        return encode(p_NativeString);
    }

    public String encodeWithEncodingCheck(String p_str, String p_outerQuote)
            throws NativeEnDecoderException
    {
        throw notApplicable();
    }

    public void setIsJavaProperty(boolean flag)
    {
        isJavaProperty = flag;
    }
}
