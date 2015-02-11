package com.globalsight.ling.docproc.extractor.xml;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class XmlFilterProcessIns
{    
    private String name = null;
    private int handleType = -1;
    
    public XmlFilterProcessIns()
    {
    }
    
    public XmlFilterProcessIns(Element tagElement)
    {
        Node tagNameElement = tagElement.getElementsByTagName("aName").item(0);
        name = tagNameElement.getFirstChild().getNodeValue();

        Node modeElement = tagElement.getElementsByTagName("handleType").item(0);
        String tt = modeElement.getFirstChild().getNodeValue();

        try
        {
            handleType = Integer.parseInt(tt);
        }
        catch (Exception e)
        {
            handleType = -1;
        }
    }

    public String getName()
    {
        return name;
    }

    public int getHandleType()
    {
        return handleType;
    }

    public boolean isNameMatched(String aName)
    {
        if (aName == null)
        {
            return false;
        }
        else
        {
            return aName.equals(name);
        }
    }
}
