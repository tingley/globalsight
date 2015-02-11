package com.globalsight.selenium.testcases.smoketest;

import org.testng.annotations.Test;

import com.globalsight.selenium.functions.TerminologyFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.BaseTestCase;

public class TerminologyRemove extends BaseTestCase
{

    private TerminologyFuncs terminologyFuncs = new TerminologyFuncs();

    @Test
    public void TermbaseRemove() throws Exception
    {
        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.TERMINOLOGY_SUBMENU);

        String removeTermbaseName = getProperty("tb.edit.newName");
        terminologyFuncs.remove(selenium, removeTermbaseName);
    }
}
