package com.globalsight.selenium.pages;

/*
 * This file contents the web elements definition about the Projects affairs.
 */

public class Projects
{
    // Projects
    public static final String NEW_BUTTON = "//input[@value='New...']";
    public static final String EDIT_BUTTON = "//input[@value='Edit...']";
    public static final String Remove_BUTTON = "//input[@value='Remove']";
    public static final String PROJECT_TABLE = "//div[@id='contentLayer']/form/table/tbody/tr[2]/td/table/tbody";

    // Create a New Project-Basic Information

    public static final String Name_TEXT_FIELD = "nameField";
    public static final String PROJECT_MANAGER_SELECT = "pmField";
    public static final String TermBase_SELECT = "tbField";
    public static final String AttributeGroup_SELECT = "attributeSet";
    public static final String Description_TEXT_FIELD = "descField";
    public static final String QuoteEmailTo_SELECT = "";
    public static final String PMCost_TEXT_FIELD = "pmcost";
    public static final String POrequired_CHECKBOX = "poRequired";

    public static final String Next_BUTTON = "Next";
    public static final String Cancel_BUTTON = "Cancel";

    // Create a New Project-User Information

    public static final String Avavilable_FORM = "from";
    public static final String AddTO_BUTTON = "addButton";
    public static final String SAVE_BUTTON = "Save";
    public static final String Previous_BUTTON = "Previous";
    public static final String Cancel_User_BUTTON = "Cancel";
    public static final String USER_BUTTON = "Users...";
    public static final String USER_DONE_BUTTON = "Done";
    public static final String Error_MSG_DIV = "errorDiv";

}
