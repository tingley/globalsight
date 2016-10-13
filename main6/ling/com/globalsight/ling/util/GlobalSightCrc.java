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
package com.globalsight.ling.util;

import java.io.UnsupportedEncodingException;

/* 
 * Calucurate Exact Match Key (CRC) used in TUV table
*/

public class GlobalSightCrc
{
    private static final Crc crc = new Crc();
    
    /**
     * Calculate CRC value of a specified String. CRC value is
     * calculated based on UTF-8 representation of the string.
     * @param segment - String to be calculated.
     * @return Long CRC value
     */
    public static long calculate(String segment)
    {
        // For Lexmark TMs leverage issue
        segment = segment.trim();
        
        long l = 0;
        try
        {
            l = crc.calculate(segment.getBytes("UTF8"));
        }
        catch(UnsupportedEncodingException e)
        {
            // won't happen
        }
        return l;
    }

    /*
     * Test code.
    static public void main(String[] args)
    {
        long l;
        l = GlobalSightCrc.calculate("This is the test string");
        System.out.println(l == 0xcd287ff2L ? "ok 1\n" : "not ok 1\n");
        System.out.println(Long.toHexString(l));

        // NI HON GO in Japanese
        l = GlobalSightCrc.calculate("\u65e5\u672c\u8a9e");
        System.out.println(l == 0x8260a87eL ? "ok 2\n" : "not ok 2\n");
        System.out.println(Long.toHexString(l));
    }
	*/
    
}
