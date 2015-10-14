package com.globalsight.everest.company;

import com.globalsight.everest.persistence.PersistentObject;

public class Category extends PersistentObject
{
	private static final long serialVersionUID = -3683727712213438456L;

	private long id;
    private String category;
    private long companyId;
    
    public long getId()
    {
        return id;
    }
    public void setId(long id)
    {
        this.id = id;
    }
    public String getCategory()
    {
        return category;
    }
    public void setCategory(String category)
    {
        this.category = category;
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
