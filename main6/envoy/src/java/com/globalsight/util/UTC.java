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

package com.globalsight.util;

import java.text.SimpleDateFormat;
import java.text.ParsePosition;

import java.util.Date;

/**
 * <p>Helper class for UTC time format "2000-02-16T15:56:00" - note
 * the letter "T" between date and time part. Provides conversions
 * from Java Date to Java Strings.</p>
 *
 * <p>The pattern for UTC is CCYY-MM-DDThh:mm:ss where CC represents
 * the century, YY the year, MM the month, and DD the day, preceded by
 * an optional leading negative (-) character to indicate a negative
 * number. If the negative character is omitted, positive (+) is
 * assumed. The T is the date/time separator and hh, mm, and ss
 * represent hour, minute, and second respectively. Additional digits
 * can be used to increase the precision of fractional seconds if
 * desired. For example, the format ss.ss... with any number of digits
 * after the decimal point is supported. The fractional seconds part
 * is optional.</p>
 *
 * <p>This representation may be immediately followed by a "Z" to
 * indicate Coordinated Universal Time (UTC) or to indicate the time
 * zone. For example, the difference between the local time and
 * Coordinated Universal Time, immediately followed by a sign, + or -,
 * followed by the difference from UTC represented as hh:mm (minutes
 * is required). If the time zone is included, both hours and minutes
 * must be present.  </p>
 */
public class UTC
{
    static public final String FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    static public final String FORMAT_ORACLE = "YYYY-MM-DD\"T\"HH24:MI:SS";

    // The short format as used by TMX.
    static public final String FORMAT_COMPRESSED = "yyyyMMdd'T'HHmmss'Z'";

    /** Static class, private constructor. */
    private UTC() { }

    /**
     * Converts a Java Date to the default (human-readable) UTC String.
     */
    public static String valueOf(Date p_date)
    {
        SimpleDateFormat formatter = new SimpleDateFormat (FORMAT);

        return formatter.format(p_date);
    }

    /**
     * Parses the default (human-readable) UTC String representation
     * into a Java Date.
     */
    public static Date parse(String p_date)
    {
        SimpleDateFormat formatter = new SimpleDateFormat (FORMAT);

        ParsePosition pos = new ParsePosition(0);

        return formatter.parse(p_date, pos);
    }

    /**
     * Converts a Java Date to a compressed UTC String
     * (machine-readable, no "-" and ":" separators).
     */
    public static String valueOfNoSeparators(Date p_date)
    {
        SimpleDateFormat formatter = new SimpleDateFormat (FORMAT_COMPRESSED);

        return formatter.format(p_date);
    }

    /**
     * Parses the compressed (machine-readable) UTC String
     * representation into a Java Date.
    */
    public static Date parseNoSeparators(String p_date)
    {
        SimpleDateFormat formatter = new SimpleDateFormat (FORMAT_COMPRESSED);

        ParsePosition pos = new ParsePosition(0);

        return formatter.parse(p_date, pos);
    }

    //
    // Test code
    //

    /*
    static void main(String[] args)
        throws Exception
    {
        Date now = new Date();
        String sNow = UTC.valueOfNoSeparators(now);
        System.out.println(sNow);

        // can we parse the short format as long?
        Date then = UTC.parse(sNow);
        System.out.println(then);

        // no we can't.
        if (then == null)
        {
            then = UTC.parseNoSeparators(sNow);
            System.out.println("Corrected: " + then);
        }
    }
    */
}
