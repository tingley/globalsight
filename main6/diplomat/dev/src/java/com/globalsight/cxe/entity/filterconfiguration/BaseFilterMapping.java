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
package com.globalsight.cxe.entity.filterconfiguration;

/**
 * The mapping for base filter
 * 
 */
public class BaseFilterMapping
{
    private long id;
    private long baseFilterId;
    private long filterId;
    private String filterTableName;

    public BaseFilterMapping()
    {
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public long getBaseFilterId()
    {
        return baseFilterId;
    }

    public void setBaseFilterId(long baseFilterId)
    {
        this.baseFilterId = baseFilterId;
    }

    public long getFilterId()
    {
        return filterId;
    }

    public void setFilterId(long filterId)
    {
        this.filterId = filterId;
    }

    public String getFilterTableName()
    {
        return filterTableName;
    }

    public void setFilterTableName(String filterTableName)
    {
        this.filterTableName = filterTableName;
    }
}
