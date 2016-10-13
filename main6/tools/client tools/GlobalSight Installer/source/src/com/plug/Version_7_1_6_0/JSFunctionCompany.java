package com.plug.Version_7_1_6_0;

public class JSFunctionCompany
{
    private long companyId;
    private String jsFunction;
    private String fileProfileName;
    public JSFunctionCompany(long companyId, String jsFunction,
            String fileProfileName)
    {
        super();
        this.companyId = companyId;
        this.jsFunction = jsFunction;
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

    public JSFunctionCompany(long companyId, String jsFunction)
    {
        this.companyId = companyId;
        this.jsFunction = jsFunction;
    }

    public long getCompanyId()
    {
        return companyId;
    }

    public void setCompanyId(long companyId)
    {
        this.companyId = companyId;
    }

    public String getJsFunction()
    {
        return jsFunction;
    }

    public void setJsFunction(String jsFunction)
    {
        this.jsFunction = jsFunction;
    }
    public String toString()
    {
        return "jsFunction:"+jsFunction+", companyId:"+companyId;
    }
}
