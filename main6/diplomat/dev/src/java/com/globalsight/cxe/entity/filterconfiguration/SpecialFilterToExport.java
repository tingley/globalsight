package com.globalsight.cxe.entity.filterconfiguration;

public class SpecialFilterToExport
{
    private Long specialFilterId;
    private String filterTableName;

    public SpecialFilterToExport(Long specialFilterId,
            String filterTableName)
    {
        this.specialFilterId = specialFilterId;
        this.filterTableName = filterTableName;
    }

    public SpecialFilterToExport()
    {
        super();
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
}
