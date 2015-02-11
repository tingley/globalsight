package com.globalsight.selenium.functions;

import org.testng.Assert;
import org.testng.Reporter;

import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.Users;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.thoughtworks.selenium.Selenium;

public class UsersFuncs extends BasicFuncs
{
    private static final String MAIN_TABLE = "//div[@id='contentLayer']/form/table/tbody/tr[2]/td/table/tbody";
    BasicFuncs basicfuncs = new BasicFuncs();
    String cm = ConfigUtil.getConfigData("company");

    /**
     * This Method is designed to check the check box with value specified.
     * 
     * Example Edit Role page of User feature.
     * 
     * You must provided the table string and the name string
     * 
     * Author:Erica
     */
    public String companyID(Selenium selenium, String iTable, int iTd)
            throws Exception
    {

        String ActivityName = null;

        iTable = iTable.trim();
        String comID = "";

        try
        {
            int i = 3;
            while (selenium.isElementPresent(iTable + "//tr[" + i + "]"))
            {
                if (selenium.isElementPresent(iTable + "//tr[" + i + "]//td["
                        + iTd + "]"))
                {
                    ActivityName = selenium.getAttribute(iTable + "//tr[" + i
                            + "]//td[" + iTd + "]/input/@name");
                    int iStart = ActivityName.lastIndexOf("_");
                    int iEnd = ActivityName.lastIndexOf("Cost");
                    comID = ActivityName.substring(iStart + 1, iEnd);
                    return comID;

                }
            }
        }
        catch (Exception e)
        {
            Reporter.log(e.toString());
            Assert.assertTrue(false, selenium.getAlert());
            return ActivityName;
        }

        return comID;
    }

    // Add new role
    public void addRoles(Selenium selenium, String UserProfiles, String NewUser)
            throws Exception
    {

        String comID = "";

        String[] iUserProfiles = UserProfiles.split("/");

        String iSourceLocal = iUserProfiles[0].trim();
        String iTargetLocal = iUserProfiles[1].trim();
        String iActivity = iUserProfiles[2].trim();

        selenium.select(Users.SrcLocale_SELECT, "label=" + iSourceLocal);
        if (NewUser.equals("true"))
        {
            selenium.select(Users.TarLocale_SELECT, "label=" + iTargetLocal);
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
            comID = companyID(selenium, Users.NewRole_TABLE_NewUser, 1);

        }
        else
        {
            selenium.addSelection(Users.TarLocale_SELECT, "label="
                    + iTargetLocal);
            comID = companyID(selenium, Users.ROLES_TABLE, 1);
        }

        String[] iActivityTypes = iActivity.split("-");
        for (String iActivityType : iActivityTypes)
        {
            String iFieldName0 = iActivityType.trim();
            selenium.click(iFieldName0 + "_" + comID + "Cost");
        }

    }

