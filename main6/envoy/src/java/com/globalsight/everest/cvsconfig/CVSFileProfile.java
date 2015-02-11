package com.globalsight.everest.cvsconfig;

import com.globalsight.cxe.entity.fileprofile.FileProfileImpl;
import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.everest.projecthandler.ProjectImpl;

public class CVSFileProfile extends PersistentObject {
	private static final long serialVersionUID = -3476552246513532499L;

	private String fileExt = "";
	private String filePath = "";
	private String sourceLocale = "";
	private long companyId = 0L;
	private ProjectImpl project = new ProjectImpl();
	private FileProfileImpl fileProfile = new FileProfileImpl();
	private CVSModule module = new CVSModule();
	
	public String getFileExt() {
		return fileExt;
	}
	public void setFileExt(String fileExt) {
		this.fileExt = fileExt;
	}
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	public String getSourceLocale() {
		return sourceLocale;
	}
	public void setSourceLocale(String sourceLocale) {
		this.sourceLocale = sourceLocale;
	}
	public long getCompanyId() {
		return companyId;
	}
	public void setCompanyId(long companyId) {
		this.companyId = companyId;
	}
	public ProjectImpl getProject() {
		return project;
	}
	public void setProject(ProjectImpl project) {
		this.project = project;
	}
	public FileProfileImpl getFileProfile() {
		return fileProfile;
	}
	public void setFileProfile(FileProfileImpl fileProfile) {
		this.fileProfile = fileProfile;
	}
	public CVSModule getModule() {
		return module;
	}
	public void setModule(CVSModule module) {
		this.module = module;
	}
	
}
