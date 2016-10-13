package com.globalsight.cxe.entity.filterconfiguration;

public class SpecialFilterToDelete implements Comparable
{
    private int topFilterId;
    private Long specialFilterId;
    private String filterTableName;
    private String color;

    public SpecialFilterToDelete(int topFilterId, Long specialFilterId,
            String filterTableName, String color)
    {
        this.topFilterId = topFilterId;
        this.specialFilterId = specialFilterId;
        this.filterTableName = filterTableName;
        this.color = color;
    }

    public SpecialFilterToDelete()
    {
        super();
    }

    public int getTopFilterId()
    {
        return topFilterId;
    }

    public void setTopFilterId(int topFilterId)
    {
        this.topFilterId = topFilterId;
    }

    public Long getSpecialFilterId()
    {
        return specialFilterId;
    }

    public void setSpecialFilterId(Long specialFilterId)
    {
        this.specialFilterId = specialFilterId;
    }

    public String getFilterTableName()
    {
        return filterTableName;
    }

    public void setFilterTableName(String filterTableName)
    {
        this.filterTableName = filterTableName;
    }

    public String getColor()
    {
        return color;
    }

    public void setColor(String color)
    {
        this.color = color;
    }

    public int compareTo(Object o)
    {
        SpecialFilterToDelete that = null;
        if (o instanceof SpecialFilterToDelete)
        {
            that = (SpecialFilterToDelete) o;
        }
        if (that == null)
        {
            return 1;
        }
        if (this.getTopFilterId() > that.getTopFilterId())
        {
            return 1;
        }
        else if (this.getTopFilterId() == that.getTopFilterId())
        {
            return 0;
        }
        else
        {
            return -1;
        }
    }
}
