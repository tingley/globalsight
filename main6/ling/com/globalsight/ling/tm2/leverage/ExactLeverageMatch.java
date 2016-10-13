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
package com.globalsight.ling.tm2.leverage;

import com.globalsight.util.GlobalSightLocale;

import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

/**
 * ExactLeverageMatch is a temporary storage of exact matches that is
 * primarily obtained from LeverageMatches object and used to create
 * ExactMatchedSegments in LeverageDataCenter
 */

class ExactLeverageMatch
{
    // map of target locale and its exact match.
    // key:    target locale (GlobalSightLocale)
    // value:  exact match (LeveragedTuv)
    private Map m_exactMatches = new HashMap();


    // add a LeveragedTuv to this object
    void add(LeveragedTuv p_leveragedTuv, GlobalSightLocale p_targetLocale)
    {
        m_exactMatches.put(p_targetLocale, p_leveragedTuv);
    }
    

    // returns an Iterator that iterates target locales. next() method
    // of this iterator returns GlobalSightLocale object
    Iterator targetLocaleIterator()
    {
        return m_exactMatches.keySet().iterator();
    }
    

    // returns an exact match of a given locale. If there isn't a
    // match for the locale, null is returned.
    LeveragedTuv getMatch(GlobalSightLocale p_targetLocale)
    {
        return (LeveragedTuv)m_exactMatches.get(p_targetLocale);
    }
    
}
