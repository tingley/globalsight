/**
 * Copyright 2009 Welocalize, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * <p>
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.globalsight.everest.page;

import jodd.util.StringUtil;

public class JobSourcePageDisplay
{
    private SourcePage sourcePage;
    private String pageUrl;
    private String dataSourceName;
    private boolean isWordCountOverriden;
    private String sourceLink;
    private boolean isImageFile;
    private boolean isActiveFileProfile;
    private long uiFileProfileId;
    // GBS-4749
    private long blaiseEntryId;
    private String blaiseStateUploadComplete = "--/--";

    public JobSourcePageDisplay(SourcePage sourcePage)
    {
        this.sourcePage = sourcePage;
    }

    public SourcePage getSourcePage()
    {
        return sourcePage;
    }

    public String getPageUrl()
    {
        return pageUrl;
    }

    public void setPageUrl(String pageUrl)
    {
        this.pageUrl = pageUrl;
    }

    public String getDataSourceName()
    {
        return dataSourceName;
    }

    public void setDataSourceName(String dataSourceName)
    {
        this.dataSourceName = dataSourceName;
    }

    public boolean getIsImageFile()
    {
        return isImageFile;
    }

    public void setImageFile(boolean isImageFile)
    {
        this.isImageFile = isImageFile;
    }

    public boolean getIsWordCountOverriden()
    {
        return isWordCountOverriden;
    }

    public void setWordCountOverriden(boolean isWordCountOverriden)
    {
        this.isWordCountOverriden = isWordCountOverriden;
    }

    public String getSourceLink()
    {
        return sourceLink;
    }

    public void setSourceLink(String sourceLink)
    {
        this.sourceLink = sourceLink;
    }

    public void setSourcePage(SourcePage sourcePage)
    {
        this.sourcePage = sourcePage;
    }

    public boolean getIsActiveFileProfile()
    {
        return isActiveFileProfile;
    }

    public void setActiveFileProfile(boolean isActiveFileProfile)
    {
        this.isActiveFileProfile = isActiveFileProfile;
    }

    public long getUiFileProfileId()
    {
        return uiFileProfileId;
    }

    public void setUiFileProfileId(long uiFileProfileId)
    {
        this.uiFileProfileId = uiFileProfileId;
    }

    public long getBlaiseEntryId()
    {
        return blaiseEntryId;
    }

    public void setBlaiseEntryId(long blaiseEntryId)
    {
        this.blaiseEntryId = blaiseEntryId;
    }

    public String getBlaiseStateUploadComplete()
    {
        return blaiseStateUploadComplete;
    }

    public void setBlaiseStateUploadComplete(String blaiseStateUploadComplete)
    {
        this.blaiseStateUploadComplete = blaiseStateUploadComplete;
    }

    public void setBlaiseStateUploadComplete(String blaiseUploadState, String blaiseCompleteState)
    {
        if (StringUtil.isEmpty(blaiseUploadState))
        {
            blaiseUploadState = "--";
        }
        if (StringUtil.isEmpty(blaiseCompleteState))
        {
            blaiseCompleteState = "--";
        }
        setBlaiseStateUploadComplete(blaiseUploadState + "/" + blaiseCompleteState);
    }
}
