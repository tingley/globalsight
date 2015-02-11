package com.globalsight.entity;

import java.util.Date;
import java.util.List;

import com.globalsight.util2.ConfigureHelperV2;

public class Job
{
	private String name = null;

	private Date createDate = null;

	private Date downDate = null;

	private User owner = null;

	private String downloadUser = null;

	private List files = null;
	
	private boolean isCVSJob = false;
	
	private String sourceLocale = null;

	public Job(String p_name, User p_owner, List p_files, Date p_cdate,
			Date p_downdate)
	{
		name = p_name;
		createDate = p_cdate;
		downDate = p_downdate;
		owner = p_owner;
		files = p_files;
		downloadUser = ConfigureHelperV2.v_na;
	}

	public Date getCreateDate()
	{
		return createDate;
	}

	public Date getDownDate()
	{
		return downDate;
	}

	public String getName()
	{
		return name;
	}

	public String toString()
	{
		return name + " (" + owner + ")";
	}

	public List getFileMappedList()
	{
		return files;
	}

	public User getOwner()
	{
		return owner;
	}

	public String getDownloadUser()
	{
		return downloadUser;
	}

	public void setDownloadUser(String downloadUser)
	{
		this.downloadUser = downloadUser;
	}

	public boolean isCVSJob() {
		return isCVSJob;
	}

	public void setCVSJob(boolean isCVSJob) {
		this.isCVSJob = isCVSJob;
	}

	public String getSourceLocale() {
		return sourceLocale;
	}

	public void setSourceLocale(String sourceLocale) {
		this.sourceLocale = sourceLocale;
	}
}
