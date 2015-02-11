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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.everest.projecthandler.ProjectImpl;

public class AttributeSet extends PersistentObject
{
    private static final long serialVersionUID = 2037545602039043777L;
    public static final String PROTECT_NAME_PREFIX = "protect_";

    private String name;
    private Set<Attribute> attributes = new HashSet<Attribute>();
    private long companyId;
    private String description;
    private Set<ProjectImpl> projects = new HashSet<ProjectImpl>();

    public void clearAttributes()
    {
        if (attributes != null)
        {
            attributes.clear();
        }
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Set<Attribute> getAttributes()
    {
        return attributes;
    }

    public List<Attribute> getAttributeAsList()
    {
        List<Attribute> atts = new ArrayList<Attribute>();
        if (attributes != null)
        {
            atts.addAll(attributes);
        }

        return atts;
    }

    public void setAttributes(Set<Attribute> attributes)
    {
        this.attributes = attributes;
    }

    public long getCompanyId()
    {
        return companyId;
    }

    public void setCompanyId(long companyId)
    {
        this.companyId = companyId;
    }

    public void addAttribute(Attribute attribute)
    {
        if (attributes == null)
        {
            attributes = new HashSet<Attribute>();
        }

        attributes.add(attribute);
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public Set<ProjectImpl> getProjects()
    {
        return projects;
    }

    public void setProjects(Set<ProjectImpl> projects)
    {
        this.projects = projects;
    }
}
