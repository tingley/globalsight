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

/**
 * Transcoder class performs encoding conversions between byte[]
 * and String.
 */
public class Transcoder
{
    public Transcoder() {
        super();
    }

    public String toUnicode(byte[] p_input, String p_toCodeset)
        throws TranscoderException
    {
        try
        {
            return new String(p_input, p_toCodeset);
        }
        catch (UnsupportedEncodingException e)
        {
            throw new TranscoderException(e.toString());
        }
    }

    public byte[] transcode(byte[] p_input, String p_fromCodeset,
        String p_toCodeset)
        throws TranscoderException
    {
        try
        {
            String strPivot = new String(p_input, p_fromCodeset);
            return strPivot.getBytes(p_toCodeset);
        }
        catch (UnsupportedEncodingException e)
        {
            throw new TranscoderException(e.toString());
        }
    }

    public byte[] transcode(String p_input, String p_toCodeset)
        throws TranscoderException
    {
        try
        {
            return p_input.getBytes(p_toCodeset);
        }
        catch (UnsupportedEncodingException e)
        {
            throw new TranscoderException(e.toString());
        }
    }
}
