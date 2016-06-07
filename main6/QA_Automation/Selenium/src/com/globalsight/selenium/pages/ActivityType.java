package com.globalsight.selenium.pages;

/*
 * This file contents the web elements definition about the Activity Types affairs.
 */
public class ActivityType
{

    // Activity Types
    public static final String New_BUTTON = "//input[@value='New...']";
    public static final String EDIT_BUTTON = "//input[@value='Edit...']";
    public static final String REMOVE_BUTTON = "//input[@value='Remove']";
    public static final String Next_LINK = "link=Next";
    public static final String ACTIVITY_TABLE = "//div[@id='contentLayer']/form/table/tbody/tr[2]/td/table/tbody";

    // New Activity Type
    public static final String Name_TEXT_FIELD = "nameField";
    public static final String DESCRIPTION_TEXT = "descField";

    public static final String Translate_RADIO = "radios";
    public static final String ReviewEditable_RADIO = "//input[@id='radios' and @name='type' and @value='reviewEditable']";
    public static final String ReviewOnly_RADIO = "//input[@id='radios' and @name='type' and @value='reviewNotEditable']";
    public static final String AutomaticActions_RADIO = "//input[@id='radios' and @name='type' and @value='autoaction']";
    public static final String GSEditionActions_RADIO = "//input[@id='radios' and @name='type' and @value='gsedition']";

    public static final String SAVE_BUTTON = "Save";
    public static final String Cancel_BUTTON = "Cancel";
    
    public static final String activityNameFilter = "activityNameFilter";
}