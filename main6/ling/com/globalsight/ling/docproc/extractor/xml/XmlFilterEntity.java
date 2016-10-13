package com.globalsight.ling.docproc.extractor.xml;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class XmlFilterEntity
{
    private String entityName = null;
    private int handleType = -1;
    private int saveAs = -1;
    private long entityCode = -1;
    
    public XmlFilterEntity()
    {
    }
    
    public XmlFilterEntity(Element tagElement)
    {
        Node tagNameElement = tagElement.getElementsByTagName("aName").item(0);
        entityName = tagNameElement.getFirstChild().getNodeValue();

        Node modeElement = tagElement.getElementsByTagName("entityType").item(0);
        String tt = modeElement.getFirstChild().getNodeValue();

        try
        {
            handleType = Integer.parseInt(tt);
        }
        catch (Exception e)
        {
            handleType = -1;
        }
        
        Node saveasElement = tagElement.getElementsByTagName("saveAs").item(0);
        String sa = saveasElement.getFirstChild().getNodeValue();

        try
        {
            saveAs = Integer.parseInt(sa);
        }
        catch (Exception e)
        {
            saveAs = 0;
        }
        
        Node codeElement = tagElement.getElementsByTagName("entityCode").item(0);
        String cd = codeElement.getFirstChild().getNodeValue();
        
        try
        {
            entityCode = Long.parseLong(cd);
        }
        catch (Exception e)
        {
            entityCode = 0;
        }
    }

    public String getEntityName()
    {
        return entityName;
    }

    public int getHandleType()
    {
        return handleType;
    }

    public int getSaveAs()
    {
        return saveAs;
    }

    public long getEntityCode()
    {
        return entityCode;
    }
    
    public String getEntityCharacter()
    {
        return entityCode > 0 ? "" + (char) entityCode : "&" + entityName + ";";
    }
    
    public boolean isNameMatched(String aName)
    {
        if (aName == null)
        {
            return false;
        }
        else
        {
            return aName.equals(entityName);
        }
    }
}
