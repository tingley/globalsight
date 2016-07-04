package com.globalsight.selenium.pages;

/*
 * This file contents the web elements definition about the Permission Groups affairs.
 */

public class PermissionGroups {
	//Permission Groups
	public static final String New_BUTTON="//input[@value='New...']";
	public static final String Edit_BUTTON="link=Administrator";
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
	
	public static final String LOCALIZATION_PARTICIPANT_LINK = "link=LocalizationParticipant";
	public static final String ADMINISTRATOR_LINK = "link=Administrator";
	
	public static final String MY_ACTIVITIES_EXPAND = "//div[@id='My Activities293']/a/img";
	public static final String MY_ACTIVITIES_VIEW_EXPAND = "//div[@id='Activities (View)295']/a/img";
	
//	public static final String DATA_SOURCE_EXPAND = "//div[@id='Data Sources162']/a/img";
	public static final String DATA_SOURCE_EXPAND = "//div[@id='Data Sources138']/a/img";
//	public static final String DATA_SOURCE_FILTER_CONFIGURATION_EXPAND =  "//div[@id='Filter Configuration(View)168']/a/img";
	public static final String DATA_SOURCE_FILTER_CONFIGURATION_EXPAND =  "//div[@id='Filter Configuration(View)144']/a/img";

	
	public static final String EXPORT_DOWNLOAD_CHECKBOX = "id=perm.activities.export.download";
	public static final String EXPORT_FILTERS_CHECKBOX = "id=perm.filter.configuration.export.filters";
	public static final String IMPORT_FILTERS_CHECKBOX = "id=perm.filter.configuration.import.filters";
}
