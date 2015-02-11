package com.globalsight.cvsoperation.entity;

import java.util.ArrayList;

public class SourceModuleMapping {
	private static final long serialVersionUID = 397407735024546664L;
	private String ID = "";
	private String locale = "";
	private String fullLocale = "";
	private String module = ""; 
	private ArrayList<TargetModuleMapping> targetModules = new ArrayList<TargetModuleMapping>();
	public String getID() {
		return ID;
	}
	public void setID(String id) {
		ID = id;
	}
	public String getLocale() {
		return locale;
	}
	public void setLocale(String locale) {
		this.locale = locale;
	}
	public String getModule() {
		return module;
	}
	public void setModule(String module) {
		this.module = module;
	}
	public ArrayList<TargetModuleMapping> getTargetModules() {
		return targetModules;
	}
	public void setTargetModules(ArrayList<TargetModuleMapping> targetModules) {
		this.targetModules = targetModules;
	}
	public int targetModuleSize() {
		return targetModules.size();
	}
	public boolean isTargetModuleEmpty() {
		return targetModules.size() == 0;
	}
	public TargetModuleMapping getTargetModuleMapping(int i) {
		if (targetModules == null || i >= targetModuleSize())
			return new TargetModuleMapping();
		return targetModules.get(i);
	}
	public void addTargetModules(TargetModuleMapping m) {
		if (targetModules == null) 
			targetModules = new ArrayList<TargetModuleMapping>();
		targetModules.add(m);
	}
	public String getFullLocale() {
		return fullLocale;
	}
	public void setFullLocale(String fullLocale) {
		this.fullLocale = fullLocale;
	}
	
}
