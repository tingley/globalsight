package com.globalsight.selenium.pages;

/*
 * This file contents the web elements definition about the File Extensions affairs.
 */

public class JobDetails {
	
	//Job:
	public static final String Details_LINK="link=Details";
	public static final String Comments_LINK="link=Comments";
	public static final String WorkOffline="link=Work Offline";
	public static final String Details_TABLE="//div[@id='contentLayer']/table[2]/tbody/tr/td/table/tbody/tr[4]/td/table/tbody/tr/td[1]/form/table/tbody";
	
    public static final String SOURCEFILESPAGE_TABLE = "//div[@id='contentLayer']/p[2]/table/tbody/tr/td/table[2]/tbody/tr/td[3]/table/tbody/tr/td/div/table/tbody";
    public static final String FIRSTSOURCEFILE_CHECKBOX = "pageIds";
    public static final String DOWNLOAD_BUTTON = "//input[@name='download Files']";
    public static final String UPLOAD_BUTTON = "//input[@name='upload Files']";
    public static final String UPLOADFILEPATH_INPUT = "fileUploadDialog";
    public static final String UPLOADDIALOG_BUTTON = "dijit_form_Button_2";
    public static final String DELETEFILES_BUTTON = "//input[@name='remove Files']";

	public static final String WORKFLOWS_TABLE="//form[@id='workflowForm']/table/tbody/tr[2]/td/table/tbody";
	public static final String Reassign_BUTTON="ReAssign";
	public static final String DISCARD_BUTTON="Discard";
	public static final String ViewError_BUTTON="ViewError";
	public static final String DETAILED_WORD_COUNTS_BUTTON="WordCount";
	public static final String RateVendor_BUTTON="Rate";
	public static final String Archive_BUTTON="Archive";
	public static final String Details_BUTTON="Details";
	public static final String EXPORT_BUTTON="Export";
	public static final String Add_BUTTON="Add";
	public static final String Edit_BUTTON="Edit";
	public static final String DISPATCH_BUTTON="Dispatch";
	public static final String Download="Download";
	public static final String Previous="Previous";
	
	//job tables
	public static final String DETAILED_STATISTICS_TABLE="//div[@id='contentLayer']/form/p[1]/table/tbody/tr[2]/td/table/tbody";
	public static final String SummaryStatistics_TABLE="//div[@id='contentLayer']/form/p[2]/table/tbody/tr[2]/td/table/tbody";
	public static final String BacktoJobDetails_BUTTON="//input[@value='Back to Job Details']";
	
	//Export the job.
	public static final String EXPORT_EXECUTE_BUTTON="Export";
}
