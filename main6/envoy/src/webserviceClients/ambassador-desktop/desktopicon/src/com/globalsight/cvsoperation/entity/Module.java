package com.globalsight.cvsoperation.entity;

import java.util.Vector;

public class Module 
{
	private int repositoryIndex = -1;
	
	private String moduleName = null;
	
	private Vector modulePath = null;
	
	private String branchTagRevision = null;
	
	private String project = null;
	
	public void setRepositoryIndex(int repIndex)
	{
		this.repositoryIndex = repIndex;
	}
	
	public int getRepositoryIndex()
	{
		return this.repositoryIndex;
	}
	
	public void setModuleName(String moduleName)
	{
		this.moduleName = moduleName;
	}
	
	public String getModuleName()
	{
		return this.moduleName;
	}
	
	public void setModulePath(Vector modulePath)
	{
		this.modulePath = modulePath;
	}
	
	public Vector getModulePath()
	{
		return this.modulePath;
	}
	
	public void setBranchTagRevision(String branchTagRevision)
	{
		this.branchTagRevision = branchTagRevision;
	}
	
	public String getBranchTagRevision()
	{
		return this.branchTagRevision;
	}
	
	public void setProject(String project)
	{
		this.project = project;
	}
	
	public String getProject()
	{
		return this.project;
	}
	
	public boolean equals(Object obj)
	{
		if (obj == this) {
			return true;
		}

		if (obj instanceof Module) {
			Module module = (Module) obj;
			return ( module.moduleName.equals(this.moduleName));
		}

		return false;
	}
	
	public String toString()
	{
		return this.moduleName;
	}
}
