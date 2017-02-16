package com.globalsight.connector.blaise.form;

/**
 * Attribute object for Blaise connector setting
 */
public class BlaiseConnectorAttribute
{
    private long id;
    private long blaiseConnectorId;
    private long attributeId;
    private String attributeValue;
    private String attributeType;
    private String blaiseJobType;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public long getBlaiseConnectorId()
    {
        return blaiseConnectorId;
    }

    public void setBlaiseConnectorId(long blaiseConnectorId)
    {
        this.blaiseConnectorId = blaiseConnectorId;
    }

    public long getAttributeId()
    {
        return attributeId;
    }

    public void setAttributeId(long attributeId)
    {
        this.attributeId = attributeId;
    }

    public String getAttributeValue()
    {
        return attributeValue;
    }

    public void setAttributeValue(String attributeValue)
    {
        this.attributeValue = attributeValue;
    }

    public String getAttributeType()
    {
        return attributeType;
    }

    public void setAttributeType(String attributeType)
    {
        this.attributeType = attributeType;
    }

    public String getBlaiseJobType()
    {
        return blaiseJobType;
    }

    public void setBlaiseJobType(String blaiseJobType)
    {
        this.blaiseJobType = blaiseJobType;
    }
}
