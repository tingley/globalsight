package com.globalsight.everest.company;

import com.globalsight.everest.persistence.PersistentObject;

public class ScorecardCategory extends PersistentObject
{
	private static final long serialVersionUID = 5195461040470880414L;

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