    /*
     * Create new users with the profiles you provided.
     */
    public String newUsers(Selenium selenium, String UserProfiles)
            throws Exception
    {
        String[] iUserProfiles = UserProfiles.split(",");
        String iuserName = null;

        try
        {
            selenium.click(Users.NEW_VALUE_BUTTON);
        }
        catch (Exception e)
        {
            if (selenium.isAlertPresent())
                selenium.getAlert();
        }
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        String sourcelocal = new String();
        String targetlocal = new String();
        String translation = new String();
        String icomName = "";

        for (String iUserProfile : iUserProfiles)
        {
            String[] ivalue = iUserProfile.split("=");
            String iFieldName = ivalue[0].trim();
            String iFieldValue = ivalue[1].trim();

            try
            {
                if (iFieldName.equals("addcomnametouser"))
                {
                    if (iFieldValue.equals("true"))
                    {
                        icomName = ConfigUtil.getConfigData("company");
                    }
                }
                if (iFieldName.equals("username"))
                {
                    selenium.type(Users.NAME_TEXT, icomName
                            + iFieldValue);
                    iuserName = icomName + iFieldValue;
                }
                else if (iFieldName.equals("firstname"))
                {
                    selenium.type(Users.FIRST_NAME_TEXT, iFieldValue);
                }
                else if (iFieldName.equals("lastname"))
                {
                    selenium.type(Users.LAST_NAME_TEXT, iFieldValue);
                }
                else if (iFieldName.equals("password"))
                {
                    selenium.type(Users.PASSWORD_TEXT, iFieldValue);
                    selenium.type(Users.CONFIRMED_PASSWORD_TEXT, iFieldValue);
                }
                else if (iFieldName.equals("companyname")
                        && selenium.isElementPresent(Users.COMPANY_SELECT))
                {
                    selenium.select(Users.COMPANY_SELECT, "label="
                            + iFieldValue);
                }
                else if (iFieldName.equals("nextbasic"))
                {
                    selenium.click(Users.NEXT_BUTTON);
                    if (selenium.isAlertPresent())
                    {
                        selenium.getAlert();
                        selenium.click(Users.CANCEL_BUTTON);
                        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
                        break;
                    }
                    else
                    {
                        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
                    }
                }
                else if (iFieldName.equals("email"))
                {
                    selenium.type(Users.EMAIL_TEXT, iFieldValue);
                }
                else if (iFieldName.equals("nextcontact"))
                {
                    selenium.click(Users.NEXT_BUTTON);
                    selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
                }
                else if (iFieldName.equals("calendar"))
                {
                    if (iFieldValue.equals("true"))
                    {
                        selenium.click(Users.NEXT_BUTTON);
                        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
                    }

                }
                else if (iFieldName.equals("sourcelocale"))
                {
                    sourcelocal = iFieldValue;
                }
                else if (iFieldName.equals("targetlocale"))
                {
                    targetlocal = iFieldValue;
                }
                else if (iFieldName.equals("dtp1"))
                {
                    selenium.click(Users.Dtp1Type_CHECKBOX);
                    selenium.select(Users.Dtp1Type_SELECT, iFieldValue);
                }
                else if (iFieldName.equals("translation"))
                {
                    translation = iFieldValue;
                    String[] transarray = translation.split(";");
                    String[] sourcearray = sourcelocal.split(";");
                    String[] targetarray = targetlocal.split(";");

                    for (int i = 0; i < transarray.length; i++)
                    {
                        String trans = transarray[i];
                        String source = sourcearray[i];
                        String target = targetarray[i];
                        selenium.select(Users.SOURCE_LOCALE_SELECT, source);
                        selenium.select(Users.TARGET_LOCALE_SELECT, target);
                        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
                        selenium.click(trans);

                        if (i < transarray.length - 1)
                        {
                            selenium.click(Users.LAN_ADD);
                        }
                    }
                }

                else if (iFieldName.substring(0, 9).equals("localpair"))
                {

                    addRoles(selenium, iFieldValue, "true");
                    selenium.click(Users.LAN_ADD);
                    if (selenium.isAlertPresent())
                    {
                        Assert.assertTrue(false, selenium.getAlert());
                    }
                    selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

                }

                else if (iFieldName.equals("nextroles"))
                {
                    selenium.click(Users.NEXT_BUTTON);
                    selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
                    selenium.click(Users.NEXT_BUTTON);
                    selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
                    selenium.click(Users.Next_BUTTON_LevelAccess);
                    selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
                    // selenium.click(Users.Next_BUTTON_LevelAccess);
                    // selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
                }
                else if (iFieldName.equals("available"))
                {
                    selenium.addSelection(Users.Available_SELECTION,
                            iFieldValue);
                    selenium.click(Users.Add_BUTTON);
                    selenium.click("//input[@value='Save']");
                    selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
                }

            }
            catch (Exception e)
            {
                Reporter.log(e.getMessage());
            }
        }

        if (iuserName != null)
        {
            selenium.click(MainFrame.SETUP_MENU);
            selenium.click(MainFrame.USERS_SUBMENU);
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

            selenium.type(Users.USER_SEARCH_NAME_TEXT, iuserName);
            selenium.click(Users.SEARCH_VALUE_BUTTON);
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
            Assert.assertEquals(
                    isElementPresent(selenium,
                            "//input[@name='radioBtn' and @value='" + iuserName
                                    + "']"), true);
        }
        else
        {
            Reporter.log("The user creation failed!");
        }
        return iuserName;
    }

