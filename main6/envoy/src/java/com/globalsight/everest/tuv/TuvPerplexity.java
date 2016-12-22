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
package com.globalsight.everest.tuv;

import com.globalsight.everest.persistence.PersistentObject;

public class TuvPerplexity extends PersistentObject
{
    private static final long serialVersionUID = 2485852038352861608L;

    // For GBS-4495 perplexity score on MT
    private double perplexitySource;
    private double perplexityTarget;
    private boolean perplexityResult;
    private long companyId;
    private long tuvId;

    public double getPerplexitySource()
    {
        return perplexitySource;
    }

    public void setPerplexitySource(double perplexitySource)
    {
        this.perplexitySource = perplexitySource;
    }

    public double getPerplexityTarget()
    {
        return perplexityTarget;
    }

    public void setPerplexityTarget(double perplexityTarget)
    {
        this.perplexityTarget = perplexityTarget;
    }

    public boolean getPerplexityResult()
    {
        return perplexityResult;
    }

    public void setPerplexityResult(boolean perplexityResult)
    {
        this.perplexityResult = perplexityResult;
    }

    public long getCompanyId()
    {
        return companyId;
    }

    public void setCompanyId(long companyId)
    {
        this.companyId = companyId;
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
