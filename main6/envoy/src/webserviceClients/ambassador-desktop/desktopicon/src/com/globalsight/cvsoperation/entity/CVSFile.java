package com.globalsight.cvsoperation.entity;

import java.io.File;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.globalsight.ui.MainFrame;
import com.globalsight.util.Constants;

public class CVSFile
{
	static Logger log = Logger.getLogger(CVSFile.class.getName());
	
	private String nodeType = null;
	
	private String root = null;
	
	private Repository repository = null;
	
	private Module module = null;
	
	private String modulePath = null;
	
	private File file = null;
	
	public CVSFile()
	{
		
	}
	
	public CVSFile(String nodeType)
	{
		this.nodeType = nodeType;
	}
	
	/**
	 * Set node type
	 * @param nodeType
	 * 	Cosntants.NODE_TYPE_ROOT = "ROOT"
	 *	Cosntants.NODE_TYPE_REPOSITORY = "REPOSITORY"
	 *  Cosntants.NODE_TYPE_MODULE = "MODULE"
	 *  Cosntants.NODE_TYPE_PATH = "MODULE_PATH"
	 *  Cosntants.NODE_TYPE_FILE = "FILE"
	 */
	public void setNodeType(String nodeType)
	{
		this.nodeType = nodeType;
	}
	
	public String getNodeType()
	{
		return this.nodeType;
	}
	
	public void setRoot(String root)
	{
		this.root = root;
	}
	
	public String getRoot()
	{
		return this.root;
	}
	
	public void setRepository(Repository rep)
	{
		this.repository = rep;
	}
	
	public Repository getRepository()
	{
		return this.repository;
	}
	
	public void setModule(Module module)
	{
		this.module = module;
	}
	
	public Module getModule()
	{
		return this.module;
	}
	
	public void setModulePath(String modulePath)
	{
		this.modulePath = modulePath;
	}
	
	public String getModulePath()
	{
		return this.modulePath;
	}
	
	public void setFile(File file)
	{
		this.file = file;
	}
	
	public File getFile()
	{
		return this.file;
	}
	
	public String toString()
	{
		String returnString = null;
		
		try 
		{
			if ( this.nodeType != null )
			{
				if ( nodeType.equals(Constants.NODE_TYPE_ROOT) )
				{
					returnString = this.root;
				}
				else if ( nodeType.equals(Constants.NODE_TYPE_REPOSITORY) )
				{
					repository.setIsAllFlag(false);
					returnString = this.repository.toString();
				}
				else if ( nodeType.equals(Constants.NODE_TYPE_MODULE) )
				{
					returnString = this.module.toString();
				}
				else if ( nodeType.equals(Constants.NODE_TYPE_PATH) )
				{
					returnString = this.modulePath;
				}
				else if ( nodeType.equals(Constants.NODE_TYPE_FILE) )
				{
					returnString = this.file.getName();
				}
			}			
		}
		catch (Exception ex)
		{
			returnString = "null";
			log.error(ex.getMessage(), ex);
		}
		
		return returnString;
	}

}
