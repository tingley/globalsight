package com.globalsight.everest.webapp.pagehandler.administration.users;

import com.globalsight.everest.persistence.PersistentObject;

public class UserDefaultActivity extends PersistentObject {

	private static final long serialVersionUID = -6588788398390073497L;
	
	private String activityName = "";
	private UserDefaultRole defaultRole = new UserDefaultRole();
	
	public String getActivityName() {
		return activityName;
	}
	public void setActivityName(String activityName) {
		this.activityName = activityName;
	}
	public UserDefaultRole getDefaultRole() {
		return defaultRole;
	}
	public void setDefaultRole(UserDefaultRole defaultRole) {
		this.defaultRole = defaultRole;
	}
	
	

}
