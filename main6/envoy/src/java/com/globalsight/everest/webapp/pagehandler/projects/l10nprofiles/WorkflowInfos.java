package com.globalsight.everest.webapp.pagehandler.projects.l10nprofiles;

import com.globalsight.util.GlobalSightLocale;

public class WorkflowInfos 
{

	private long lnProfileId;
	private long wfId;
    private long mtProfileId;
	private String name;
	private boolean isActive;
	private GlobalSightLocale targetLocale;
	public GlobalSightLocale getTargetLocale() {
		return targetLocale;
	}

	public void setTargetLocale(GlobalSightLocale targetLocale) {
		this.targetLocale = targetLocale;
	}
	
    public WorkflowInfos(long lnProfileId, long wfId, boolean isActive,
			GlobalSightLocale targetLocale) 
	{
		super();
		this.lnProfileId = lnProfileId;
		this.wfId = wfId;
		this.isActive = isActive;
		this.targetLocale = targetLocale;
	}

    public WorkflowInfos(long lnProfileId, long wfId, long mtProfileId,
            boolean isActive, GlobalSightLocale targetLocale)
    {
        super();
        this.lnProfileId = lnProfileId;
        this.wfId = wfId;
        this.mtProfileId = mtProfileId;
        this.isActive = isActive;
        this.targetLocale = targetLocale;
    }

	public WorkflowInfos(long lnProfileId, long wfId, boolean isActive) 
	{
		super();
		this.lnProfileId = lnProfileId;
		this.wfId = wfId;
		this.isActive = isActive;
	}
	
	public WorkflowInfos(long wfId, String name,
			boolean isActive) {
		super();
		this.wfId = wfId;
		this.name = name;
		this.isActive = isActive;
	}



    public long getLnProfileId()
    {
		return lnProfileId;
	}
	public void setLnProfileId(long lnProfileId) {
		this.lnProfileId = lnProfileId;
	}
	public long getWfId() {
		return wfId;
	}
	public void setWfId(long wfId) {
		this.wfId = wfId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isActive() {
		return isActive;
	}
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public boolean equals(Object o)
	{
		WorkflowInfos that = (WorkflowInfos)o;
		return (new Long(this.wfId).intValue() == new Long(that.wfId).intValue());
	}
	public int hashCode()
	{
		return new Long(this.lnProfileId).intValue() + new Long(this.wfId).intValue();
	}

    public long getMtProfileId()
    {
        return mtProfileId;
    }

    public void setMtProfileId(long mtProfileId)
    {
        this.mtProfileId = mtProfileId;
    }
}
