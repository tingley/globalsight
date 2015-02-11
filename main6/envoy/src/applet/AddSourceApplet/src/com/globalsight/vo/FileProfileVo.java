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

package com.globalsight.vo;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author ares
 *
 */
@XmlRootElement
public class FileProfileVo
{
    private long id;

    private String name;

    private long projectId;
    
    private long l10nProfileId;

    private List<String> fileExtensions;

    private String sourceLocale;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public long getProjectId()
    {
        return projectId;
    }

    public void setProjectId(long projectId)
    {
        this.projectId = projectId;
    }

    public long getL10nProfileId()
    {
		return l10nProfileId;
	}

	public void setL10nProfileId(long l10nProfileId)
	{
		this.l10nProfileId = l10nProfileId;
	}
    
    public String getSourceLocale()
    {
        return sourceLocale;
    }

    public void setSourceLocale(String sourceLocale)
    {
        this.sourceLocale = sourceLocale;
    }

    public List<String> getFileExtensions()
    {
        return fileExtensions;
    }

    public void setFileExtensions(List<String> fileExtensions)
    {
        this.fileExtensions = fileExtensions;
    }

    @Override
    public String toString()
    {
        return name;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (id ^ (id >>> 32));
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
        FileProfileVo other = (FileProfileVo) obj;
        if (id != other.id)
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
}
