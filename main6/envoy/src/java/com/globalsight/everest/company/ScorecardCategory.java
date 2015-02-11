package com.globalsight.everest.company;

public class ScorecardCategory
{
    private long id;
    private String scorecardCategory;
    private long companyId;
    
    public long getId()
    {
        return id;
    }
    public void setId(long id)
    {
        this.id = id;
    }
    public String getScorecardCategory()
    {
        return scorecardCategory;
    }
    public void setScorecardCategory(String scorecardCategory)
    {
        this.scorecardCategory = scorecardCategory;
    }
    public long getCompanyId()
    {
        return companyId;
    }
    public void setCompanyId(long companyId)
    {
        this.companyId = companyId;
    }
}
