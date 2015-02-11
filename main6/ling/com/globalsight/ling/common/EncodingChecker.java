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

import java.io.UnsupportedEncodingException;

import sun.io.CharToByteConverter;

public class EncodingChecker
{
    private String m_encoding = null;
    private CharToByteConverter m_converter = null;

    public EncodingChecker(String p_encoding)
        throws UnsupportedEncodingException
    {
        m_encoding = p_encoding;
        m_converter = CharToByteConverter.getConverter(p_encoding);
    }

    public boolean canConvert(char c)
    {
        return m_converter.canConvert(c);
    }

    public String getEncoding()
    {
        return m_encoding;
    }
    
}
