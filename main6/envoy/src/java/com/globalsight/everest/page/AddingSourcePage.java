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

package com.globalsight.everest.page;

import java.io.File;

import com.globalsight.everest.foundation.BasicL10nProfile;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.GlobalSightLocale;

public class AddingSourcePage extends Page
{
    private static final long serialVersionUID = -8422131105550711644L;
    
    private long jobId = -1;
    private long l10nProfileId = -1;
    private String dataSource = "Unknown";
    
    @Override
    protected void createPrimaryFile(int p_type)
    {

    }
    
    public AddingSourcePage()
    {
        super();
    }
    

    @Override
    public GlobalSightLocale getGlobalSightLocale()
    {
        return null;
    }

    @Override
    public long getLocaleId()
    {
        return 0;
    }

    public File getFile()
    {
        String filePath = AmbFileStoragePathUtils.getCxeDocDirPath()
                + File.separator + SourcePage.filtSpecialFile(getExternalPageId());
        File file = new File(filePath);
        if (!file.exists())
        {
            file = null;
        }
        
        return file;
    }

    public long getJobId()
    {
        return jobId;
    }

    public void setJobId(long jobId)
    {
        this.jobId = jobId;
    }
    
    public boolean equal(SourcePage page)
    {
        String path = this.getExternalPageId();
        String path2 = page.getExternalPageId();
        if (path == null || path2 == null)
        {
            return false;
        }
        
        path = path.replace("\\", "/").trim();
        path2 = path2.replace("\\", "/").trim();
        return path.equals(path2);
    }

    public long getL10nProfileId()
    {
        return l10nProfileId;
    }

    public void setL10nProfileId(long profileId)
    {
        l10nProfileId = profileId;
    }
    
    public BasicL10nProfile getL10nProfile()
    {
        return HibernateUtil.get(BasicL10nProfile.class, l10nProfileId);
    }
    
    public long getProjectId()
    {
        BasicL10nProfile p = HibernateUtil.get(BasicL10nProfile.class, l10nProfileId);
        if (p != null)
        {
            return p.getProjectId();
        }
        
        return -1;
    }

    public String getDataSource()
    {
        return dataSource;
    }

    public void setDataSource(String dataSource)
    {
        this.dataSource = dataSource;
    }
}
