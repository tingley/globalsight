package com.globalsight.ui.attribute;

public class ColumnInfo
{
    private String value;
    private boolean fromSuprerCompany;
    
    public ColumnInfo()
    {}
    
    public ColumnInfo(String value, boolean fromSuprerCompany)
    {
        this.value = value;
        this.fromSuprerCompany = fromSuprerCompany;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    public boolean isFromSuprerCompany()
    {
        return fromSuprerCompany;
    }

    public void setFromSuprerCompany(boolean fromSuprerCompany)
    {
        this.fromSuprerCompany = fromSuprerCompany;
    }

    @Override
    public String toString()
    {
        return value;
    }
}
