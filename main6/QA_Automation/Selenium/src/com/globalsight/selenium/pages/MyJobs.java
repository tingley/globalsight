package com.globalsight.selenium.pages;

/*
 * This file contents the web elements definition about the My Jobs affairs.
 */

public class MyJobs {
	
	public static final String MyJobs_TABLE="//div[@id='contentLayer']/table[2]/tbody/tr[2]/td/table/tbody/tr[1]/td/form/table/tbody[2]";
	public static final String MyJobs_Ready_TABLE="//div[@id='contentLayer']/table[2]/tbody/tr[2]/td/table/tbody/tr/td/form/table/tbody";
	public static final String MyJobs_InProgress_TABLE="//div[@id='contentLayer']/table[2]/tbody/tr[2]/td/table/tbody/tr/td/form/table/tbody";
	public static final String MyJobs_AllStatus_TABLE="//div[@id='contentLayer']/table[2]/tbody/tr[2]/td/form/table/tbody[2]";
			

	//My Jobs-Ready
	public static final String Status_SELECTION="sto";
	public static final String JobName_SELECTION="no";
	public static final String SEARCH_JOB_NAME_TEXT="nf";
	public static final String SEARCH_BUTTON="Search";
	public static final String SEARCH_AND_REPLACE_BUTTON="search";
	public static final String CHANGE_WF_MANAGER_BUTTON="ChangeWFMgr";
	public static final String DISCARD_BUTTON="Discard";
	public static final String DISPATCH_BUTTON="Dispatch";
	public static final String CHECK_ALL_LINK="link=Check All";
	public static final String CHECK_ALL_PAGES_LINK="link=Check All Pages";
	public static final String CLEAR_ALL_LINK="link=Clear All";
	public static final String AdvancedSearch_LINNK="link=Advanced Search";
	public static final String JobName_Slection_begins_With = "begins with"; 
	public static final String JobName_Slection_Ends_With = "ends with";
	public static final String JobName_Slection_Contains = "contains";
	
	
	//MyJobs-All Status
	
	public static final String ALL_BUTTONS_BUTTON="//input[@type='BUTTON']"; //used to check is any button displayed.
	
}
