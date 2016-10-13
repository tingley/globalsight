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

public class CandidateMatchPriorityComparator
    implements Comparator
{
    private long m_projectTm = 0;
    private Locale m_targetLocale = null;

    public CandidateMatchPriorityComparator(Locale p_targetLocale)
    {
        super();
        m_targetLocale = p_targetLocale;
    }

    public void setProjectTm(long p_projectTmId)
    {
        m_projectTm = p_projectTmId;
    }

    /**
     * Note: this comparator imposes orderings that are inconsistent
     * with equals().
    */
    public int compare(Object p_one, Object p_two)
    {
        CandidateMatch one = (CandidateMatch)p_one;
        CandidateMatch two = (CandidateMatch)p_two;

        int typeOne = one.getMatchType();
        int typeTwo = two.getMatchType();
        short scoreOne = one.getScoreNum();
        short scoreTwo = two.getScoreNum();

        int result = 0;
        if (typeOne > typeTwo)
        {
            result = 1;
        }
        else if (typeOne < typeTwo)
        {
            result = -1;
        }
        // leverage match types are the same
        else
        {
            if (scoreOne > scoreTwo)
            {
                result = -1;
            }
            else if (scoreOne < scoreTwo)
            {
                result = 1;
            }
        }

        // type and score are the same
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
