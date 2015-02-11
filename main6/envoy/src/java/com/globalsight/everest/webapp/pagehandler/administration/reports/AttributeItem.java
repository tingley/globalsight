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

package com.globalsight.everest.webapp.pagehandler.administration.reports;

import com.globalsight.cxe.entity.customAttribute.Attribute;
import com.globalsight.cxe.entity.customAttribute.AttributeClone;
import com.globalsight.everest.company.CompanyWrapper;

public class AttributeItem
{
    private long id;
    private String name;
    private boolean fromSuper;
    private boolean isNumber;
    private boolean total;

    public AttributeItem()
    {
        
    }
    
    public AttributeItem(AttributeClone clone)
    {
        setId(clone.getId());
        setName(clone.getDisplayName());
        setFromSuper(CompanyWrapper.SUPER_COMPANY_ID.equals(clone
                .getCompanyId()));
        setNumber(Attribute.TYPE_FLOAT.equals(clone.getType())
                || Attribute.TYPE_INTEGER.equals(clone.getType()));
    }
    
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public boolean isFromSuper()
    {
        return fromSuper;
    }

    public void setFromSuper(boolean fromSuper)
    {
        this.fromSuper = fromSuper;
    }

    public boolean isNumber()
    {
        return isNumber;
    }

    public void setNumber(boolean isNumber)
    {
        this.isNumber = isNumber;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (fromSuper ? 1231 : 1237);
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AttributeItem other = (AttributeItem) obj;
        if (fromSuper != other.fromSuper)
            return false;
        if (name == null)
        {
            if (other.name != null)
                return false;
        }
        else if (!name.equals(other.name))
            return false;
        return true;
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String getDndType()
    {
        if (isNumber)
        {
            return "number";
        }
        
        return "text";
    }
    
    public static String getAllDndType()
    {
        return "number, text";
    }

    public boolean isTotal()
    {
        return total;
    }

    public void setTotal(boolean total)
    {
        this.total = total;
    }
    
}
