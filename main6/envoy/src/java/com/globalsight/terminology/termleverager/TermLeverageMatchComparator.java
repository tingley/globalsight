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

package com.globalsight.terminology.termleverager;

import java.util.Comparator;

/**
 * Please document me.
 */
public class TermLeverageMatchComparator
    implements Comparator
{
    /** Constructor argument to request comparison by source term id. */
    static public final int COMPARE_SOURCETERMID = 1;
    /** Constructor argument to request comparison by target locale. */
    static public final int COMPARE_TARGETLOCALE = 2;

    private boolean m_sourceTermIdAsPrimaryKey = true;

    /**
     * This class exposes two comparison behaviours that can be
     * switched by constructing instances with either
     * COMPARE_SOURCETERMID or COMPARE_TARGETLOCALE.
     */
    public TermLeverageMatchComparator(int p_comparisonType)
    {
        switch (p_comparisonType)
        {
        case COMPARE_SOURCETERMID:
            m_sourceTermIdAsPrimaryKey = true;
            break;
        case COMPARE_TARGETLOCALE:
            m_sourceTermIdAsPrimaryKey = false;
            break;
        }
    }

    public int compare(Object p_termMatch1, Object p_termMatch2)
    {
        TermLeverageMatch match1 = (TermLeverageMatch)p_termMatch1;
        TermLeverageMatch match2 = (TermLeverageMatch)p_termMatch2;

        if (m_sourceTermIdAsPrimaryKey)
        {
            return sortUsingSourceTermIdAsPrimaryKey(match1, match2);
        }
        else
        {
            return sortUsingTargetLocaleAsPrimaryKey(match1, match2);
        }
    }

    /**
     * Sort by source term id, real target locale name, fuzzy score.
     */
    private int sortUsingSourceTermIdAsPrimaryKey(
        TermLeverageMatch p_termMatch1, TermLeverageMatch p_termMatch2)
    {
        long srcId1 = p_termMatch1.getMatchedSourceTermId();
        long srcId2 = p_termMatch2.getMatchedSourceTermId();

        String locale1 = p_termMatch1.getRealTargetLocale().toString();
        String locale2 = p_termMatch2.getRealTargetLocale().toString();

        if (srcId1 < srcId2)
        {
            return -1;
        }
        else if (srcId1 > srcId2)
        {
            return 1;
        }
        else // src ids must be equal
        {
            // compare locale names
            return locale1.compareTo(locale2);
        }
    }

    /**
     * Sort by real target locale name, fuzzy score, then length of
     * source term match.
     */
    private int sortUsingTargetLocaleAsPrimaryKey(
        TermLeverageMatch p_termMatch1, TermLeverageMatch p_termMatch2)
    {
        String locale1 = p_termMatch1.getRealTargetLocale().toString();
        String locale2 = p_termMatch2.getRealTargetLocale().toString();

        int score1 = p_termMatch1.getScore();
        int score2 = p_termMatch2.getScore();

        int length1 = p_termMatch1.getMatchedSourceTerm().length();
        int length2 = p_termMatch2.getMatchedSourceTerm().length();


        // compare locale names
        int cmp = locale1.compareTo(locale2);

        // same locale, now sort by fuzzy score
        if (cmp == 0)
        {
            if (score1 > score2)
            {
                cmp = -1;
            }
            else if (score1 < score2)
            {
                cmp = 1;
            }
            else // fuzzy scores equal
            {
                // longer length is better
                if(length1 > length2)
                {
                    cmp = -1;
                }
                else if (length1 < length2)
                {
                    cmp = 1;
                }
            }
        }

        return cmp;
    }
}
