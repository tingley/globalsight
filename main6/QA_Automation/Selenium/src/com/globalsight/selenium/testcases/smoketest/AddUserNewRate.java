package com.globalsight.selenium.testcases.smoketest;

import org.testng.annotations.Test;

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.RatesFuncs;
import com.globalsight.selenium.functions.UsersFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.Users;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.globalsight.selenium.testcases.BaseTestCase;

public class AddUserNewRate extends BaseTestCase
{
    private RatesFuncs rateFuncs = new RatesFuncs();
    private UsersFuncs userFuncs = new UsersFuncs();

    @Test
    public void aUserRate() throws Exception
    {
        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.USERS_SUBMENU);

        String src = getProperty("userRate.sourceLocale");
        String tar = getProperty("userRate.targetLocale");
        String iUserName = ConfigUtil.getConfigData("adminName");
        rateFuncs.addUserNewRate(selenium, iUserName);
        selenium.select(Users.SrcLocale_SELECT, src);
        selenium.addSelection(Users.TarLocale_SELECT, tar);

        // Use this statement to implement lost focus.
        selenium.fireEvent(Users.TarLocale_SELECT, "blur");

        String newRateName = getProperty("rate.newRateName");

        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        rateFuncs.selectRadioButtonFromTable(selenium, Users.ROLES_TABLE,
                "Dtp1", 1);
        String comid = userFuncs.companyID(selenium, Users.ROLES_TABLE, 1);
        selenium.select("Dtp1_" + comid + "_expense","label=" + newRateName);
        clickAndWait(selenium, Users.DONE_VALUE_BUTTON);
        clickAndWait(selenium, Users.DONE_VALUE_BUTTON);
        clickAndWait(selenium, Users.SAVE_BUTTON);
    }
}
