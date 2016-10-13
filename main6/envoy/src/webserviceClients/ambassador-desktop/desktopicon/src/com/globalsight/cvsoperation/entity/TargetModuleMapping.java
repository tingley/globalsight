package com.globalsight.cvsoperation.entity;

import java.util.ArrayList;

public class TargetModuleMapping {
	private String ID = "";
	private String locale = "";
	private String fullLocale = "";
	private String module = "";
	private ArrayList fileRenames = new ArrayList();
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
	public ArrayList getFileRenames() {
		return fileRenames;
	}
	public void setFileRenames(ArrayList fileRenames) {
		this.fileRenames = fileRenames;
	}
	public int size() {
		return fileRenames.size();
	}
	public boolean isEmpty() {
		return fileRenames.size() == 0;
	}
	public FileRename getFileRename(int i) {
		if (fileRenames == null || i >= fileRenames.size())
			return new FileRename();
		return (FileRename)fileRenames.get(i);
	}
	public void addFileRename(FileRename fr) {
		if (fr == null)
			return;
		if (fileRenames == null)
			fileRenames = new ArrayList();
		fileRenames.add(fr);
	}
	public String getFullLocale() {
		return fullLocale;
	}
	public void setFullLocale(String fullLocale) {
		this.fullLocale = fullLocale;
	}
	public boolean equals(TargetModuleMapping t) {
		if (t == null)
			return false;
		return locale.equals(t.getLocale()) && module.equals(t.getModule()); 
	}
	
}
