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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.io.Serializable;

/**
 * TermLeverageMatchResultSet contains a collection of
 * TermLeverageMatchResult objects that are found for a page.  This
 * class provides methods to retrieve a list of
 * TermLeverageMatchResult depending on source tuv id and its sub id.
 */
public class TermLeverageMatchResultSet implements Serializable
{
    private static final long serialVersionUID = -8647154251162713683L;

    // Key:   Source TUV id
    // Value: collection of TermLeverageMatchResult
    private HashMap<Long, List<TermLeverageMatchResult>> m_tuvMatchesMapping = new HashMap<Long, List<TermLeverageMatchResult>>();

    /**
     * Set a collection of TermLeverageMatchResult grouped by TUV id
     * and its subflow id.
     *
     * @param p_sourceTuvId Source TUV id.
     * @param p_subId The id of a subflow within the source TUV or 0
     * for parent segment.
     * @param p_leverageMatches a collection of TermLeverageMatchResult.
     */
    public void setLeverageMatches(long p_sourceTuvId, long p_subId,
        ArrayList<TermLeverageMatchResult> p_leverageMatches)
    {
        // sub id is not used in this release (4.2).
        m_tuvMatchesMapping.put(new Long(p_sourceTuvId), p_leverageMatches);
    }

    /**
     * Get a collection of TermLeverageMatchResult grouped by TUV id
     * and its subflow id.
     *
     * @param p_sourceTuvId Source TUV id.
     * @param p_subId The id of a subflow within the source TUV or 0
     * for parent segment.
     */
    public ArrayList<TermLeverageMatchResult> getLeverageMatches(
            long p_sourceTuvId, long p_subId)
    {
        // sub id is not used in this release (4.2).
        return (ArrayList<TermLeverageMatchResult>) m_tuvMatchesMapping
                .get(new Long(p_sourceTuvId));
    }
}
