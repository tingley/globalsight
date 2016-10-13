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

package com.globalsight.cxe.entity.customAttribute;

import java.util.HashSet;
import java.util.Set;

import com.globalsight.everest.persistence.PersistentObject;

public class Attribute extends PersistentObject
{
    private static final long serialVersionUID = -9043113788985393187L;

    public static final String PROTECT_NAME_PREFIX = "protect_";

    public static final String TYPE_TEXT = "text";
    public static final String TYPE_CHOICE_LIST = "choiceList";
    public static final String TYPE_DATE = "date";
    public static final String TYPE_INTEGER = "integer";
    public static final String TYPE_FLOAT = "float";
    public static final String TYPE_FILE = "file";

    private String name;
    private String displayName;
    private boolean visible;
    private boolean editable;
    private boolean required;
    private long companyId;
    private Set<AttributeSet> attributeSets = new HashSet<AttributeSet>();
    // private Set<JobAttribute> jobAttributes = new HashSet<JobAttribute>();;
    private String description;

    private Condition condition;

    public String getType()
    {
        if (condition == null)
            return TYPE_TEXT;

        return condition.getType();
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public boolean getEditable()
    {
        return editable;
    }

    public void setEditable(boolean editable)
    {
        this.editable = editable;
    }

    public long getCompanyId()
    {
        return companyId;
    }

    public void setCompanyId(long companyId)
    {
        this.companyId = companyId;
    }

    public boolean isVisible()
    {
        return visible;
    }

    public void setVisible(boolean visible)
    {
        this.visible = visible;
    }

    public boolean isRequired()
    {
        return required;
    }

    public void setRequired(boolean required)
    {
        this.required = required;
    }

    public Condition getCondition()
    {
        return condition;
    }

    public void setCondition(Condition condition)
    {
        this.condition = condition;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public Set<AttributeSet> getAttributeSets()
    {
        return attributeSets;
    }

    public void setAttributeSets(Set<AttributeSet> attributeSets)
    {
        this.attributeSets = attributeSets;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public void setDisplayName(String displayName)
    {
        this.displayName = displayName;
    }

    // public Set<JobAttribute> getJobAttributes()
    // {
    // return jobAttributes;
    // }
    //
    // public void setJobAttributes(Set<JobAttribute> jobAttributes)
    // {
    // this.jobAttributes = jobAttributes;
    // }

    public boolean removeable()
    {
        return !name.startsWith(PROTECT_NAME_PREFIX);
    }

    public AttributeClone getCloneAttribute()
    {
        AttributeClone clone = new AttributeClone();
        clone.setId(-1);
        clone.setCompanyId(this.getCompanyId());
        clone.setCondition(this.getCondition().getCloneCondition());
        clone.setDescription(this.getDescription());
        clone.setEditable(this.getEditable());
        clone.setName(this.getName());
        clone.setDisplayName(this.displayName);
        clone.setRequired(this.isRequired());
        clone.setVisible(this.visible);
        return clone;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
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
        Attribute other = (Attribute) obj;
        if (name == null)
        {
            if (other.name != null)
                return false;
        }
        else if (!name.equals(other.name))
            return false;
        return true;
    }
}
