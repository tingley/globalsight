package com.globalsight.selenium.testcases.smoketest;

import org.testng.annotations.Test;

import com.globalsight.selenium.functions.TerminologyFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.BaseTestCase;

public class TerminologyEdit extends BaseTestCase
{
    private TerminologyFuncs terminologyFuncs = new TerminologyFuncs();

    @Test
    public void editTermbase() throws Exception
    {
        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.TERMINOLOGY_SUBMENU);

        String editProfile = getProperty("tb.edit");
        terminologyFuncs.edit(selenium, editProfile);
    }
}
