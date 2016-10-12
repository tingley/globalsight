package com.globalsight.everest.page;

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
    
    public JobSourcePageDisplay(SourcePage sourcePage){
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
}
