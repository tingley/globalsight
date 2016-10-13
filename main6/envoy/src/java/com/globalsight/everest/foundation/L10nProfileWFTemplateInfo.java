package com.globalsight.everest.foundation;

public class L10nProfileWFTemplateInfo 
{
	private L10nProfileWFTemplateInfoKey key;
	private boolean isActive = true;
	public L10nProfileWFTemplateInfoKey getKey()
	{
		return key;
	}
	public void setKey(L10nProfileWFTemplateInfoKey key) 
	{
		this.key = key;
	}
	public boolean getIsActive() 
	{
		return isActive;
	}
	public void setIsActive(boolean isActive)
	{
		this.isActive = isActive;
	}
	
}
