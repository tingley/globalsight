package com.plug.Version_7_1_6_0;

public class HeaderTranslateCompany
{
    private String headerTranslate;
    private long companyId;
    private String fileProfileName;
    
    public HeaderTranslateCompany(String headerTranslate, long companyId,
            String fileProfileName)
    {
        super();
        this.headerTranslate = headerTranslate;
        this.companyId = companyId;
        this.fileProfileName = fileProfileName;
    }

    public String getFileProfileName()
    {
        return fileProfileName;
    }

    public void setFileProfileName(String fileProfileName)
    {
        this.fileProfileName = fileProfileName;
    }

    public String getHeaderTranslate()
    {
        return headerTranslate;
    }

    public void setHeaderTranslate(String headerTranslate)
    {
        this.headerTranslate = headerTranslate;
    }

    public long getCompanyId()
    {
        return companyId;
    }

    public void setCompanyId(long companyId)
    {
        this.companyId = companyId;
    }

    public HeaderTranslateCompany(String headerTranslate, long companyId)
    {
        super();
        this.headerTranslate = headerTranslate;
        this.companyId = companyId;
    }
    public String toString()
    {
        return "headerTranslate:"+headerTranslate+", companyId:"+companyId;
    }
}
