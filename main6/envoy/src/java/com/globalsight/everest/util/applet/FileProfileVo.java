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

package com.globalsight.everest.util.applet;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.fileextension.FileExtension;
import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.cxe.persistence.fileprofile.FileProfilePersistenceManagerLocal;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.servlet.util.ServerProxy;

@XmlRootElement
public class FileProfileVo
{
    private static final Logger CATEGORY = Logger
            .getLogger(FileProfileVo.class);

    private long id;

    private String name;

    private long projectId;
    
    private long l10nProfileId;

	private List<String> fileExtensions;

    private String sourceLocale;

    public void init(FileProfile fileProfile)
    {
        fileExtensions = new ArrayList<String>();

        setId(fileProfile.getId());
        setName(fileProfile.getName());
//        setSourceLocale(sourceLocale)
        
        FileProfilePersistenceManagerLocal local = new FileProfilePersistenceManagerLocal();
        Object[] extensions;
        try
        {
            extensions = local.getFileExtensionsByFileProfile(fileProfile)
                    .toArray();
            for (int i = 0; i < extensions.length; i++)
            {
                FileExtension fe = (FileExtension) extensions[i];
                String extName = fe.getName();
                fileExtensions.add(extName.toLowerCase());
            }
        }
        catch (Exception e1)
        {
            CATEGORY.error(e1.getMessage(), e1);
        }

        l10nProfileId = fileProfile.getL10nProfileId();
        L10nProfile lp;
        try
        {
            lp = ServerProxy.getProjectHandler().getL10nProfile(l10nProfileId);
            setProjectId(lp.getProject().getId());
            setL10nProfileId(l10nProfileId);
        }
        catch (Exception e)
        {
            CATEGORY.error(e.getMessage(), e);
        }
    }

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
}
