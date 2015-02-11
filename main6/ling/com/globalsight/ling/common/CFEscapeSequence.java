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

/**
 * <P>Implementation of encoding and decoding escape sequences for
 * CFScript (ColdFusion).</P>
 *
 * @see http://www.macromedia.com/support/coldfusion/internationalization.html
 *
 * @see NativeEnDecoder
 */
public class CFEscapeSequence
    extends NativeEnDecoder
{
    //
    // Implementation of Interface -- NativeEnDecoder
    //

    public String decode (String p_str)
        throws NativeEnDecoderException
    {
        throw notApplicable();
    }

    public String decode (String p_str, String p_outerQuote)
        throws NativeEnDecoderException
    {
        return decodeString(p_str, p_outerQuote);
    }

    public String encode(String p_str)
        throws NativeEnDecoderException
    {
        throw notApplicable();
    }

    public String encode(String p_str, String p_outerQuote)
        throws NativeEnDecoderException
    {
        return encodeString(p_str, p_outerQuote);
    }


    public String encodeWithEncodingCheck(String p_str)
        throws NativeEnDecoderException
    {
        return encodeWithEncodingCheck(p_str, "\uffff");
    }

    public String encodeWithEncodingCheck(String p_str, String p_outerQuote)
        throws NativeEnDecoderException
    {
        String ret = encodeString(p_str, p_outerQuote);

        // Check that all characters can be converted correctly. If
        // not, there is no way to encode them. Tell user to select
        // the correct output encoding (use of UTF-8 is recommended).
        for (int i = 0; i < ret.length(); ++i)
        {
            char c = ret.charAt(i);

            if (encChecker.canConvert(c))
            {
                continue;
            }

            throw new NativeEnDecoderException(
                "Illegal character: U+" + Integer.toHexString(c) +
                " for " + encChecker.getEncoding());
        }

        return ret;
    }


    //
    // Public Methods
    //

    /**
     * <P>Decodes CFScript strings by collapsing double single quotes
     * or double double quotes, and double hashs (',",#).</P>
     *
     * Note: only the quote equal to the outer quote can be doubled,
     * not the other one. E.g. " "" ''" == " ''.
     */
    public static String decodeString(String s, String p_outerQuote)
    {
        StringBuffer ret = new StringBuffer();

        // System.err.println("In  " + s);

        char theQuote = '\0';
        char outerQuote = p_outerQuote.charAt(0);
        for (int i = 0; i < s.length(); ++i)
        {
            char ch = s.charAt(i);

            if (ch == '\'' || ch == '\"' || ch == '#')
            {
                if (theQuote == '\0')
                {
                    // only outer quote can (must) be doubled
                    if (ch == outerQuote || ch == '#')
                    {
                        theQuote = ch;
                    }
                    // the other quote is just the single char
                    else
                    {
                        ret.append(ch);
                        theQuote = '\0';
                    }
                }
                else if (theQuote == ch)
                {
                    ret.append(theQuote);
                    theQuote = '\0';
                }
                else /*if (theQuote != ch)*/
                {
                    ret.append(theQuote);
                    theQuote = ch;
                }
            }
            else
            {
                if (theQuote != '\0')
                {
                    ret.append(theQuote);
                }

                theQuote = '\0';

                ret.append(ch);
            }
        }

        if (theQuote != '\0')
        {
            ret.append(theQuote);
        }

        // System.err.println("Out " + ret.toString());

        return ret.toString();
    }

    /**
     * <P>Encodes characters in strings to CFScript escape sequences.
     * Single quotes, double quotes, and hash will be escaped by
     * doubling them.</P>
     *
     * @param s string to be encoded
     * @return encoded string
     */
    public static String encodeString(String s, String p_outerQuote)
    {
        StringBuffer ret = new StringBuffer();

        // System.err.println("In  " + s);

        char outerQuote = p_outerQuote.charAt(0);
        for (int i = 0; i < s.length(); ++i)
        {
            char ch = s.charAt(i);

            if (ch == '\'' || ch == '\"')
            {
                if (ch == outerQuote)
                {
                    ret.append(ch); ret.append(ch);
                }
                else
                {
                    ret.append(ch);
                }
            }
            else if (ch == '#')
            {
                ret.append(ch); ret.append(ch);
            }
            else
            {
                ret.append(ch);
            }
        }

        // System.err.println("Out " + ret.toString());

        return ret.toString();
    }
}
