package com.globalsight.cxe.entity.gitconnector;

import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.persistence.PersistentObject;

public class GitConnectorFileMapping extends PersistentObject
{
	private static final long serialVersionUID = 9201252332489195301L;
	
	private String sourceLocale = "";
	private String sourceMappingPath = "";
	private String targetLocale = "";
	private String targetMappingPath = "";
	private long companyId = -1;
	private long gitConnectorId = -1;
	private long parentId = -1;
	
	public void setSourceLocale(String sourceLocale) {
		this.sourceLocale = sourceLocale;
	}
	public String getSourceLocale() {
		return sourceLocale;
	}
	public void setTargetLocale(String targetLocale) {
		this.targetLocale = targetLocale;
	}
	public String getTargetLocale() {
		return targetLocale;
	}
	public void setCompanyId(long companyId) {
		this.companyId = companyId;
	}
	public long getCompanyId() {
		return companyId;
	}
	public void setGitConnectorId(long gitConnectorId) {
		this.gitConnectorId = gitConnectorId;
	}
	public long getGitConnectorId() {
		return gitConnectorId;
	}
	public void setParentId(long parentId) {
		this.parentId = parentId;
	}
	public long getParentId() {
		return parentId;
	}
	public void setSourceMappingPath(String sourceMappingPath) {
		this.sourceMappingPath = sourceMappingPath;
	}
	public String getSourceMappingPath() {
		return sourceMappingPath;
	}
	public void setTargetMappingPath(String targetMappingPath) {
		this.targetMappingPath = targetMappingPath;
	}
	public String getTargetMappingPath() {
		return targetMappingPath;
	}
	// Utility
	public String getCompanyName()
	{
		return CompanyWrapper.getCompanyNameById(getCompanyId());
	}
}