    /*
     * Remove a user with the profiles you provided.
     */
    public void removeUsers(Selenium selenium, String username)
            throws Exception
    {

        boolean selected = selectRadioButtonFromTable(selenium, MAIN_TABLE, cm
                + username);
        if (!selected)
        {
            Reporter.log("Cannot find a proper user to remove.");
            return;
        }

        clickAndWait(selenium, Users.REMOVE_VALUE_BUTTON);

        if (selenium.isConfirmationPresent())
        {
            selenium.getConfirmation();
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        }

        selenium.type(Users.USER_SEARCH_NAME_TEXT, cm + username);
        selenium.click(Users.SEARCH_VALUE_BUTTON);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        Assert.assertEquals(
                isElementPresent(selenium,
                        "//input[@name='radioBtn' and @value='" + cm + username
                                + "']"), false);
    }

    // Create superusers
    public String newSuperUsers(Selenium selenium, String UserProfiles)
            throws Exception
    {
        String[] iUserProfiles = UserProfiles.split(",");
        String iuserName = null;

        selenium.click(Users.NEW_VALUE_BUTTON);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        String icomName = "";

        for (String iUserProfile : iUserProfiles)
        {
            String[] ivalue = iUserProfile.split("=");
            String iFieldName = ivalue[0].trim();
            String iFieldValue = ivalue[1].trim();
            try
            {
                if (iFieldName.equals("addcomnametouser"))
                {
                    if (iFieldValue.equals("true"))
                    {
                        icomName = ConfigUtil.getConfigData("company");
                    }
                }
                if (iFieldName.equals("username"))
                {
                    selenium.type(Users.NAME_TEXT, icomName
                            + iFieldValue);
                    iuserName = icomName + iFieldValue;
                }
                else if (iFieldName.equals("firstname"))
                {
                    selenium.type(Users.FIRST_NAME_TEXT, iFieldValue);
                }
                else if (iFieldName.equals("lastname"))
                {
                    selenium.type(Users.LAST_NAME_TEXT, iFieldValue);
                }
                else if (iFieldName.equals("password"))
                {
                    selenium.type(Users.PASSWORD_TEXT, iFieldValue);
                    selenium.type(Users.CONFIRMED_PASSWORD_TEXT, iFieldValue);
                }
                else if (iFieldName.equals("companyname")
                        && selenium.isElementPresent(Users.COMPANY_SELECT))
                {
                    selenium.select(Users.COMPANY_SELECT, "label="
                            + iFieldValue);
                }
                else if (iFieldName.equals("nextbasic"))
                {
                    selenium.click(Users.NEXT_BUTTON);
                    if (selenium.isAlertPresent())
                    {
                        Assert.assertEquals(
                                selenium.getAlert(),
                                "The username you have entered is already in use by a"
                                        + "nother User, the user may be active or deactiv"
                                        + "iated.  Please try a different username.");
                        selenium.click(Users.CANCEL_BUTTON);
                        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
                        break;
                    }
                    else
                    {
                        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
                    }
                }
                else if (iFieldName.equals("email"))
                {
                    selenium.type(Users.EMAIL_TEXT, iFieldValue);
                }
                else if (iFieldName.equals("nextcontact"))
                {
                    selenium.click(Users.NEXT_BUTTON);
                    selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
                }
                else if (iFieldName.equals("calendar"))
                {
                    if (iFieldValue.equals("true"))
                    {
                        selenium.click(Users.NEXT_BUTTON);
                        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
                    }

                }
                else if (iFieldName.equals("nextroles"))
                {
                    selenium.click(Users.NEXT_BUTTON);
                    selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
                }
                else if (iFieldName.equals("project"))
                {
                    selenium.addSelection(Users.Project_Select_Table_Name,
                            "label=" + iFieldValue);
                    selenium.click(Users.Add_BUTTON);
                    selenium.click(Users.NEXT_BUTTON);
                    selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
                    selenium.click(Users.Next_BUTTON_LevelAccess);
                    selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
                }
                else if (iFieldName.equals("available"))
                {
                    selenium.select(Users.Available_SELECTION_Permission,
                            "label=" + iFieldValue);
                    selenium.click(Users.SAVE_VALUE_BUTTON);
                    selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
                }
            }
            catch (Exception e)
            {
                Reporter.log(e.getMessage());
            }
        }
        if (iuserName != null)
        {
            selenium.click(MainFrame.SETUP_MENU);
            selenium.click(MainFrame.USERS_SUBMENU);
            selenium.type(Users.USER_SEARCH_NAME_TEXT, iuserName);
            selenium.click(Users.SEARCH_VALUE_BUTTON);
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
            Assert.assertEquals(
                    isElementPresent(selenium,
                            "//input[@name='radioBtn' and @value='" + iuserName
                                    + "']"), true);
        }
        else
        {
            Reporter.log("The user creation failed!");
        }
        return iuserName;
    }

