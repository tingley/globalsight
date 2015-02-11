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

public class HtmlEscapeSequence
    extends NativeEnDecoder
{
    static private final String NCR_BEGIN = "&#";
    static private final String NCR_END = ";";

    private HtmlEntities m_converter = null;

    public HtmlEscapeSequence()
    {
        super();
        m_converter = new HtmlEntities();
    }

    public String decode(String p_NativeString)
        throws NativeEnDecoderException
    {
        return m_converter.decodeString(p_NativeString, null);
    }

    public String decode(String p_str, String p_outerQuote)
        throws NativeEnDecoderException
    {
        throw notApplicable();
    }

    public String encode(String p_NativeString)
        throws NativeEnDecoderException
    {
        // NBSP, COPYRIGHT, TRADE MARK, REGISTERED
        // GSDEF 4023: export &trade; as NCR.
        // char[] charsToInclude = {'\u00a0', '\u00a9', '\u2122', '\u00ae'};
        char[] charsToInclude = {'\u00a0'};
        m_converter.setUseDefaultHtmlEncoderChar(true);
        return m_converter.encodeString(p_NativeString, charsToInclude);
    }

    public String encode(String p_str, String p_outerQuote)
        throws NativeEnDecoderException
    {
        throw notApplicable();
    }

    public String encodeWithEncodingCheckForSkeleton(String p_NativeString)
        throws NativeEnDecoderException
    {
        return encodeWithEncodingCheck(p_NativeString, false);
    }
    
    public String encodeWithEncodingCheck(String p_NativeString)
        throws NativeEnDecoderException
    {
        return encodeWithEncodingCheck(p_NativeString, true);
    }
    
    public String encodeWithEncodingCheck(String p_NativeString,
            boolean p_useDefaultHtmlEncoderChar, char[] charsToInclude)
            throws NativeEnDecoderException
        {
            m_converter.setUseDefaultHtmlEncoderChar(p_useDefaultHtmlEncoderChar);
            StringBuffer sbuf = new StringBuffer();

            String s = m_converter.encodeString(p_NativeString, charsToInclude);

            // If there are characters that cannot be converted to the
            // specified encoding, they will be converted to character
            // references.
            for (int i = 0; i < s.length(); i++)
            {
                char c = s.charAt(i);
                if (encChecker.canConvert(c))
                {
                    sbuf.append(c);
                }
                else
                {
                    sbuf.append(NCR_BEGIN + Integer.toString(c) + NCR_END);
                }
            }

            return sbuf.toString();
        }

    private String encodeWithEncodingCheck(String p_NativeString,
        boolean p_useDefaultHtmlEncoderChar)
        throws NativeEnDecoderException
    {
        // NBSP, COPYRIGHT, TRADE MARK, REGISTERED
        // GSDEF 4023: export &trade; as NCR.
        // char[] charsToInclude = {'\u00a0', '\u00a9', '\u2122', '\u00ae'};
        char[] charsToInclude = {'\u00a0'};

        return encodeWithEncodingCheck(p_NativeString, p_useDefaultHtmlEncoderChar, charsToInclude);
    }

    public String encodeWithEncodingCheck(String p_str, String p_outerQuote)
        throws NativeEnDecoderException
    {
        throw notApplicable();
    }
}
