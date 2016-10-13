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

import com.sun.org.apache.regexp.internal.RE;
import com.sun.org.apache.regexp.internal.RESyntaxException;

/**
 * This class encapsulate the ORO Perl5 regular expresion engine offering a much
 * simpler interface.
 */
public final class RegEx
{
    /**
     * Does the Perl pattern generates a match in the string (case sensitive)?
     * 
     * @return boolean
     * @param p_strText
     *            String
     * @param p_strPattern
     *            String. Perl style, don't forget that for example \s becomes
     *            \\s...
     */
    public static boolean matchExact(String p_strText, String p_strPattern)
            throws RegExException
    {
        return matchExact(p_strText, p_strPattern, true);
    }

    /**
     * Does the Perl pattern generates a match in the string?
     * 
     * @return boolean
     * @param p_strText
     *            String
     * @param p_strPattern
     *            String. Perl style don't forget that for example \s becomes
     *            \\s...
     */
    public static boolean matchExact(String p_strText, String p_strPattern,
            boolean p_bCaseSensitive) throws RegExException
    {

        RE pattern = null;
        boolean match = false;

        if (p_strText == null || p_strPattern == null)
        {
            return false;
        }

        // Attempt to compile the pattern. If the pattern is not
        // valid, report the error and exit.
        try
        {
            if (p_bCaseSensitive)
            {
                pattern = new RE(p_strPattern);
            }
            else
            {
                pattern = new RE(p_strPattern, RE.MATCH_CASEINDEPENDENT);
            }
        }
        catch (RESyntaxException e)
        {
            throw new RegExException(e.getMessage());
        }

        // test to see if the match encompasses the entire input
        // string
        if (pattern.match(p_strText))
        {
            if (pattern.getParenEnd(0) == (p_strText.length() - 1))
            {
                match = true;
            }
        }

        return match;
    }

    /**
     * Does a substitution of all occurences of the pattern (/g equivalent in
     * Perl). It is case sensitive
     * 
     * @param p_strText
     *            the original String
     * @param p_strRegEx
     *            the regular expression
     * @param p_strSubWith
     *            the substitution text
     * @return a string in which all occurences of the regular expression have
     *         been replaced.
     */
    public static String substituteAll(String p_strText, String p_strRegEx,
            String p_strSubWith) throws RegExException
    {
        return substituteAll(p_strText, p_strRegEx, p_strSubWith, true);
    }

    /**
     * Does a substitution of all occurences of the pattern (/g equivalent in
     * Perl).
     * 
     * @param p_strText
     *            the original String
     * @param p_strRegEx
     *            the regular expression
     * @param p_strSubWith
     *            the substitution text
     * @param p_bCaseSensitive
     *            boolean case sensitive?
     * @return a string in which all occurences of the regular expression have
     *         been replaced.
     */
    public static String substituteAll(String p_strText, String p_strRegEx,
            String p_strSubWith, boolean p_bCaseSensitive)
            throws RegExException
    {
        if (p_strText == null || p_strRegEx == null)
        {
            return "";
        }

        RE pattern = null;
        try
        {
            if (p_bCaseSensitive)
            {
                pattern = new RE(p_strRegEx);
            }
            else
            {
                pattern = new RE(p_strRegEx, RE.MATCH_CASEINDEPENDENT);
            }
        }
        catch (RESyntaxException e)
        {
            throw new RegExException(e.getMessage());
        }

        return pattern.subst(p_strText, p_strSubWith);
    }

    /**
     * Match the Perl pattern with the substring in the given string (case
     * sensitive)
     * 
     * @return MatchResult or null if the pattern did not match
     * @param p_strText
     *            string to search in
     * @param p_strPattern
     *            a Perl-style regex pattern. Don't forget that for example \s
     *            becomes \\s...
     */
    public static RegExMatchInterface matchSubstring(String p_strText,
            String p_strPattern) throws RegExException
    {
        return matchSubstring(p_strText, p_strPattern, true);
    }

    /**
     * Match the Perl pattern with the substring in the given string
     * 
     * @return MatchResult or null if the pattern did not match
     * @param p_strText
     *            string to search in
     * @param p_strPattern
     *            a Perl-style pattern. Don't forget that for example \s becomes
     *            \\s...
     * @param p_bCaseSensitive
     *            do a case sensitive match if this is true
     */
    public static RegExMatchInterface matchSubstring(String p_strText,
            String p_strPattern, boolean p_bCaseSensitive)
            throws RegExException
    {
        return matchSubstring(p_strText, p_strPattern, p_bCaseSensitive, false);
    }

    /**
     * Match the Perl pattern with the substring in the given string
     * 
     * @return MatchResult or null if the pattern did not match
     * @param p_strText
     *            string to search in
     * @param p_strPattern
     *            a Perl-style pattern. Don't forget that for example \s becomes
     *            \\s...
     * @param p_bCaseSensitive
     *            do a case sensitive match if this is true
     * @param p_singleLineMatch
     *            if this is set to true, input string is considered as a single
     *            line. "^" and "$" ignore \n, but "." matches with \n.
     */
    public static RegExMatchInterface matchSubstring(String p_strText,
            String p_strPattern, boolean p_bCaseSensitive,
            boolean p_singleLineMatch) throws RegExException
    {

        if (p_strText == null || p_strPattern == null)
        {
            return null;
        }

        int matchFlags = 0;
        if (p_bCaseSensitive)
        {
            matchFlags |= RE.MATCH_NORMAL;
        }
        else
        {
            matchFlags |= RE.MATCH_CASEINDEPENDENT;
        }

        if (p_singleLineMatch)
        {
            matchFlags |= RE.MATCH_SINGLELINE;
        }
        else
        {
            matchFlags |= RE.MATCH_MULTILINE;
        }

        // Attempt to compile the pattern. If the pattern is not
        // valid, report the error and exit.
        RE pattern = null;
        try
        {
            pattern = new RE(p_strPattern, matchFlags);
        }
        catch (RESyntaxException e)
        {
            throw new RegExException(e.getMessage());
        }

        RegExMatchInterface result = null;
        if (pattern.match(p_strText))
        {
            result = new RegExMatch(pattern);
        }

        return result;
    }
}
