package com.globalsight.ling.docproc.extractor.html;

public class HtmlExtractorImpl implements IHtmlExtractor
{

    private String fileProfileId;
    private long filterId;
    private String jsFunctionText;
    public String getJsFunctionText()
    {
        return jsFunctionText;
    }

    public void setJsFunctionText(String jsFunctionText)
    {
        this.jsFunctionText = jsFunctionText;
    }

    public String getFileProfileId()
    {
        return fileProfileId;
    }

    public long getFilterId()
    {
        return filterId;
    }

    public void setFileProfileId(String fileProfileId)
    {
        this.fileProfileId = fileProfileId;
    }

    public void setFilterId(long filterId)
    {
        this.filterId = filterId;
    }

}
