package com.globalsight.everest.page;

public class JobSourcePageDisplay
{
    private SourcePage sourcePage;
    private String pageUrl;
    private String dataSourceName;
    private boolean isWordCountOverriden;
    private String sourceLink;
    private boolean isImageFile;
    
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

    public boolean getIsWordCountOverriden()
    {
        return isWordCountOverriden;
    }

    public void setWordCountOverriden(boolean isWordCountOverriden)
    {
        this.isWordCountOverriden = isWordCountOverriden;
    }
    
    public boolean getIsImageFile()
    {
        return isImageFile;
    }

    public void setImageFile(boolean isImageFile)
    {
        this.isImageFile = isImageFile;
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
}
