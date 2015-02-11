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

/**
 * A data object which holds subflow information.
 * Primarily used to renumber subflows.
 */
public class SubflowMergeInfo
{
    /** The segment which this information is for. */
    public long m_infoOwnerSegId = 0;
    /** The segment under which the merge occured. */
    public long m_parentOfMergeSegId = 0;

    /**
     * The offset value to be added (merge) or subtracted (split) from
     * each subflow beloging to the owner m_infoOwnerSegId.
     *
     * For instance, the offset for all subs belonging to the first
     * segment would be 0, the second segment offest would be 100, the
     * third segment offset 200 and so on in increments of one hundred.
     */
    public int m_offset = 0;

    /**
     * Creates a new instance of SubflowInfo.
     *
     * @param p_infoOwnerSegId The segment which this information is for.
     * @param p_parentOfMergeSegId The segment under which the merge occured
     * @param p_offset The offset value to be added (merge) or
     * subtracted (split) from each subflow beloging to the owner
     * m_infoOwnerSegId
     */
    public SubflowMergeInfo(long p_infoOwnerSegId, long p_parentOfMergeSegId,
        int p_offset)
    {
        m_infoOwnerSegId = p_infoOwnerSegId;
        m_parentOfMergeSegId = p_parentOfMergeSegId;
        m_offset = p_offset;
    }
}
