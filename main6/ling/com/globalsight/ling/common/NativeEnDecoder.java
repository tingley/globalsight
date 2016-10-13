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
import com.globalsight.ling.common.NativeEnDecoderException;

public abstract class NativeEnDecoder
{
    private String m_lastChar = null;
    EncodingChecker encChecker = null;

    public abstract String decode(String p_nativeString)
        throws NativeEnDecoderException;

    public abstract String decode(String p_nativeString, String p_outerQuote)
        throws NativeEnDecoderException;

    public abstract String encode(String p_nativeString)
        throws NativeEnDecoderException;

    public abstract String encode(String p_nativeString, String p_outerQuote)
        throws NativeEnDecoderException;

    /**
     * This method encodes the string plus checks the encoding.
     * Designed to be used for merging.
     */
    public abstract String encodeWithEncodingCheck(String p_nativeString)
        throws NativeEnDecoderException;

    public abstract String encodeWithEncodingCheck(String p_nativeString,
        String p_outerQuote)
        throws NativeEnDecoderException;

    public String encodeWithEncodingCheckForSkeleton(String p_NativeString)
        throws NativeEnDecoderException
    {
        return p_NativeString;
    }
    

    /**
     * Sets EncodingChecker.
     */
    public void setEncodingChecker(EncodingChecker p_checker)
    {
        encChecker = p_checker;
    }

    /**
     * Helper method for encode/decode to throw exception.
     */
    static final protected NativeEnDecoderException notApplicable()
    {
        return new NativeEnDecoderException("not applicable for this format");
    }

    /**
     * Helper method for encode/decode to throw exception.
     */
    static final protected void throwNotApplicable()
        throws NativeEnDecoderException
    {
        throw notApplicable();
    }

    public void setLastChar(String lastChar)
    {
        m_lastChar = lastChar;        
    }
    
    public String getLastChar()
    {
        return m_lastChar;        
    }

}
