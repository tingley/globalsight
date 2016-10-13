package com.globalsight.entity;

import java.io.File;

public class FileMapped
{
	private File m_file;

	private FileProfile m_fp;

	public FileMapped(File file, FileProfile fp)
	{
		m_file = file;
		m_fp = fp;
	}

	public File getFile()
	{
		return m_file;
	}

	public FileProfile getFileProfile()
	{
		return m_fp;
	}

	public String toString()
	{
		return m_fp.getName() + ":file:" + m_file.getAbsolutePath();
	}
}