    /**
     * 
     * Edit default roles for super users
     * 
     * @param
     * @return
     * @throws Exception
     */
    public String editDefaultRoles(Selenium selenium, String iDefaulRole,
            String Sourcelocale, String Targetlocale) throws Exception
    {

        boolean result = basicfuncs.selectRadioButtonFromTable(selenium,
                Users.USER_LIST_TABLE, iDefaulRole);
        if (result)
        {
            selenium.click(Users.EDIT_BUTTON);
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

            selenium.click(Users.DEFAULT_ROLES_VALUE_BUTTON);
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
            
            selenium.click(Users.NEW_BUTTON);
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
    
            selenium.select(Users.SOURCE_LOCALE_SELECT, "label=" + Sourcelocale);
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
            
            selenium.addSelection(Users.TARGET_LOCALE_SELECT, "label="
                    + Targetlocale);
            selenium.click(Users.Activities_Types1);
            selenium.click(Users.Activities_Types2);
            selenium.click(Users.DONE_VALUE_BUTTON);
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
            selenium.click(Users.DONE_BUTTON);
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
            selenium.click(Users.SAVE_BUTTON);
        }
        
        return iDefaulRole;
    }

    /**
     * Verify default roles have been added to roles
     * 
     */
    public Boolean verifyRoles(Selenium selenium, String rolename,
            String Sourcelocale, String Targetlocale) throws Exception
    {
        boolean result = basicfuncs.selectRadioButtonFromTable(selenium,
                Users.USER_LIST_TABLE, rolename);
        if (result)
        {
            selenium.click(Users.EDIT_BUTTON);
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

            selenium.click(Users.ROLES_BUTTON);
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
            result = basicfuncs.selectRadioForRemove(selenium,
                    Users.USER_ROLES_TABLE, Sourcelocale, Targetlocale);
            if (result)
            {
                Reporter.log("The default roles has been added to the roles");
            }
        }
        return result;
    }

    public String searchUsers(Selenium selenium, String UserProfiles)
            throws Exception
    {
        String[] iUserProfiles = UserProfiles.split(",");
        String iFieldValue = null;
        for (String iUserProfile : iUserProfiles)
        {
            String[] ivalue = iUserProfile.split("=");
            iFieldValue = ivalue[1].trim();
            String iFieldName = ivalue[0].trim();
            if (iFieldName.equals("username"))
            {
                selenium.type(Users.USER_SEARCH_NAME_TEXT, iFieldValue);
                selenium.click(Users.SEARCH_VALUE_BUTTON);
                selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
                Assert.assertEquals(
                        isElementPresent(selenium,
                                "//input[@name='radioBtn' and @value='"
                                        + iFieldValue + "']"), true);
                break;
            }
        }
        return iFieldValue;
    }

    // Edit user's Basic Information
    public void editUserBasicInfo(Selenium selenium, String UserProfiles)
            throws Exception
    {
        String[] iUserProfiles = UserProfiles.split(",");

        for (String iUserProfile : iUserProfiles)
        {
            String[] ivalue = iUserProfile.split("=");
            String iFieldName = ivalue[0].trim();
            String iFieldValue = ivalue[1].trim();

            if (iFieldName.equals("username"))
            {
                searchUsers(selenium, iFieldName + "=" + cm + iFieldValue);
                boolean selected = selectRadioButtonFromTable(selenium,
                        Users.USER_LIST_TABLE, cm + iFieldValue);
                if (!selected)
                {
                    Assert.assertTrue(false,
                            "Cannot find a proper user to edit.");
                }
                clickAndWait(selenium, Users.EDIT_BUTTON);
            }

            else if (iFieldName.equals("firstname"))
            {
                selenium.type(Users.FIRST_NAME_TEXT, iFieldValue);
            }
            else if (iFieldName.equals("lastname"))
            {
                selenium.type(Users.LAST_NAME_TEXT, iFieldValue);
            }
            else if (iFieldName.equals("password"))
            {
                selenium.type(Users.PASSWORD_TEXT, iFieldValue);
                selenium.type(Users.CONFIRMED_PASSWORD_TEXT, iFieldValue);
            }
            else if (iFieldName.equals("title"))
            {
                selenium.type(Users.TITLE_TEXT, iFieldValue);

            }

        }
        selenium.click(Users.SAVE_VALUE_BUTTON);

        try
        {
            if (selenium.isAlertPresent())
            {
                Assert.assertTrue(false, selenium.getAlert());
            }
        }
        catch (Exception e)
        {

            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        }

        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
    }

