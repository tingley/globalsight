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
package com.globalsight.ling.tm;

import java.util.Comparator;
import java.util.Locale;
import org.apache.log4j.Logger;
import com.globalsight.ling.tm.LingManagerException;
import org.apache.log4j.Category;

/**
 * This comparator is used to find and remove duplicate candidate
 * matches, so it primarily sorts the segments in alphabetic order.
 *
 * Sort order:
 * 1) source string alphabetically
 * 2) target string alphabetically
 * 3) match type (in numeric order)
 * 4) target locale
 */
public class CandidateMatchComparator
    implements Comparator
{
    // Log facility
    private static final Category CATEGORY =
        Logger.getLogger(
            CandidateMatchComparator.class.getName());

    private Locale m_targetLocale;

    public CandidateMatchComparator(Locale p_targetLocale)
    {
        super();
        m_targetLocale = p_targetLocale;
    }

    public int compare(Object p_one, Object p_two)
    {
        CandidateMatch one = (CandidateMatch)p_one;
        CandidateMatch two = (CandidateMatch)p_two;

        String stringOneSource = null;
        String stringOneTarget = null;
        String stringTwoSource = null;
        String stringTwoTarget = null;

        int typeOne = one.getMatchType();
        int typeTwo = two.getMatchType();

        try
        {
            stringOneSource = one.getSourceExactMatchFormat();
            stringOneTarget = one.getTargetExactMatchFormat();

            stringTwoSource = two.getSourceExactMatchFormat();
            stringTwoTarget = two.getTargetExactMatchFormat();
        }
        catch (LingManagerException e)
        {
            // This method can't throw any exception because it
            // overrides java.util.Comparator#compare. So it just log
            // the error.
            CATEGORY.error("cannot get segments", e);
        }

        int result = stringOneSource.compareTo(stringTwoSource);
        if (result == 0)
        {
            result = stringOneTarget.compareTo(stringTwoTarget);
        }


        // source+target strings are the same
        if (result == 0)
        {
            // smaller match type is better
            if (typeOne > typeTwo)
            {
                result = 1;
            }
            // larger match type is worse
            else if (typeOne < typeTwo)
            {
                result = -1;
            }
        }

        // source+target strings and the types are the same
        if (result == 0)
        {
            // if one and two have the same Locale, result is 0
            if (!one.getTargetLocale().equals(two.getTargetLocale()))
            {
                // if either one has the same locale as the target,
                // that comes first
                if (one.getTargetLocale().equals(m_targetLocale))
                {
                    result = -1;
                }
                else if (two.getTargetLocale().equals(m_targetLocale))
                {
                    result = 1;
                }
            }
        }

        return result;
    }
}
