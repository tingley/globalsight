package com.globalsight.machineTranslation.iptranslator.request;

import java.io.Serializable;

public class Request implements Serializable
{
    private static final long serialVersionUID = 1L;

    /*
     * all api request requires a valid key
     */
    private String key;

    public Request()
    {

    }

    public Request(String key)
    {
        super();
        this.key = key;
    }

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }
}