    public void editUserContactInfo(Selenium selenium, String UserProfiles)
            throws Exception
    {
        String[] iUserProfiles = UserProfiles.split(",");

        for (String iUserProfile : iUserProfiles)
        {
            String[] ivalue = iUserProfile.split("=");
            String iFieldName = ivalue[0].trim();
            String iFieldValue = ivalue[1].trim();

            if (iFieldName.equals("username"))
            {
                searchUsers(selenium, iFieldName + "=" + cm + iFieldValue);
                boolean selected = selectRadioButtonFromTable(selenium,
                        Users.USER_LIST_TABLE, cm + iFieldValue);
                if (!selected)
                {
                    Assert.assertTrue(false,
                            "Cannot find a proper user to edit.");
                }
                clickAndWait(selenium, Users.EDIT_BUTTON);
                clickAndWait(selenium, Users.Edit_ContactInfo_BUTTON);
            }
            else if (iFieldName.equals("address"))
            {
                selenium.type(Users.ADDRESS_TEXT, iFieldValue);
            }
            else if (iFieldName.equals("homePhone"))
            {
                selenium.type(Users.HOMEPHONE_TEXT, iFieldValue);
            }
            else if (iFieldName.equals("workPhone"))
            {
                selenium.type(Users.WORKPHONE_TEXT, iFieldValue);
            }
            else if (iFieldName.equals("cellPhone"))
            {
                selenium.type(Users.CELLPHONE_TEXT, iFieldValue);
            }
            else if (iFieldName.equals("email"))
            {
                selenium.type(Users.EMAIL_TEXT, iFieldValue);
            }

        }
        selenium.click(Users.DONE_VALUE_BUTTON);

        try
        {
            if (selenium.isAlertPresent())
            {
                Assert.assertTrue(false, selenium.getAlert());
            }
        }
        catch (Exception e)
        {

            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        }
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        selenium.click(Users.SAVE_VALUE_BUTTON);
        try
        {
            if (selenium.isAlertPresent())
            {
                Assert.assertTrue(false, selenium.getAlert());
            }
        }
        catch (Exception e)
        {

            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        }
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

    }

    public void editUserProjects(Selenium selenium, String UserProfiles)
            throws Exception
    {
        String[] iUserProfiles = UserProfiles.split(",");

        for (String iUserProfile : iUserProfiles)
        {
            String[] ivalue = iUserProfile.split("=");
            String iFieldName = ivalue[0].trim();
            String iFieldValue = ivalue[1].trim();

            if (iFieldName.equals("username"))
            {
                searchUsers(selenium, iFieldName + "=" + cm + iFieldValue);
                boolean selected = selectRadioButtonFromTable(selenium,
                        Users.USER_LIST_TABLE, cm + iFieldValue);
                if (!selected)
                {
                    Assert.assertTrue(false,
                            "Cannot find a proper user to edit.");
                }
                clickAndWait(selenium, Users.EDIT_BUTTON);
                clickAndWait(selenium, Users.Edit_Project_BUTTON);
            }
            else if (iFieldName.equals("allprojects"))
            {
                if (iFieldValue.equals("true"))
                {
                    if (!(selenium.isChecked(Users.AllProject_CHECKBOX)))
                    {
                        selenium.click(Users.AllProject_CHECKBOX);
                    }
                }
                else if (iFieldValue.equals("false"))
                {
                    if (selenium.isChecked(Users.AllProject_CHECKBOX))
                    {
                        selenium.click(Users.AllProject_CHECKBOX);
                    }
                }

            }

        }
        selenium.click(Users.DONE_VALUE_BUTTON);
        if (selenium.isAlertPresent())
        {
            Assert.assertTrue(false, selenium.getAlert());
        }
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        selenium.click(Users.SAVE_VALUE_BUTTON);

        try
        {
            if (selenium.isAlertPresent())
            {
                Assert.assertTrue(false, selenium.getAlert());
            }
        }
        catch (Exception e)
        {

            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        }

        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
    }

