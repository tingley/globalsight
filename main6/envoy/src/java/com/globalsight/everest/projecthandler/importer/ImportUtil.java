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

package com.globalsight.everest.projecthandler.importer;

import org.apache.log4j.Logger;

import com.sun.org.apache.regexp.internal.RE;

/**
 * Reads CSV files and produces Entry objects.
 */
public class ImportUtil
{
    private static final Logger CATEGORY = Logger.getLogger(ImportUtil.class);

    //
    // Private Member Variables
    //

    //
    // Constructors
    //

    /** Static class, private constructor */
    private ImportUtil()
    {
    }

    public static RE getDelimiterRegexp(String p_delimiter) throws Exception
    {
        // Build a regexp from the separator char and be careful
        // to protect special chars like '|' (make them "\|").

        String pattern;

        if (p_delimiter.equals("tab"))
        {
            pattern = "\t";
        }
        else if (p_delimiter.equals("space"))
        {
            pattern = " ";
        }
        else
        {
            pattern = RE.simplePatternToFullRegularExpression(p_delimiter);
        }

        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("delimiter regexp = `" + pattern + "'");
        }

        RE regexp = new RE(pattern);

        return regexp;
    }

    public static boolean isEmptyLine(String p_line)
    {
        return p_line.trim().length() == 0;
    }

}
