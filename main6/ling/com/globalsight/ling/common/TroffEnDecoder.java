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

import com.globalsight.ling.common.EncodingChecker;
import com.globalsight.ling.common.NativeEnDecoder;
import com.globalsight.ling.common.NativeEnDecoderException;

public class TroffEnDecoder
    extends NativeEnDecoder
{
    public String decode(String p_nativeString)
        throws NativeEnDecoderException
    {
        throw notApplicable();
    }

    public String decode(String p_nativeString, String p_outerQuote)
        throws NativeEnDecoderException
    {
        throw notApplicable();
    }

    /**
     * Encoder routine for troff tags.
     */
    public String encode(String p_nativeString)
        throws NativeEnDecoderException
    {
        int len = p_nativeString.length();
        StringBuffer result = new StringBuffer(len);

        for (int i = 0; i < len; i++)
        {
            char ch = p_nativeString.charAt(i);

            if (ch == '<')
            {
                result.append("<\\<>");
            }
            else if (ch == '@')
            {
                result.append("<\\@>");
            }
            else if (ch == '\\')
            {
                result.append("<\\\\>");
            }
            else if (ch == '\u00a0')
            {
                // non-breaking space
                result.append("<\\!s>");
            }
            else if (ch == '\u2011')
            {
                // non-breaking hyphen
                result.append("<\\!->");
            }
            else if (ch == '\u2014')
            {
                // breaking em-dash
                result.append("<\\m>");
            }
            else
            {
                result.append(ch);
            }
        }

        return result.toString();
    }

    public String encode(String p_nativeString, String p_outerQuote)
        throws NativeEnDecoderException
    {
        throw notApplicable();
    }

    /**
     * This method encodes the string plus checks the encoding.
     * Designed to be used for merging.
     */
    public String encodeWithEncodingCheck(String p_nativeString)
        throws NativeEnDecoderException
    {
        String s = encode(p_nativeString);

        return encodingCheck(s);
    }

    public String encodeWithEncodingCheck(String p_nativeString,
        String p_outerQuote)
        throws NativeEnDecoderException
    {
        throw notApplicable();
    }

    private String encodingCheck(String p_nativeString)
        throws NativeEnDecoderException
    {
        int len = p_nativeString.length();
        StringBuffer result = new StringBuffer(len);

        // If there are characters that cannot be converted to the
        // specified encogind, they will be converted to character
        // references.
        for (int i = 0; i < len; i++)
        {
            char c = p_nativeString.charAt(i);

            if (encChecker.canConvert(c))
            {
                result.append(c);
            }
            else
            {
                result.append("?");
            }
        }

        return result.toString();
    }
}