    public void editUserPermissions(Selenium selenium, String UserProfiles)
            throws Exception
    {
        String[] iUserProfiles = UserProfiles.split(",");
        String[] permision =
        { "Administrator", "Customer", "LocaleManager",
                "LocalizationParticipant", "ProjectManager", "VendorAdmin",
                "VendorManager", "VendorViewer", "WorkflowManager" };

        for (String iUserProfile : iUserProfiles)
        {
            String[] ivalue = iUserProfile.split("=");
            String iFieldName = ivalue[0].trim();
            String iFieldValue = ivalue[1].trim();

            if (iFieldName.equals("username"))
            {
                searchUsers(selenium, iFieldName + "=" + cm + iFieldValue);
                boolean selected = selectRadioButtonFromTable(selenium,
                        Users.USER_LIST_TABLE, cm + iFieldValue);
                if (!selected)
                {
                    Assert.assertTrue(false,
                            "Cannot find a proper user to edit.");
                }
                clickAndWait(selenium, Users.EDIT_BUTTON);
                clickAndWait(selenium, Users.Edit_Permissions_BUTTON);
            }
            else if (iFieldName.equals("edit"))
            {
                for (String s : permision)
                {
                    if (selenium.getText("to").contains(s))
                    {
                        selenium.addSelection(
                                Users.Available_SELECTION_Permission, s);
                        selenium.click(Users.Permission_Remove_BUTTON);
                    }
                }
                String[] iUserProfiles1 = iFieldValue.split("/");

                for (String iUserProfile1 : iUserProfiles1)
                {
                    selenium.addSelection(Users.Available_SELECTION,
                            iUserProfile1);
                    selenium.click(Users.Add_BUTTON);
                }
            }
            else if (iFieldName.equals("add"))
            {
                String[] iUserProfiles1 = iFieldValue.split("/");

                for (String iUserProfile1 : iUserProfiles1)
                {
                    selenium.addSelection(Users.Available_SELECTION,
                            iUserProfile1);
                    selenium.click(Users.Add_BUTTON);
                }
            }
            else if (iFieldName.equals("remove"))
            {
                String[] iUserProfiles1 = iFieldValue.split("/");

                for (String iUserProfile1 : iUserProfiles1)
                {
                    selenium.addSelection(Users.Available_SELECTION_Permission,
                            iUserProfile1);
                    selenium.click(Users.Permission_Remove_BUTTON);
                }

            }
        }
        selenium.click(Users.DONE_VALUE_BUTTON);
        if (selenium.isAlertPresent())
        {
            Assert.assertTrue(false, selenium.getAlert());
        }
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        selenium.click(Users.SAVE_VALUE_BUTTON);
        try
        {
            if (selenium.isAlertPresent())
            {
                Assert.assertTrue(false, selenium.getAlert());
            }
        }
        catch (Exception e)
        {

            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        }

        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

    }

