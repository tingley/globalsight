package com.globalsight.connector.eloqua.models;

import java.util.List;

public class HtmlContent
{
    private String type;
    private String docType;
    private String htmlBody;
    private List<String> metaTags;
    private String root;
    private String systemHeader;

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getDocType()
    {
        return docType;
    }

    public void setDocType(String docType)
    {
        this.docType = docType;
    }

    public String getHtmlBody()
    {
        return htmlBody;
    }

    public void setHtmlBody(String htmlBody)
    {
        this.htmlBody = htmlBody;
    }

    public List<String> getMetaTags()
    {
        return metaTags;
    }

    public void setMetaTags(List<String> metaTags)
    {
        this.metaTags = metaTags;
    }

    public String getRoot()
    {
        return root;
    }

    public void setRoot(String root)
    {
        this.root = root;
    }

    public String getSystemHeader()
    {
        return systemHeader;
    }

    public void setSystemHeader(String systemHeader)
    {
        this.systemHeader = systemHeader;
    }
}
