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
package com.globalsight.everest.edit.offline.page;

import com.globalsight.everest.integration.ling.tm2.MatchTypeStatistics;
import com.globalsight.everest.tuv.PageSegments;

/**
 * A simple container that holds an OfflinePageData and the
 * PageSegments it was create from.
 */
public class PageData
{
    //
    // Private Members
    //

    PageSegments m_PS = null;
    OfflinePageData m_OPD = null;
    MatchTypeStatistics m_matchTypeStats = null;
    //
    // Constructors
    //

    /** Creates a new instance of PageData. */
    public PageData(PageSegments p_PS, OfflinePageData p_OPD)
    {
        m_PS = p_PS;
        m_OPD = p_OPD;
    }

    //
    // Public methods
    //

    /** Returns the PageSegments used to OfflinePageData. */
    public PageSegments getPageSegments()
    {
        return m_PS;
    }

    /** Returns the OfflinePageData. */
    public OfflinePageData getOfflinePageData()
    {
        return m_OPD;
    }
    public void setMatchStatistics(MatchTypeStatistics matchTypeStats)
    {
        m_matchTypeStats = matchTypeStats;
    }
    public MatchTypeStatistics getMatchTypeStatistics()
    {
        return m_matchTypeStats;
    }
}
