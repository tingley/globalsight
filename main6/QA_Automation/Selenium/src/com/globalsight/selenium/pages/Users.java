package com.globalsight.selenium.pages;

/*
 * This file contents the web elements definition about the Users affairs.
 */

public class Users implements BasePage
{
    public static final String USER_LIST_TABLE = "//div[@id='contentLayer']/form/table/tbody/tr[2]/td/table/tbody";

    // Users
    public static final String USER_SEARCH_NAME_TYPE_SELECT = "nameTypeOptions";
    public static final String USER_SEARCH_NAME_TYPE_USER_NAME = "User Name";
    public static final String USER_SEARCH_NAME_TYPE_FIRST_NAME = "First Name";
    public static final String USER_SEARCH_NAME_TYPE_LAST_NAME = "Last Name";
    
    public static final String USER_SEARCH_NAME_OPTION_SELECT = "nameOptions";
    public static final String USER_SEARCH_NAME_OPTION_BEGIN_WITH = "begins with";
    public static final String USER_SEARCH_NAME_OPTION_END_WITH = "ends with";
    public static final String USER_SEARCH_NAME_OPTION_CONTAIN = "contains";
    
    public static final String USER_SEARCH_NAME_TEXT = "uNameFilter";
    public static final String USER_ROLES_TABLE = "//div[@id='contentLayer']/table/tbody/tr[2]/td/table/tbody";

    // New User-Basic Information
    public static final String TITLE_TEXT = "title";
    public static final String NAME_TEXT = "userName";
    public static final String FIRST_NAME_TEXT = "firstName";
    public static final String LAST_NAME_TEXT = "lastName";
    public static final String PASSWORD_TEXT = "password";
    public static final String CONFIRMED_PASSWORD_TEXT = "passwordConfirm";
    public static final String COMPANY_SELECT = "//select[@name='companies']";

    // New User-Contact Infomation
    public static final String ADDRESS_TEXT = "address";
    public static final String HOMEPHONE_TEXT = "homePhone";
    public static final String WORKPHONE_TEXT = "workPhone";
    public static final String CELLPHONE_TEXT = "cellPhone";
    public static final String FAX_TEXT = "fax";
    public static final String EMAIL_TEXT = "email";

    // New Roles
    public static final String SOURCE_LOCALE_SELECT = "//div[@id='contentLayer']/table/tbody/tr/td/form/table/tbody/tr/td[2]/select";
    public static final String TARGET_LOCALE_SELECT = "//div[@id='contentLayer']/table/tbody/tr/td/form/table/tbody/tr[2]/td[2]/select";

    public static final String SrcLocale_SELECT = "selectSourceLocale";
    public static final String TarLocale_SELECT = "selectTargetLocale";

    public static final String Dtp1Type_CHECKBOX = "Dtp1_1118Cost";
    public static final String Dtp1Type_SELECT = "Dtp1_1118_expense";
    public static final String LAN_ADD = "Add";
    
    public static final String DEFAULT_ROLES_VALUE_BUTTON = "//input[@name='Default Roles']";
    public static final String ROLES_BUTTON = "Roles";

    // New User-Projects
    public static final String Project_Select_Table = "//div[@id='contentLayer']/form/table/tbody/tr[4]/td";
    public static final String Project_Select_Table_Name = "from";
    // New User-Field Level Access

    public static final String Next_BUTTON_LevelAccess = "//input[@value='Next']";

    // New User-Permisson Groups
    public static final String Available_SELECTION = "from";
    public static final String Available_SELECTION_Permission = "to";
    public static final String Add_BUTTON = "addButton";
    public static final String Permission_Remove_BUTTON = "removedButton";

    // New Role
    public static final String ROLES_TABLE = "//div[@id='contentLayer']/form/table/tbody/tr[2]/td/table/tbody/tr[3]/td/table/tbody/tr[3]/td/table/tbody";
    public static final String NewRole_TABLE_DTP1_CheckBox = ROLES_TABLE
            + "/tr[5]/td[2]/select";
    public static final String NewRole_TABLE_NewUser = "//div[@id='contentLayer']/table[3]/tbody";

    // Activities Types
    public static final String Activities_Types1 = "activity_10";
    public static final String Activities_Types2 = "activity_11";

    // Edit user
    public static final String Edit_Roles_BUTTON = "//input[@value='Roles...']";
    public static final String Edit_ContactInfo_BUTTON = "//input[@value='Contact Info...']";
    public static final String Edit_Calendar_BUTTON = "//input[@value='Calendar...']";
    public static final String Edit_Project_BUTTON = "//input[@value='Projects...']";
    public static final String Edit_Security_BUTTON = "//input[@value='Security...']";
    public static final String Edit_Permissions_BUTTON = "//input[@value='Permissions...']";

    // Edit Project
    public static final String AllProject_CHECKBOX = "idAllProjects";

    // Edit Role
    public static final String Role_TABLE_Activity = "//div[@id='contentLayer']/form/table/tbody/tr[2]/td/table/tbody/tr[3]/td/table/tbody/tr[3]/td/table/tbody";
    public static final String Edit_Role_LocalPairs_TABLE = "//div[@id='contentLayer']/table/tbody/tr[2]/td/table/tbody";
}
