package com.plug.Version_7_1_6_0;

public class SidEscapeCompany
{
    private String enableSidSupport;
    private String unicodeEscape;
    private long companyId;
    private String fileProfileName;

    public SidEscapeCompany(String enableSidSupport, String unicodeEscape,
            long companyId, String fileProfileName)
    {
        super();
        this.enableSidSupport = enableSidSupport;
        this.unicodeEscape = unicodeEscape;
        this.companyId = companyId;
        this.fileProfileName = fileProfileName;
    }

    public String getEnableSidSupport()
    {
        return enableSidSupport;
    }

    public void setEnableSidSupport(String enableSidSupport)
    {
        this.enableSidSupport = enableSidSupport;
    }

    public String getUnicodeEscape()
    {
        return unicodeEscape;
    }

    public void setUnicodeEscape(String unicodeEscape)
    {
        this.unicodeEscape = unicodeEscape;
    }

    public long getCompanyId()
    {
        return companyId;
    }

    public void setCompanyId(long companyId)
    {
        this.companyId = companyId;
    }

    public SidEscapeCompany(String enableSidSupport, String unicodeEscape,
            long companyId)
    {
        super();
        this.enableSidSupport = enableSidSupport;
        this.unicodeEscape = unicodeEscape;
        this.companyId = companyId;
    }

    public String toString()
    {
        return "enableSidSupport: " + enableSidSupport + ",unicodeEscape: "
                + unicodeEscape + ",companyId:" + companyId;
    }

    public String getFileProfileName()
    {
        return fileProfileName;
    }

    public void setFileProfileName(String fileProfileName)
    {
        this.fileProfileName = fileProfileName;
    }
}