    // Add User New Roles
    public void editUserRoles(Selenium selenium, String UserProfiles)
            throws Exception
    {
        String[] iUserProfiles = UserProfiles.split(",");
        String newSourcelocal = null;

        for (String iUserProfile : iUserProfiles)
        {
            String[] ivalue = iUserProfile.split("=");
            String iFieldName = ivalue[0].trim();
            String iFieldValue = ivalue[1].trim();
            String comID = "";

            if (iFieldName.equals("username"))
            {
                searchUsers(selenium, iFieldName + "=" + cm + iFieldValue);
                boolean selected = selectRadioButtonFromTable(selenium,
                        Users.USER_LIST_TABLE, cm + iFieldValue);
                if (!selected)
                {
                    Assert.assertTrue(false,
                            "Cannot find a proper user to edit.");
                }
                clickAndWait(selenium, Users.EDIT_BUTTON);
                clickAndWait(selenium, Users.Edit_Roles_BUTTON);

            }
            else if (iFieldName.equals("editsourcelocale"))
            {
                newSourcelocal = iFieldValue;
            }
            else if (iFieldName.equals("edittargetlocale"))
            {
                boolean selected = selectRadioForRemove(selenium,
                        Users.Edit_Role_LocalPairs_TABLE, newSourcelocal,
                        iFieldValue);
                if (!selected)
                {
                    Assert.assertTrue(false,
                            "Cannot find a proper user to edit.");
                }
                clickAndWait(selenium, Users.EDIT_VALUE_BUTTON);

            }
            else if (iFieldName.equals("edit"))
            {
                comID = companyID(selenium, Users.ROLES_TABLE, 1);
                int i = 3;
                while (selenium.isElementPresent(Users.Role_TABLE_Activity
                        + "//tr[" + i + "]"))
                {
                    if (selenium.isElementPresent(Users.Role_TABLE_Activity
                            + "//tr[" + i + "]//td[1]"))
                    {
                        String ActivityName = selenium
                                .getAttribute(Users.Role_TABLE_Activity
                                        + "//tr[" + i + "]//td[1]/input/@name");
                        if (selenium.isChecked(ActivityName))
                        {
                            selenium.uncheck(ActivityName);
                        }

                    }
                    i++;
                }
                String[] iUserProfiles1 = iFieldValue.split("/");

                for (String iUserProfile1 : iUserProfiles1)
                {
                    String iFieldName0 = iUserProfile1.trim();
                    selenium.click(iFieldName0 + "_" + comID + "Cost");
                }
            }
            else if (iFieldName.equals("add"))
            {
                comID = companyID(selenium, Users.ROLES_TABLE, 1);
                String[] iUserProfiles1 = iFieldValue.split("/");
                for (String iUserProfile1 : iUserProfiles1)
                {
                    if (!(selenium.isChecked(iUserProfile1 + "_" + comID
                            + "Cost")))
                    {
                        selenium.click(iUserProfile1 + "_" + comID + "Cost");
                    }
                }
            }
            else if (iFieldName.equals("remove"))
            {
                comID = companyID(selenium, Users.ROLES_TABLE, 1);
                String[] iUserProfiles1 = iFieldValue.split("/");
                for (String iUserProfile1 : iUserProfiles1)
                {
                    if (selenium
                            .isChecked(iUserProfile1 + "_" + comID + "Cost"))
                    {
                        selenium.click(iUserProfile1 + "_" + comID + "Cost");
                    }
                }
            }

        }
        selenium.click(Users.DONE_VALUE_BUTTON);
        if (selenium.isAlertPresent())
        {
            Assert.assertTrue(false, selenium.getAlert());
        }
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        selenium.click(Users.DONE_VALUE_BUTTON);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        selenium.click(Users.SAVE_VALUE_BUTTON);

        try
        {
            if (selenium.isAlertPresent())
            {
                Assert.assertTrue(false, selenium.getAlert());
            }
        }
        catch (Exception e)
        {

            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        }

        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
    }

    /*
     * Edit User Edit New role
     */
    public void editAddUserRoles(Selenium selenium, String UserProfiles)
            throws Exception
    {
        String[] iUserProfiles = UserProfiles.split(",");

        for (String iUserProfile : iUserProfiles)
        {
            String[] ivalue = iUserProfile.split("=");
            String iFieldName = ivalue[0].trim();
            String iFieldValue = ivalue[1].trim();

            if (iFieldName.equals("username"))
            {
                searchUsers(selenium, iFieldName + "=" + cm + iFieldValue);
                boolean selected = selectRadioButtonFromTable(selenium,
                        Users.USER_LIST_TABLE, cm + iFieldValue);
                if (!selected)
                {
                    Assert.assertTrue(false,
                            "Cannot find a proper user to edit.");
                }
                clickAndWait(selenium, Users.EDIT_BUTTON);
                clickAndWait(selenium, Users.Edit_Roles_BUTTON);

            }
            else if (iFieldName.substring(0, 9).equals("localpair"))
            {

                clickAndWait(selenium, Users.NEW_VALUE_BUTTON);

                addRoles(selenium, iFieldValue, "false");

                selenium.click(Users.DONE_VALUE_BUTTON);
                if (selenium.isAlertPresent())
                {
                    Assert.assertTrue(false, selenium.getAlert());
                }
                selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

            }

        }
        selenium.click(Users.DONE_VALUE_BUTTON);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        selenium.click(Users.SAVE_VALUE_BUTTON);

        try
        {
            if (selenium.isAlertPresent())
            {
                Assert.assertTrue(false, selenium.getAlert());
            }
        }
        catch (Exception e)
        {

            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        }

        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
    }
}
