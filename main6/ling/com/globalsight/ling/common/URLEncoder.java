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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.BitSet;

import org.apache.commons.lang.StringUtils;

import com.sun.jndi.toolkit.url.UrlUtil;

/**
 * The class contains a utility method for converting a
 * <code>String</code> into a MIME format called
 * "<code>x-www-form-urlencoded</code>" format.
 * <p>
 * To convert a <code>String</code>, each character is examined in turn:
 * <ul>
 * <li>The ASCII characters '<code>a</code>' through '<code>z</code>',
 *     '<code>A</code>' through '<code>Z</code>', '<code>0</code>'
 *     through '<code>9</code>', and &quot;.&quot;, &quot;-&quot;,
 * &quot;*&quot;, &quot;_&quot; remain the same.
 * <li>The space character '<code>&nbsp;</code>' is converted into a
 *     plus sign '<code>+</code>'.
 * <li>All other characters are converted into the 3-character string
 *     "<code>%<i>xy</i></code>", where <i>xy</i> is the two-digit
 *     hexadecimal representation of the lower 8-bits of the character.
 * </ul>
 *
 * @author  Herb Jellinek
 * @version 1.18, 02/02/00
 * @since   JDK1.0
 */
public class URLEncoder
{
    static BitSet dontNeedEncoding;
    static final int caseDiff = ('a' - 'A');
    
    static final String URL_SEPERATOR = "/";

    /* The list of characters that are not encoded have been determined by
       referencing O'Reilly's "HTML: The Definitive Guide" (page 164). */

    static {
        dontNeedEncoding = new BitSet(256);
        int i;
        for (i = 'a'; i <= 'z'; i++) {
            dontNeedEncoding.set(i);
        }
        for (i = 'A'; i <= 'Z'; i++) {
            dontNeedEncoding.set(i);
        }
        for (i = '0'; i <= '9'; i++) {
            dontNeedEncoding.set(i);
        }
        /* encoding a space to a + is done in the encode() method */
        dontNeedEncoding.set(' ');
        dontNeedEncoding.set('-');
        dontNeedEncoding.set('_');
        dontNeedEncoding.set('.');
        dontNeedEncoding.set('*');
    }

    /**
     * You can't call the constructor.
     */
    private URLEncoder() { }
    
    public static String encodeUrlString(String url)
    {
        url = url.replace('\\','/');
        String segments[] = url.split(URLEncoder.URL_SEPERATOR);
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < segments.length; i++)
        {
            if (!StringUtils.isEmpty(segments[i])) 
            {
                sb.append(URLEncoder.URL_SEPERATOR)
                  .append(URLEncoder.encode(segments[i]));
            }
        }
        
        return sb.toString();
    }
    
    public static String encodeUrlStr(String url)
    {
        url = url.replace('\\','/');
        String segments[] = url.split(URLEncoder.URL_SEPERATOR);
        StringBuffer sb = new StringBuffer();
		try 
		{
			for (int i = 0; i < segments.length; i++) 
			{
				if (!StringUtils.isEmpty(segments[i])) 
				{
					sb.append(URLEncoder.URL_SEPERATOR).append(
							UrlUtil.encode(segments[i], "utf-8"));
				}
			}
		} 
		catch (Exception e) 
		{
			// ignore
		}
        
        return sb.toString();
    }

    /**
     * Equals with <code>java.net.URLEncoder.encode(String, "UTF-8")</code>
     * @param   s   <code>String</code> to be translated.
     * @return  the translated <code>String</code>.
     */
    public static String encode(String s) {
        // we set the URIEncoding of tomcat in jboss to "UTF-8"
        // we always use UTF-8 to do url encoding and decoding.
        try
        {
            return java.net.URLEncoder.encode(s, "UTF-8");
        }
        catch(UnsupportedEncodingException e)
        {
            // This will not happen. "UTF-8" is always supported.
            return s;
        }
    }

    /**
     * Equals with <code>java.net.URLEncoder.encode(String, "UTF-8")</code>
     * @see java.net.URLEncoder.encode(String, String)
     * @param s
     * @param enc We will ignore this parameter, and use "UTF-8" instead.
     * @return
     */
    public static String encode(String s, String enc) {

        // we always use UTF-8 to do url encoding and decoding.
        return encode(s);

//        int maxBytesPerChar = 10;
//        StringBuffer out = new StringBuffer(s.length());
//        ByteArrayOutputStream buf = new ByteArrayOutputStream(maxBytesPerChar);
//        // GlobalSight change. Always convert using given encoding. Don't use
//        // system default encoding.
//        OutputStreamWriter writer = null;
//        try
//        {
//            writer = new OutputStreamWriter(buf, enc);
//        }
//        catch (UnsupportedEncodingException e)
//        {
//            // doesn't reach here
//        }
//
//
//        for (int i = 0; i < s.length(); i++) {
//            int c = (int)s.charAt(i);
//            if (dontNeedEncoding.get(c)) {
//                if (c == ' ') {
//                    c = '+';
//                }
//                out.append((char)c);
//            } else {
//                // convert to external encoding before hex conversion
//                try {
//                    writer.write(c);
//                    writer.flush();
//                } catch(IOException e) {
//                    buf.reset();
//                    continue;
//                }
//                byte[] ba = buf.toByteArray();
//                for (int j = 0; j < ba.length; j++) {
//                    out.append('%');
//                    char ch = Character.forDigit((ba[j] >> 4) & 0xF, 16);
//                    // converting to use uppercase letter as part of
//                    // the hex value if ch is a letter.
//                    if (Character.isLetter(ch)) {
//                        ch -= caseDiff;
//                    }
//                    out.append(ch);
//                    ch = Character.forDigit(ba[j] & 0xF, 16);
//                    if (Character.isLetter(ch)) {
//                        ch -= caseDiff;
//                    }
//                    out.append(ch);
//                }
//                buf.reset();
//            }
//        }
//
//        return out.toString();
    }
}
