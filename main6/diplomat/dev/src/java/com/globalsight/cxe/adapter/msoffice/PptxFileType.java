/**
 *  Copyright 2009 Welocalize, Inc. 
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */
package com.globalsight.cxe.adapter.msoffice;

import java.io.File;
import java.io.FileFilter;
import java.util.Comparator;

public class PptxFileType 
{
	private String prefix;
	private String dir;
	private String mergeFile;
	
	public String getPrefix() 
	{
		return prefix;
	}
	
	public void setPrefix(String prefix) 
	{
		this.prefix = prefix;
	}
	
	public String getDir() 
	{
		return dir;
	}
	
	public void setDir(String dir) 
	{
		this.dir = dir;
	}
	
	public String getMergeFile() 
	{
		return mergeFile;
	}
	
	public void setMergeFile(String mergeFile) 
	{
		this.mergeFile = mergeFile;
	}
	
	public FileFilter getFileFilter()
	{
		return new FileFilter() 
		{
			@Override
			public boolean accept(File pathname) 
			{
				return pathname.isFile() && pathname.getName().startsWith(prefix);
			}
		};
	}
	
	public Comparator<File> getComparator()
	{
		return new Comparator<File>() 
    	{
			@Override
			public int compare(File o1, File o2) 
			{
				String name1 = o1.getName();
				String name2 = o2.getName();
				
				int length = getPrefix().length();
				String n1 = name1.substring(length, name1.lastIndexOf("."));
				String n2 = name2.substring(length, name2.lastIndexOf("."));
				
				int m1 = Integer.parseInt(n1);
				int m2 = Integer.parseInt(n2);
				
				return m1 - m2;
			}
		};
	}
}
