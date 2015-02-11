package com.globalsight.everest.webapp.pagehandler.administration.users;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import com.globalsight.everest.costing.Rate;
import com.globalsight.everest.localemgr.LocaleManagerException;
import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;

public class UserDefaultRole extends PersistentObject{
	private static final long serialVersionUID = -5219287185562542048L;

	private long sourceLocaleId = 0l;
	private long targetLocaleId = 0l;
	private GlobalSightLocale sourceLocaleObject = null;
	private GlobalSightLocale targetLocaleObject = null;
	private String userId = "";
	private Set activities = null;
	private String status = "";
	
	public long getSourceLocaleId() {
		return sourceLocaleId;
	}
	public void setSourceLocaleId(long sourceLocaleId) {
		this.sourceLocaleId = sourceLocaleId;
	}
	public long getTargetLocaleId() {
		return targetLocaleId;
	}
	public void setTargetLocaleId(long targetLocaleId) {
		this.targetLocaleId = targetLocaleId;
	}
	public GlobalSightLocale getSourceLocaleObject() {
		try {
			if (sourceLocaleObject == null)
				this.sourceLocaleObject = ServerProxy.getLocaleManager().getLocaleById(sourceLocaleId);
		} catch (Exception e) {
			this.sourceLocaleObject = new GlobalSightLocale();
		}
		return this.sourceLocaleObject;
	}
	public void setSourceLocaleObject(GlobalSightLocale sourceLocaleObject) {
		this.sourceLocaleObject = sourceLocaleObject;
	}
	public GlobalSightLocale getTargetLocaleObject() {
		try {
			if (targetLocaleObject == null)
				this.targetLocaleObject = ServerProxy.getLocaleManager().getLocaleById(targetLocaleId);
		} catch (Exception e) {
			this.targetLocaleObject = new GlobalSightLocale();
		}
		return targetLocaleObject;
	}
	public void setTargetLocaleObject(GlobalSightLocale targetLocaleObject) {
		this.targetLocaleObject = targetLocaleObject;
	}
	public Set getActivities() {
		return activities;
	}
	public void setActivities(Set activities) {
		this.activities = activities;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	public void removeActivity(UserDefaultActivity act) {
		this.getActivities().remove(act);
	}
	
}
