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
 * This class sorts MatchRecords by the source term score (multiple
 * termbases or indexes may be searched.)
 */
public class SourceTermComparator
    implements Comparator
{
    public int compare(Object p_object1, Object p_object2)
    {
        TermLeverageResult.MatchRecord match1 =
            (TermLeverageResult.MatchRecord)p_object1;
        TermLeverageResult.MatchRecord match2 =
            (TermLeverageResult.MatchRecord)p_object2;

        int score1 = match1.getScore();
        int score2 = match1.getScore();

        if (score1 > score2)
        {
            return -1;
        }
        else if (score1 < score2)
        {
            return 1;
        }

        return 0;
    }
}
