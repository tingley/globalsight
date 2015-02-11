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
 * This is a wrapper class for com.globalsight.ling.common.URLDecoder.
 * And we just used the method com.globalsight.ling.common.URLDecoder(String, String).
 */
public class URLDecoder
{
    /**
     * Equals with <code>com.globalsight.ling.common.URLDecoder.decode(String, "UTF-8")</code>
     * @param s
     * @return
     */
    public static String decode(String s)
    {
        // we set the URIEncoding of tomcat in jboss to "UTF-8"
        // we always use UTF-8 to do url encoding and decoding.
        try
        {
            return java.net.URLDecoder.decode(s, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            // This will not happen. "UTF-8" is always supported.
            return s;
        }
    }

    /**
     * Equals with <code>com.globalsight.ling.common.URLDecoder.decode(String, "UTF-8")</code>
     * @see com.globalsight.ling.common.URLDecoder.decode(String, String)
     * @param s
     * @param enc We will ignore this parameter, and use "UTF-8" instead.
     * @return
     */
    public static String decode(String s, String enc)
    {
        // we always use UTF-8 to do url encoding and decoding.
        return decode(s);
    }
}
