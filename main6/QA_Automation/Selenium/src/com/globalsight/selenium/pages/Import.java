package com.globalsight.selenium.pages;

/*
 * This file contents the web elements definition about the Job Editors affairs.
 */

public class Import {
	
	//Select Files to Import
	public static final String AvailableFiles_TABLE="//div[@id='contentLayer']/form[2]/div[2]/table/tbody[2]";
	public static final String BackFile_IMG="//div[@id='contentLayer']/form[2]/div[2]/table/tbody[2]/tr[1]/td[4]/a/img";
	
	
	public static final String Add_BUTTON="//input[@value='>>']";
	public static final String Remove_BUTTON="//input[@value='<<']";
	public static final String Next_BUTTON="//input[@value='Next']";
	public static final String Cancel_BUTTON="//input[@value='Cancel']";
	
	
	//Map Selected files to file profiles
	public static final String SelectedFiles_TABLE="//div[@id='contentLayer']";
	
	public static final String Cancel_BUTTON_MAP="//input[@value='Cancel']";
	public static final String Previous_BUTTON_MAP="//input[@value='Previous']";
	public static final String Next_BUTTON_MAP="//input[@value='Next']";
	
	
	//Enter Job Name
    public static final String JobName_TEXT_FIELD="jobName";
    public static final String TargetLocale_Selection="targetLocaleIds";
    public static final String ContinueToCreate_CHECKBOX="checkboxGoTo";
    
    public static final String CreateJob_BUTTON_JOB="//input[@value='Create Job']";
    public static final String Previous_BUTTON_JOB="//input[@value='Previous']";
    public static final String Cancel_BUTTON_JOB="//input[@value='Cancel']";

}
