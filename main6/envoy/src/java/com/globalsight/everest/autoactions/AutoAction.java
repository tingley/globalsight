package com.globalsight.everest.autoactions;

import com.globalsight.everest.persistence.PersistentObject;

public class AutoAction extends PersistentObject
{

    private static final long serialVersionUID = -676241146976859445L;

    private String name = "";
    private String email = "";
    private String description = "";
    private long company_id;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public long getCompanyID()
    {

        return company_id;
    }

    public void setCompanyID(long p_company)
    {
        this.company_id = p_company;
    }

}
