package com.globalsight.selenium.pages;

/*
 * This file contents the web elements definition about the Permission Groups affairs.
 */

public class PermissionGroups {
	//Permission Groups
	public static final String New_BUTTON="//input[@value='New...']";
	public static final String Edit_BUTTON="editBtn";
	public static final String PermissionGroups_TABLE="//div[@id='contentLayer']/form/table/tbody/tr[2]/td/table/tbody";
	
	//Edit Permission Group-Basic Information
	
	public static final String Permissions_BUTTON="perms";
	public static final String Save_BUTTON="save";
	public static final String Cancel_BUTTON="Cancel";
	
	
	//Edit Permission Group(Administrator) -Permissions
    public static final String ALLPERMISSION = "cat.lb_permissions";
	public static final String DataSources_CHECKBOX="cat.lb_data_sources";
	public static final String Permissions_CHECKBOX="cat.lb_permissions";
	public static final String SetUp_CHECKBOX="cat.lb_setup";
	public static final String REPORT_CHECKBOX = "cat.lb_reports";
	public static final String Done_BUTTON="done";
	public static final String Cancel_BUTTON_Administrator="cancel";
	
}
