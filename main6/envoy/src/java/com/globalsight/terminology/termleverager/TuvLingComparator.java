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

import com.globalsight.ling.tm.TuvLing;

import java.util.Comparator;

class TuvLingComparator
    implements Comparator
{
    public TuvLingComparator()
    {
    }

    public int compare(Object p_tuvLing1, Object p_tuvLing2)
    {
        long tuvLing1 = ((TuvLing)p_tuvLing1).getId();
        long tuvLing2 = ((TuvLing)p_tuvLing2).getId();

        if (tuvLing1 < tuvLing2)
        {
            return -1;
        }
        else if (tuvLing1 > tuvLing2)
        {
            return 1;
        }

        return 0;
    }
}
