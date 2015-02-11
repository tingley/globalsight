package com.globalsight.everest.cvsconfig.modulemapping;

import java.util.Set;

import com.globalsight.everest.persistence.PersistentObject;

public class ModuleMapping extends PersistentObject{
	private static final long serialVersionUID = -9210462704700975111L;
	
	private String sourceLocale = "";
	private String sourceLocaleLong = "";
	private String sourceModule = "";
	private String targetLocale = "";
	private String targetLocaleLong = "";
	private String targetModule = "";
	private long companyId = -1L;
	private long moduleId = -1L;
	private String subFolderMapped = "0";
	private Set fileRenames = null;
	
	public String getSourceLocale() {
		return sourceLocale;
	}
	public void setSourceLocale(String sourceLocale) {
		this.sourceLocale = sourceLocale;
	}
	public String getSourceLocaleLong() {
		return sourceLocaleLong;
	}
	public void setSourceLocaleLong(String sourceLocaleLong) {
		this.sourceLocaleLong = sourceLocaleLong;
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
	public String getTargetLocaleLong() {
		return targetLocaleLong;
	}
	public void setTargetLocaleLong(String targetLocaleLong) {
		this.targetLocaleLong = targetLocaleLong;
	}
	public String getTargetModule() {
		return targetModule;
	}
	public void setTargetModule(String targetModule) {
		this.targetModule = targetModule;
	}
	public long getCompanyId() {
		return companyId;
	}
	public void setCompanyId(long companyId) {
		this.companyId = companyId;
	}
	public Set getFileRenames() {
		return fileRenames;
	}
	public void setFileRenames(Set fileRenames) {
		this.fileRenames = fileRenames;
	}
	public long getModuleId() {
		return moduleId;
	}
	public void setModuleId(long moduleId) {
		this.moduleId = moduleId;
	}
	public String getSubFolderMapped() {
		return subFolderMapped;
	}
	public void setSubFolderMapped(String subFolderMapped) {
		this.subFolderMapped = subFolderMapped;
	}
	
}
