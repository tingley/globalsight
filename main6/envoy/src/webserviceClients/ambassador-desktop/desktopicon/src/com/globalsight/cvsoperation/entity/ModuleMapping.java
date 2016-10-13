package com.globalsight.cvsoperation.entity;

public class ModuleMapping {
	private String id;
	private String sourceLocale;
	private String fullSourceLocale;
	private String sourceModule;
	private String targetLocale;
	private String fullTargetLocale;
	private String targetModule;
	private String userID;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getSourceLocale() {
		return sourceLocale;
	}
	public void setSourceLocale(String sourceLocale) {
		this.sourceLocale = sourceLocale;
	}
	public String getFullSourceLocale() {
		return fullSourceLocale;
	}
	public void setFullSourceLocale(String fullSourceLocale) {
		this.fullSourceLocale = fullSourceLocale;
	}
	public String getSourceModule() {
		return sourceModule;
	}
	public void setSourceModule(String sourceModule) {
		this.sourceModule = sourceModule;
	}
	public String getTargetLocale() {
		return targetLocale;
	}
	public void setTargetLocale(String targetLocale) {
		this.targetLocale = targetLocale;
	}
	public String getFullTargetLocale() {
		return fullTargetLocale;
	}
	public void setFullTargetLocale(String fullTargetLocale) {
		this.fullTargetLocale = fullTargetLocale;
	}
	public String getTargetModule() {
		return targetModule;
	}
	public void setTargetModule(String targetModule) {
		this.targetModule = targetModule;
	}
	public String getUserID() {
		return userID;
	}
	public void setUserID(String userID) {
		this.userID = userID;
	}
	
}
