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

public class LeverageMatchPriorityComparator
    implements Comparator
{
    private long m_projectTm = 0;

    /**
    LeverageMatchPriorityComparator constructor comment.
    */
    public LeverageMatchPriorityComparator()
    {
        super();
    }

    /**
    Note: this comparator imposes orderings that are inconsistent with equals.
    */
    public int compare(Object p_one, Object p_two)
    {
        int typeOne = ((LeverageMatch)p_one).getMatchType();
        int typeTwo = ((LeverageMatch)p_two).getMatchType();
        short scoreOne = ((LeverageMatch)p_one).getScoreNum();
        short scoreTwo = ((LeverageMatch)p_two).getScoreNum();

        // leverage match types are the same
        if (typeOne == typeTwo)
        {
            if (scoreOne > scoreTwo)
            {
                return -1;
            }

            if (scoreOne < scoreTwo)
            {
                return 1;
            }
        }

        if (typeOne > typeTwo)
        {
            return 1;
        }

        if (typeOne < typeTwo)
        {
            return -1;
        }

        return 0;
    }

    /**
     *
     * @param p_projectTmId long
     */
    public void setProjectTm(long p_projectTmId)
    {
        m_projectTm = p_projectTmId;
    }
}
