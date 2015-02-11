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
package com.globalsight.everest.comment;

import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.everest.tuv.TuvImpl;

public class IssueEditionRelation extends PersistentObject
{
    private static final long serialVersionUID = -1275637165851849002L;

    private long originalTuId;
    private long originalTuvId;
    private String originalIssueHistoryId;
    private TuvImpl tuv = null;
    private long tuvId = 0;

    public IssueEditionRelation()
    {
    }

    public IssueEditionRelation(long p_originalTuId, long p_originalTuvId,
            TuvImpl p_newTuv, String p_originalIssueHistoryId)
    {
        this.originalTuId = p_originalTuId;
        this.originalTuvId = p_originalTuvId;
        this.originalIssueHistoryId = p_originalIssueHistoryId;
        this.tuv = p_newTuv;
        if (tuv != null)
        {
            tuvId = tuv.getId();
        }
    }

    public long getOriginalTuId()
    {
        return originalTuId;
    }

    public void setOriginalTuId(long originalTuId)
    {
        this.originalTuId = originalTuId;
    }

    public long getOriginalTuvId()
    {
        return originalTuvId;
    }

    public void setOriginalTuvId(long originalTuvId)
    {
        this.originalTuvId = originalTuvId;
    }

    public String getOriginalIssueHistoryId()
    {
        return originalIssueHistoryId;
    }

    public void setOriginalIssueHistoryId(String originalIssueHistoryId)
    {
        this.originalIssueHistoryId = originalIssueHistoryId;
    }

    public void setTuv(TuvImpl tuvimpl)
    {
        tuv = tuvimpl;
        if (tuv != null)
        {
            tuvId = tuv.getId();
        }
    }

    public long getTuvId()
    {
        return tuvId;
    }

    public void setTuvId(long tuvId)
    {
        this.tuvId = tuvId;
    }

}
