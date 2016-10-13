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


import com.globalsight.ling.tm2.BaseTmTuv;

import java.util.Map;
import java.util.HashMap;

/**
 * SegmentIdMap maps Tuv with its id and sub id.
 */

public class SegmentIdMap
{
    // Key:    Combination of String representation of Tuv id and sub id
    // Value:  BaseTmTuv
    Map m_segmentIdMap = null;
    

    // default constructor
    public SegmentIdMap()
    {
        m_segmentIdMap = new HashMap();
    }

    // constructor with a default size
    public SegmentIdMap(int p_size)
    {
        m_segmentIdMap = new HashMap(p_size);
    }

    public BaseTmTuv get(long p_id, String p_subId)
    {
        return (BaseTmTuv)m_segmentIdMap.get(createKey(p_id, p_subId));
    }
    
    public void put(long p_id, String p_subId, BaseTmTuv p_tuv)
    {
        m_segmentIdMap.put(createKey(p_id, p_subId), p_tuv);
    }


    private String createKey(long p_id, String p_subId)
    {
        return Long.toString(p_id) + " " + (p_subId == null ? "" : p_subId);
    }

}
