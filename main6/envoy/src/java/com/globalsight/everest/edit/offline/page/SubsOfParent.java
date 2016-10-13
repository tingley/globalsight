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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Primarily a data object. Currently only used during upload to
 * collect and renumber modified subs.
 */
public class SubsOfParent
{
    /** The owner of the subflows. */
    public Long m_parentTuvId = null;
    /** Map of subflows to be saved for this parent. */
    public Map m_subMap = null;

    /**
     * Creates a SubsOfParent with no modified subflows defined.
     * @param p_parentTuvId owner of the subflows
     */
    public SubsOfParent(Long p_parentTuvId)
    {
        m_parentTuvId = p_parentTuvId;
    }

    /**
     * Adds a modified subflow to be saved.
     * @param p_subId the sub id
     * @param p_subText the subflow content
     */
    public void setSubflow(String p_subId, String p_subText)
    {
        if (m_subMap == null)
        {
            m_subMap = new HashMap();
        }

        m_subMap.put(p_subId, p_subText);
    }

    /**
     * Renumbers the subflow ids acroding to the SubflowMergeInfo.
     *
     * @param p_subInfo the merge info for the m_parentTuvId
     *
     * Note: For download, the parent portion of All sublfow ids is written
     *       as the original unmerged parent Id. This simplifies resource
     *       retrieval whil in the offline environment. Upon upload, this
     *       method is called to both renumber the subflow ids and adjust
     *       the parent id under which they are now merged.
     */
    public void renumber(SubflowMergeInfo p_subInfo)
    {
        HashMap newMap = new HashMap();
        Set keys = m_subMap.keySet();

        for (Iterator it = keys.iterator(); it.hasNext(); )
        {
            String curSubIdAsStr = (String)it.next();
            Integer newSubIdAsInt =
                new Integer(Integer.parseInt(curSubIdAsStr) + p_subInfo.m_offset);

            newMap.put(newSubIdAsInt.toString(), m_subMap.get(curSubIdAsStr));
        }

        m_subMap = newMap;
        m_parentTuvId = new Long(p_subInfo.m_parentOfMergeSegId);
    }
}
