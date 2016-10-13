package com.globalsight.test;

import java.util.ArrayList;
import java.util.List;

import com.globalsight.entity.FileProfile;
import com.globalsight.util.SortHelper;

import junit.framework.TestCase;

public class SortHelperTestCase extends TestCase
{
	List fileProfiles;
	List stringList;
	String special_str = " a1b";

	public static void main(String[] args)
	{
		junit.swingui.TestRunner.run(SortHelperTestCase.class);
	}

	protected void setUp() throws Exception
	{
		super.setUp();
		//for testSortStringList
		stringList = new ArrayList();
		
		stringList.add("1b");
		stringList.add("Ab");
		stringList.add("AA");
		stringList.add("AAAABDDF");
		stringList.add("absdc");
		stringList.add("xadfe");
		stringList.add(special_str);
		stringList.add(" ab");
		//for testSortFileProfiles
		fileProfiles = new ArrayList();

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
		FileProfile fp_6 = new FileProfile();
		fp_6.setName(special_str);

		fileProfiles.add(fp_1);
		fileProfiles.add(fp_2);
		fileProfiles.add(fp_3);
		fileProfiles.add(fp_4);
		fileProfiles.add(fp_5);
		fileProfiles.add(fp_6);
	}

	/*
	 * Test method for 'com.globalsight.util.SortHelper.sortFileProfiles(List)'
	 */
	public void testSortFileProfiles()
	{
		SortHelper.sortFileProfiles(fileProfiles);
		FileProfile fp = (FileProfile)fileProfiles.get(0);
		assertEquals(special_str, fp.getName());
	}

	/*
	 * Test method for 'com.globalsight.util.SortHelper.sortStringList(List)'
	 */
	public void testSortStringList()
	{
		SortHelper.sortStringList(stringList);
		assertEquals(special_str, stringList.get(0));
	}

}
