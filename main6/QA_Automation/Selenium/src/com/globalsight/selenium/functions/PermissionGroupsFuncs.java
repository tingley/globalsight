package com.globalsight.selenium.functions;

/*
 * FileName: PermissionGroupsFuncs.java
 * Author:Jester
 * Methods:PermissionGroupsEdit()  
 * 
 * History:
 * Date       Comments       Updater
 * 2011-6-8  First Version  Jester
 */

import com.globalsight.selenium.pages.PermissionGroups;
import com.thoughtworks.selenium.Selenium;

public class PermissionGroupsFuncs extends BasicFuncs
{
    /*
     * Edit the Permission Groups.
     */

    public void editPermissionGroups(Selenium selenium, String GroupName,
            String Permissionprofiles) throws Exception
    {
        selectRadioButtonFromTable(selenium,
                PermissionGroups.PermissionGroups_TABLE, GroupName);
        selenium.click(PermissionGroups.Edit_BUTTON);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        selenium.click(PermissionGroups.Permissions_BUTTON);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        if (Permissionprofiles.equals("data sources"))
        {
            selenium.click(PermissionGroups.DataSources_CHECKBOX);
            selenium.click(PermissionGroups.DataSources_CHECKBOX);
        }

        if (Permissionprofiles.equals("setup"))
        {
            selenium.click(PermissionGroups.SetUp_CHECKBOX);
            selenium.click(PermissionGroups.SetUp_CHECKBOX);
        }

        if (Permissionprofiles.equals("all permission"))
        {
            selenium.click(PermissionGroups.ALLPERMISSION);
            selenium.click(PermissionGroups.ALLPERMISSION);
        }

        selenium.click(PermissionGroups.Done_BUTTON);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        selenium.click(PermissionGroups.Save_BUTTON);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
    }

}
