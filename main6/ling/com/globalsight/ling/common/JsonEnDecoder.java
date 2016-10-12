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

public class JsonEnDecoder extends NativeEnDecoder
{
    static private final String NCR_BEGIN = "&#x";
    static private final String NCR_END = ";";
    
    @Override
    public String decode(String p_nativeString) throws NativeEnDecoderException
    {
        return null;
    }

    @Override
    public String decode(String p_nativeString, String p_outerQuote)
            throws NativeEnDecoderException
    {
        return null;
    }

    @Override
    public String encode(String p_nativeString) throws NativeEnDecoderException
    {
        return null;
    }

    @Override
    public String encode(String p_nativeString, String p_outerQuote)
            throws NativeEnDecoderException
    {
        return null;
    }

    @Override
    public String encodeWithEncodingCheck(String p_nativeString) throws NativeEnDecoderException
    {
        return encodingCheck(p_nativeString);
    }

    @Override
    public String encodeWithEncodingCheck(String p_nativeString, String p_outerQuote)
            throws NativeEnDecoderException
    {
        return null;
    }
    
    private String encodingCheck(String p_nativeString)
            throws NativeEnDecoderException
        {
            StringBuffer sbuf = new StringBuffer();

            // If there are characters that cannot be converted to the
            // specified encogind, they will be converted to character
            // references.
            for (int i = 0; i < p_nativeString.length(); i++)
            {
                char c = p_nativeString.charAt(i);

                if (encChecker.canConvert(c))
                {
                    sbuf.append(c);
                }
                else
                {
                    sbuf.append(NCR_BEGIN + Integer.toHexString(c) + NCR_END);
                }
            }

            return sbuf.toString();
        }
}
