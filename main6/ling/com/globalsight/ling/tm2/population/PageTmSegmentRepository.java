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
package com.globalsight.ling.tm2.population;

import com.globalsight.util.GlobalSightLocale;


/**
 * A repository of Page TM segments.
 */

public class PageTmSegmentRepository
    extends UniqueSegmentRepository
{
    // Tm id of the page
    private long m_tmId;
    
    /**
     * Constructor.
     * @param p_sourceLocale source locale
     */
    public PageTmSegmentRepository(GlobalSightLocale p_sourceLocale)
    {
        super(p_sourceLocale);
    }


    public void setTmId(long p_tmId)
    {
        m_tmId = p_tmId;
    }
    

    public long getTmId()
    {
        return m_tmId;
    }
}
