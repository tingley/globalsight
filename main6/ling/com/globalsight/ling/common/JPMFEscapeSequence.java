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
import com.globalsight.ling.common.JPEscapeSequence;

/**
 * Encodes and decodes Java MessageFormat escape sequences.
 */
public class JPMFEscapeSequence
    extends NativeEnDecoder
{
    JPEscapeSequence m_JPCodec = new JPEscapeSequence();

    /*
     * Converts escaped single qoutes to their native forms.
     */
    public String decode (String p_str)
        throws NativeEnDecoderException
    {
        // first decode as standard java property
        p_str = m_JPCodec.decode(p_str);

        char aChar;
        Character c;

        int len = p_str.length();
        StringBuffer result = new StringBuffer(len);

        for (int x = 0; x < len; )
        {
            aChar = p_str.charAt(x++);
            if (aChar == '\'' && x < len && p_str.charAt(x) == '\'')
            {
                // remove escapement on single qoute
                result.append(aChar);
                x++;
            }
            else if (aChar == '\'' &&
              x < len && (p_str.charAt(x) == '{' || p_str.charAt(x) == '}') &&
              x + 1 < len && p_str.charAt(x + 1) == '\'')
            {
                // remove escapement on open/close bracket
                result.append(p_str.charAt(x++));
                x++;                              // skip closing quote
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
     * Encodes single quotes as escaped single quotes.
     */
    public String encode(String p_str)
    {
        // first encode as standard java message format

        char aChar;
        int len = p_str.length();
        StringBuffer result = new StringBuffer(len*2);

        for (int x = 0; x < len; )
        {
            aChar = p_str.charAt(x++);
            switch (aChar)
            {
            case '\'': result.append("''");  continue;
            case '{':  result.append("'{'"); continue;
            case '}':  result.append("'}'"); continue;
            default:
                result.append(aChar);
                break;
            }
        }

        // This a message format within a javaproperty file so
        // secondly, we encode again as standard java property
        String finalResult = m_JPCodec.encode(result.toString());

        return finalResult;
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
}
