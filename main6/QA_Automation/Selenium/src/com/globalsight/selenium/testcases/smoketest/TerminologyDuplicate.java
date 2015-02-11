package com.globalsight.selenium.testcases.smoketest;

import org.testng.annotations.Test;

import com.globalsight.selenium.functions.TerminologyFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.BaseTestCase;

public class TerminologyDuplicate extends BaseTestCase
{
    private TerminologyFuncs terminologyFuncs = new TerminologyFuncs();

    @Test
    public void TBDup() throws Exception
    {
        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.TERMINOLOGY_SUBMENU);

        String tbName = getProperty("tb.name");
        String dupName = getProperty("tb.duplicate.name");
        terminologyFuncs.duplicate(selenium, tbName, dupName);
    }
}
