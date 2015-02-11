package com.globalsight.everest.foundation;

import java.io.Serializable;

public class L10nProfileWFTemplateInfoKey implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private long l10nProfileId;
	private long wfTemplateId;
	public long getL10nProfileId() 
	{
		return l10nProfileId;
	}
	public void setL10nProfileId(long profileId) 
	{
		l10nProfileId = profileId;
	}
	public long getWfTemplateId() 
	{
		return wfTemplateId;
	}
	public void setWfTemplateId(long wfTemplateId) 
	{
		this.wfTemplateId = wfTemplateId;
	}
}
