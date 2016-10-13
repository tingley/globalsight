package com.globalsight.util;

import org.apache.log4j.*;

import com.globalsight.entity.FileProfile;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * A sort helper, for sort FileProfiles
 * 
 * @author quincy.zou
 * 
 */
public class SortHelper
{
	private static Logger log = Logger.getLogger(SortHelper.class);

	public static void main(String[] args)
	{
		List fileProfiles = new ArrayList();
		
		FileProfile fp_1 = new FileProfile();
		fp_1.setName("bADFEAFDF");
		FileProfile fp_2 = new FileProfile();
		fp_2.setName("AAC");
		FileProfile fp_3 = new FileProfile();
		fp_3.setName("AAB");
		FileProfile fp_4 = new FileProfile();
		fp_4.setName("abc");
		FileProfile fp_5 = new FileProfile();
		fp_5.setName("ABC");
		
		fileProfiles.add(fp_1);
		fileProfiles.add(fp_2);
		fileProfiles.add(fp_3);
		fileProfiles.add(fp_4);
		fileProfiles.add(fp_5);
		sortFileProfiles(fileProfiles);
		
		printList(fileProfiles);
		
	}

    /**
     * sort the fileProfiles
     * @param p_fileProfiles
     */
	public static void sortFileProfiles(List p_fileProfiles)
	{
		//get fileProfiles name
		List names = new ArrayList();
		for(int i = 0; i < p_fileProfiles.size(); i++)
		{
			names.add(((FileProfile)p_fileProfiles.get(i)).getName());
		}
		//sort names
		sortStringList(names);
		//new fileProfiles follow the names after sorted
		List new_fileProfiles = new ArrayList(p_fileProfiles);
		for(int i = 0; i < p_fileProfiles.size(); i++)
		{
			FileProfile fileProfile = (FileProfile)p_fileProfiles.get(i);
			new_fileProfiles.set( names.indexOf(fileProfile.getName()), fileProfile);
		}
		
		p_fileProfiles.removeAll(p_fileProfiles);
		p_fileProfiles.addAll(new_fileProfiles);
	}
	
	public static void sortStringList(List p_list)
	{
		try
		{
			for(int i = 0; i < p_list.size(); i++)
			{
				for(int j = i+1; j < p_list.size(); j++)
				{
					if(((String)p_list.get(i)).compareTo(((String)p_list.get(j)))>0)
					{
						swapInList(p_list, i, j);
					}
				}
			}
		}
		catch (ClassCastException e)
		{
			log.info("sortStringList() can only deal String List ");
			throw e;
		}
		
	}

	private static void swapInList(List p_list, int p_index1, int p_index2)
	{
		Object temp = p_list.get(p_index1);
		p_list.remove(p_index1);
		p_list.add(p_index1, p_list.get((p_index2 < p_index1) ? p_index2 : p_index2 - 1));
		p_list.remove(p_index2);
		p_list.add(p_index2, temp);
	}

	private static void swapLongArray()
	{
		
	}
	
	private static void printList(List p_list)
	{
		for (int i = 0; i < p_list.size(); i++)
		{
			log.info(p_list.get(i));
		}
	}
}
