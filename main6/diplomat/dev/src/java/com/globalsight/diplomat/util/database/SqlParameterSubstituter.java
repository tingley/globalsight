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
package com.globalsight.diplomat.util.database;

import com.globalsight.diplomat.util.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Utility class used for substituting parameters into Sql statements.
 * All public methods are declared static on this class.
 */
public class SqlParameterSubstituter
{
    //
    // PRIVATE CONSTANTS
    //
    private static final String PARAMETER_DELIMITER = "||||";
    private static final String SUBSTITUTION_DELIMITER = "%%";
    private static final String MISSING_IDENTIFIER = 
        "No identifier found between substitution delimiters";
    private static final String INVALID_IDENTIFIER = 
        "Invalid parameter identifier";

    //
    // PRIVATE STATIC VARIABLES
    //
    private static Logger c_logger = Logger.getLogger();

    /**
     * Return a well-formed Sql string created by inserting parameters from the
     * p_sqlParams string into the p_sqlTemplate.
     *
     * @param p_sqlTemplate a string of partially formed sql containing marked
     * parameter indices, surrounded by substitution delimiters "%%".  For
     * example to refer to the first parameter in the list, use "%%1%%"; to
     * refer to the nth parameter, use "%%<n>%%" where n is an integer.
     *
     * @param p_sqlParams a list of parameter values to substitute into the
     * template.  Parameters are separated from each other by the parameter
     * delimiter "||||" (i.e. 4 consecutive vertical bars).
     *
     * @return a "well-formed" sql string with the parameter values inserted.
     */
    public static String substitute(String p_sqlTemplate, String p_sqlParams)
    {
        c_logger.println(Logger.DEBUG_D, "SqlParameterSubstituter: substituting(template=" +
                         p_sqlTemplate + ", paramString=" + p_sqlParams + ")");
        StringBuffer sb = new StringBuffer(p_sqlTemplate);
        Vector params = tokenize(p_sqlParams);
        for (int i = 1 ; i <= params.size(); i++)
{
            String search = SUBSTITUTION_DELIMITER + i + SUBSTITUTION_DELIMITER;
            String substitute = (String)params.elementAt(i - 1);
            int start = 0;
            while (start > -1)
            {
                start = sb.toString().indexOf(search);
                int end = start + search.length();
                if (start > -1)
                {
                    sb.replace(start, end, escapeQuotes(substitute));
                }
            }
        }
        c_logger.println(Logger.DEBUG_D, "SqlParameterSubstituter: result=" + sb.toString());
        return sb.toString();
    }

    /**
     * Return a well-formed Sql string created by inserting parameters from the
     * p_paramMap map into the p_sqlTemplate.
     *
     * @param p_sqlTemplate a string of partially formed sql containing marked
     * parameter indices, surrounded by substitution delimiters "%%".  For
     * example to refer to the first parameter in the list, use "%%1%%"; to
     * refer to the nth parameter, use "%%<n>%%" where n is an integer.
     *
     * @param p_paramMap a hashmap of parameter key-value pairs to substitute
     * into the template.  Keys and values must be strings; keys are NOT
     * case sensitive. 
     *
     * @return a "well-formed" sql string with the parameter values inserted.
     *
     * @throws SqlParameterSubstitutionException if an attempt is made to
     * insert a named parameter that cannot be found on the parameter map.
     */
    public static String substitute(String p_sqlTemplate, HashMap p_paramMap)
    throws SqlParameterSubstitutionException
    {
        c_logger.println(Logger.DEBUG_D, "SqlParameterSubstituter: substituting(template=" +
                         p_sqlTemplate + ", paramMap=" + p_paramMap + ")");
        int subLen = SUBSTITUTION_DELIMITER.length();
        StringBuffer sb = new StringBuffer(p_sqlTemplate);
        boolean done = false;
        int start;
        int end;
        while (!done)
        {
            String str = sb.toString();
            start = str.indexOf(SUBSTITUTION_DELIMITER);
            done = (start == -1);
            if (!done)
            {
                end = str.indexOf(SUBSTITUTION_DELIMITER, start + subLen);
                if (end == -1 || end == start + subLen)
                {
                    throwException(MISSING_IDENTIFIER, p_sqlTemplate);
                }
                else
                {
                    end += subLen;
                }
                String subValue =
                    find(str.substring(start + subLen, end - subLen), p_paramMap);
                if (subValue == null)
                {
                    throwException(INVALID_IDENTIFIER, str.substring(start, end));
                }
                sb.replace(start, end, escapeQuotes(subValue));
            }
        }
        c_logger.println(Logger.DEBUG_D, "SqlParameterSubstituter: result=" + sb.toString());
        return sb.toString();
    }

    /**
     * Return a vector containing (non-empty) strings, where each string
     * represents a single parameter to be substituted into an Sql template.
     *
     * @param p_paramTemplate a string of parameters concatenated together,
     * separated from each other by the parameter delimiter, "||||".
     *
     * @return a vector containing the separated strings.
     */
    public static Vector tokenize(String p_paramTemplate)
    {
        StringTokenizer st = new StringTokenizer(p_paramTemplate, PARAMETER_DELIMITER);
        Vector v = new Vector();
        while (st.hasMoreTokens())
        {
            String s = ((String)st.nextToken()).trim();
            if (s.length() > 0)
            {
                v.addElement(s);
            }
        }
        return v;
    }

    /* Find the string mapped to the given key, ignoring case. */
    private static String find(String p_key, HashMap p_map)
    {
        String value = null;
        Iterator it = p_map.entrySet().iterator();
        while (value == null && it.hasNext())
        {
            Map.Entry e = (Map.Entry)it.next();
            if (p_key.equalsIgnoreCase((String)e.getKey()))
            {
                value = (String)e.getValue();
            }
        }
        return value;
    }

    /* Replace all single quotes in the given string with 2 single quotes */
    private static String escapeQuotes(String p_string)
    {
        char quote = '\'';
        StringBuffer sb = new StringBuffer();
        for (int i = 0 ; i < p_string.length() ; i++)
        {
            char ch = p_string.charAt(i);
            sb.append(ch);
            if (ch == quote)
            {
                sb.append(quote);
            }
        }
        return sb.toString();
    }

    /* Throw a generic exception. */
    private static void throwException(String p_s1, String p_s2)
    throws SqlParameterSubstitutionException
    {
        throw new SqlParameterSubstitutionException(p_s1 + " [" + p_s2 + "]");
    }
}

