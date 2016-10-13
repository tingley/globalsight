/**
 * Copyright 2009 Welocalize, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * 
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 */

package com.globalsight.everest.category;

import com.globalsight.everest.persistence.PersistentObject;

/**
 * Category Object
 * @author VincentYan
 * @since 8.7.2
 */
public class CommonCategory extends PersistentObject
{
    private static final long serialVersionUID = 8590474927530159442L;

    private String memo;
    private int type;
    private long companyId;
    private String isAvailable;

    public String getMemo()
    {
        return memo;
    }

    public void setMemo(String memo)
    {
        this.memo = memo;
    }

    public int getType()
    {
        return type;
    }

    public void setType(int type)
    {
        this.type = type;
    }

    public long getCompanyId()
    {
        return companyId;
    }

    public void setCompanyId(long companyId)
    {
        this.companyId = companyId;
    }

    public boolean getIsAvailable()
    {
        return "Y".equalsIgnoreCase(isAvailable);
    }

    public void setIsAvailable(String isAvailable)
    {
        this.isAvailable = isAvailable;
    }

    public void setIsAvailable(boolean isAvailable)
    {
        this.isAvailable = isAvailable ? "Y" : "N";
    }

    public boolean isAvailable()
    {
        return "Y".equalsIgnoreCase(isAvailable);
    }

}
