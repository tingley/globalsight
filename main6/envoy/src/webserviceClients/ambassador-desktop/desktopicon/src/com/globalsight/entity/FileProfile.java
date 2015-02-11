package com.globalsight.entity;

public class FileProfile implements Cloneable
{

	private String id;

	private String name;
	
	private String l10nprofile;

	private String description;

	private String[] fileExtensions;

	private String sourceLocale;

	private String[] targetLocales;

	private String[] usedTargetLocales;

	public static String unkown = "unknown";
	
	/**
     * Default constructor
     */
	public FileProfile()
	{
	}

	public FileProfile(String p_id, String p_name)
	{
		id = p_id;
		name = p_name;
		fileExtensions = new String[0];
		sourceLocale = "";
		targetLocales = new String[0];
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String[] getFileExtension()
	{
		return fileExtensions;
	}

	public void setFileExtension(String[] fileExtension)
	{
		this.fileExtensions = fileExtension;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getSourceLocale()
	{
		return sourceLocale;
	}

	public void setSourceLocale(String sourceLocale)
	{
		this.sourceLocale = sourceLocale;
	}

	public String[] getTargetLocales()
	{
		return targetLocales;
	}

	/**
     * init both targetLocales and usedTargetLocales
     * 
     * @param targetLocales
     */
	public void setTargetLocales(String[] targetLocales)
	{
		this.usedTargetLocales = targetLocales;
		this.targetLocales = targetLocales;
	}

	public String toString()
	{
		return getName();
	}

	public String[] getUsedTargetLocales()
	{
		return usedTargetLocales;
	}

	public void setUsedTargetLocales(String[] usedTargetLocales)
	{
		this.usedTargetLocales = usedTargetLocales;
	}

	public boolean equals(Object o)
	{
		if (o == this) return true;

		if (o instanceof FileProfile)
		{
			FileProfile fp = (FileProfile) o;
			return this.getId().equals(fp.getId());
		}

		return false;
	}

	public Object clone()
	{
		FileProfile fp = new FileProfile();
		fp.setDescription(this.getDescription());
		fp.setFileExtension(this.getFileExtension());
		fp.setId(this.getId());
		fp.setName(this.getName());
		fp.setSourceLocale(this.getSourceLocale());
		fp.setTargetLocales(fp.getTargetLocales());
		fp.setUsedTargetLocales(this.getUsedTargetLocales());
		return fp;
	}

	public String getL10nprofile()
	{
		return l10nprofile;
	}

	public void setL10nprofile(String l10nprofile)
	{
		this.l10nprofile = l10nprofile;
	}

	
}
