package com.globalsight.selenium.pages;

/*
 * This file contents the web elements definition about the Users affairs.
 */

public class Users
{
    // Users
	public static final String User_TABLE = "//div[@id='contentLayer']/form/table[2]/tbody/tr[2]/td/table/tbody";
    public static final String NameTypeOption_SELECT = "nameTypeOptions";
    public static final String NameOption_SELECT = "nameOptions";
    public static final String UserNameSearch_TEXT_FIELD = "nameField";
    public static final String User_Edit_BUTTON ="editBtn";
    public static final String Roles_BUTTON = "Roles";
    public static final String EditUser_Save_BUTTON = "Save";
    public static final String Search_BUTTON = "//input[@value='Search...']";
    public static final String New_BUTTON = "//input[@value='New...']";
    public static final String Roles_Done_BUTTON = "//input[@value='Done']";
    public static final String Next_LINK = "link=Next";
    public static final String Edit_BUTTON= "editBtn";
    public static final String Default_ROLES="//input[@name='Default Roles']";
    public static final String New_BUTTON_Default_Roles="newBtn";
    public static final String Remove_BUTTON = "//input[@value='Remove']";
    public static final String Done_BUTTON_Default_Roles="doneBtn";
    public static final String Save_BUTTON="Save";
    public static final String Users_TABLE="//div[@id='contentLayer']/form/table/tbody/tr[2]/td/table/tbody";
    public static final String Roles="Roles";
    public static final String Roles_TABLE="//div[@id='contentLayer']/table/tbody/tr[2]/td/table/tbody";
    public static final String Done_BUTTON_Default_Roles_new="//input[@value='Done']";
    // New User-Basic Information
    public static final String UserName_TEXT_FIELD = "userName";
    public static final String FirstName_TEXT_FIELD = "firstName";
    public static final String LastName_TEXT_FIELD = "lastName";
    public static final String Password_TEXT_FIELD = "password";
    public static final String RepeatPassword_TEXT_FIELD = "passwordConfirm";
    public static final String Title_TEXT_FIELD = "title";
    public static final String CompanyName_SELECT = "//select[@name='companies']";

    public static final String Next_BUTTON = "Next";
    public static final String Cancel_BUTTON = "Cancel";
    //New User-Contact Infomation
    public static final String Address_TEXT_FIELD="address";
    public static final String HomePhone_TEXT_FIELD="homePhone";
    public static final String WorkPhone_TEXT_FIELD="workPhone";
    public static final String CellPhone_TEXT_FIELD="cellPhone";
    public static final String Fax_TEXT_FIELD="fax";
    public static final String EamilAddress_TEXT_FIELD="email";
    public static final String Next_BUTTON_Contact="Next";
    public static final String Edit_ContactInfo_Done_BUTTON = "//input[@value='Done']";
 
    
    //New Roles
    public static final String SourceLocale_SELECT="//div[@id='contentLayer']/table/tbody/tr/td/form/table/tbody/tr/td[2]/select";
    public static final String TargetLocale_SELECT="//div[@id='contentLayer']/table/tbody/tr/td/form/table/tbody/tr[2]/td[2]/select";
    
    public static final String SrcLocale_SELECT="selectSourceLocale";
    public static final String TarLocale_SELECT="selectTargetLocale";
    
    public static final String Next_BUTTON_Roles="Next";
    public static final String Dtp1Type_CHECKBOX="Dtp1_1118Cost";
    public static final String Dtp1Type_SELECT="Dtp1_1118_expense";
    public static final String LAN_ADD="Add";
    public static final String SourceLocale_SELECT_Default="sourceLocale";
    public static final String TargetLocale_SELECT_Default="targetLocale";
    
    
    //New User-Projects
    
    public static final String Next_BUTTON_Projects="Next";
    public static final String Project_Select_Table="//div[@id='contentLayer']/form/table/tbody/tr[4]/td";
    public static final String Project_Select_Table_Name="from";
    //New User-Field Level Access
    
    public static final String Next_BUTTON_LevelAccess="//input[@value='Next']";
    
    //New User-Permisson Groups
    
    public static final String Available_SELECTION="from";
    public static final String Available_SELECTION_Permission="to";
    public static final String Add_BUTTON="addButton";
    public static final String Permission_Remove_BUTTON="removedButton";
    public static final String Save_BUTTON_Permission="//input[@value='Save']";
    public static final String Edit_Permission_Cancel_BUTTON = "//input[@value='Cancel']";
    public static final String Edit_Permission_Done_BUTTON = "//input[@value='Done']";
    
    
    //New Role
    public static final String NewRole_TABLE = "//div[@id='contentLayer']/form/table/tbody/tr[2]/td/table/tbody/tr[3]/td/table/tbody/tr[3]/td/table/tbody";
    public static final String NewRole_TABLE_DTP1_CheckBox = NewRole_TABLE + "/tr[5]/td[2]/select";
    public static final String NewRole_TABLE_NewUser = "//div[@id='contentLayer']/table[3]/tbody";
    public static final String NewRolw_Done = "doneButton";
    public static final String NewRole_Done = "//input[@value='Done']";
    public static final String NewRole_Cancel = "//input[@value='Cancel']";

    //Activities Types

    public static final String Activities_Types1="activity_10";
    public static final String Activities_Types2="activity_11";
    
  //Edit user
    public static final String Edit_Cancel_BUTTON = "//input[@value='Cancel']";
    public static final String Edit_Roles_BUTTON = "//input[@value='Roles...']";
    public static final String Edit_ContactInfo_BUTTON = "//input[@value='Contact Info...']";
    public static final String Edit_Calendar_BUTTON = "//input[@value='Calendar...']";
    public static final String Edit_Project_BUTTON = "//input[@value='Projects...']";
    public static final String Edit_Security_BUTTON = "//input[@value='Security...']";
    public static final String Edit_Permissions_BUTTON = "//input[@value='Permissions...']";
    public static final String Edit_Save_BUTTON = "Save";
    
    
    
    //Edit Project
    public static final String AllProject_CHECKBOX = "idAllProjects";
    public static final String Edit_Project_Done_BUTTON = "//input[@value='Done']";

    //Edit Role
    public static final String Edit_Role_Cancel_BUTTON = "//input[@value='Cancel']";
    public static final String Edit_Role_Edit_BUTTON = "//input[@value='Edit...']";
    public static final String Edit_Role_New_BUTTON = "//input[@value='New...']";
    public static final String Edit_Role_Done_BUTTON = "//input[@value='Done']";
    public static final String Edit_Role_SourceLocal_SELECT = "selectSourceLocale";
    public static final String Edit_Role_TargetLocal_BUTTON = "selectTargetLocale";
    public static final String Role_TABLE_Activity = "//div[@id='contentLayer']/form/table/tbody/tr[2]/td/table/tbody/tr[3]/td/table/tbody/tr[3]/td/table/tbody";
    public static final String Edit_Role_LocalPairs_TABLE = "//div[@id='contentLayer']/table/tbody/tr[2]/td/table/tbody";
}
