package com.globalsight.cxe.adapter.msoffice.excel;

import org.w3c.dom.Node;

public class Cell
{
    private String r;
    private String c;
    private Node node;
    private boolean fromSharedString = false;
    private String ssId;

    public String getR()
    {
        return r;
    }

    public void setR(String r)
    {
        this.r = r;
    }

    public String getC()
    {
        return c;
    }

    public void setC(String c)
    {
        this.c = c;
    }

    public Node getNode()
    {
        return node;
    }

    public void setNode(Node node)
    {
        this.node = node;
    }

    public boolean isFromSharedString()
    {
        return fromSharedString;
    }

    public void setFromSharedString(boolean fromSharedString)
    {
        this.fromSharedString = fromSharedString;
    }

    public String getSsId()
    {
        return ssId;
    }

    public void setSsId(String ssId)
    {
        this.ssId = ssId;
    }
}
