package com.globalsight.ling.docproc.extractor.html;

public interface IHtmlExtractor
{
    String getFileProfileId();

    void setFileProfileId(String fileProfileId);

    long getFilterId();

    void setFilterId(long filterId);
    
    void setJsFunctionText(String jsFunctionText);
    
    String getJsFunctionText();
}
