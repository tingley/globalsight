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

package com.globalsight.everest.workflowmanager;

public class ArrorInfo
{
    private long sourceId;
    private String arrorName;

    public ArrorInfo(long sourceId, String arrorName)
    {
        this.sourceId = sourceId;
        this.arrorName = arrorName;
    }

    public long getSourceId()
    {
        return sourceId;
    }

    public void setSourceId(long sourceId)
    {
        this.sourceId = sourceId;
    }

    public String getArrorName()
    {
        return arrorName;
    }

    public void setArrorName(String arrorName)
    {
        this.arrorName = arrorName;
    }
    
    
}
