package com.plug.Version_7_1_6_0;

public class RuleCompany
{
    private long xmlRuleId;
    private long companyId;
    private String fileProfileName;
    
    public RuleCompany(long xmlRuleId, long companyId, String fileProfileName)
    {
        super();
        this.xmlRuleId = xmlRuleId;
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

    public long getXmlRuleId()
    {
        return xmlRuleId;
    }

    public void setXmlRuleId(long xmlRuleId)
    {
        this.xmlRuleId = xmlRuleId;
    }

    public long getCompanyId()
    {
        return companyId;
    }

    public void setCompanyId(long companyId)
    {
        this.companyId = companyId;
    }

    public RuleCompany(long xmlRuleId, long companyId)
    {
        this.xmlRuleId = xmlRuleId;
        this.companyId = companyId;
    }

    public String toString()
    {
        return "xmlRuleId:"+xmlRuleId+",companyId:"+companyId;
    }
}
