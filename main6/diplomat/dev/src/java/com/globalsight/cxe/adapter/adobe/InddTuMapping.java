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
package com.globalsight.cxe.adapter.adobe;

import java.util.Date;

import com.globalsight.everest.persistence.PersistentObject;

/**
 * This class only represent a wrapper object for the company names defined in
 * Envoy database and is used for defining a COTI project
 * 
 */
public class InddTuMapping extends PersistentObject
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    private long jobId;
    private long srcPageId;
    private long tuId;
    private long companyId;
    private int pageNum;
    
    public long getJobId()
    {
        return jobId;
    }

    public void setJobId(long jobId)
    {
        this.jobId = jobId;
    }

    public long getSrcPageId()
    {
        return srcPageId;
    }

    public void setSrcPageId(long srcPageId)
    {
        this.srcPageId = srcPageId;
    }

    public long getTuId()
    {
        return tuId;
    }

    public void setTuId(long tuId)
    {
        this.tuId = tuId;
    }

    public long getCompanyId()
    {
        return companyId;
    }

    public void setCompanyId(long companyId)
    {
        this.companyId = companyId;
    }

    public int getPageNum()
    {
        return pageNum;
    }

    public void setPageNum(int pageNum)
    {
        this.pageNum = pageNum;
    }

}
