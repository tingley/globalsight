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
 * Encodes and decodes VBScript escape sequences.
 */
public class VBEscapeSequence
    extends NativeEnDecoder
{
    /**
     * Converts escaped unicode (\\uxxxx) to unicode chars,
     * and standard escape sequences to their native forms.
     */
    public String decode (String p_str)
        throws NativeEnDecoderException
    {
        return p_str;
    }

    public String decode(String p_str, String p_outerQuote)
        throws NativeEnDecoderException
    {
        throw notApplicable();
    }

    /**
     * Encodes standard character escapes and converts characters
     * below 20 and above 127 into unicode escapes (\\uxxxx).  Note:
     * The generic Merger should have removed all TMX and XML escapes.
     */
    public String encode(String p_str)
    {
        return p_str;
    }

    public String encode(String p_str, String p_outerQuote)
        throws NativeEnDecoderException
    {
        throw notApplicable();
    }

    /**
     * Encodes a string with respect to a given target character encoding and
     * encodes characters not in the target charset if necessary.
     */
    public String encodeWithEncodingCheck(String p_nativeString)
        throws NativeEnDecoderException
    {
        return encode(p_nativeString);
    }

    public String encodeWithEncodingCheck(String p_str, String p_outerQuote)
        throws NativeEnDecoderException
    {
        throw notApplicable();
    }
}
