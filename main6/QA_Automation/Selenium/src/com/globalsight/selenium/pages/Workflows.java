package com.globalsight.selenium.pages;

/*
 * This file contents the web elements definition about the Rates affairs.
 */

public class Workflows {

	//Workflows
	public static final String SearchName_SELECT="nameOptions";
	public static final String SearchName_TEXT_FIELD="nameField";
	public static final String Search_BUTTON="//input[@value='Search...']";
	
	public static final String New_BUTTON="//input[@value='New...']";
	public static final String Import_BUTTON="impBtn";
	public static final String Export_BUTTON="expBtn";
	public static final String Duplicate_BUTTON="dupBtn";
	public static final String RemoveWF_BUTTON="removeBtn";
		
	public static final String Workflows_TABLE="//div[@id='contentLayer']/form/p/table/tbody/tr[2]/td/table/tbody";
	
	
	//Import Workflow
	public static final String Name_TEXT_FIELD_IMPORT="nameTF";
	public static final String FileToImport_TEXT_FIELD="idFilename";
	public static final String Project_SELECT="project";
	public static final String SourceLocale_SELECTION="sourceLocale";
	public static final String TargetLocale_SELECTION="targetLocale";
	public static final String Add_BUTTON="addButton";
	public static final String Remove_BUTTON="removedButton";
	
	public static final String Cancel_BUTTON="Cancel";
	public static final String Save_BUTTON="Save";
	
	//Duplicate Workflow
	public static final String Name_TEXT_FIELD_DUPLICATE="nameTF";
	public static final String SourceLocle_SELECTION_DUPLICATE="sourceLocale";
	public static final String TargetLocale_SELECTION_DUPLICATE="targetLocale";
	public static final String Add_BUTTON_DUPLICATE="addButton";
	public static final String Remove_BUTTON_DUPLICATE="removedButton";
	public static final String Cancel_BUTTON_DUPLICATE="Cancel";
	public static final String Save_BUTTON_DUPLICATE="Save";
}
