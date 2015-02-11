package com.globalsight.selenium.testcases.smoketest;

import org.testng.annotations.Test;

import com.globalsight.selenium.functions.TerminologyFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.BaseTestCase;

public class TerminologyExport extends BaseTestCase
{
    TerminologyFuncs terminologyFuncs = new TerminologyFuncs();

    @Test
    public void export() throws Exception
    {
        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.TERMINOLOGY_SUBMENU);

        String exportTermbaseName = getProperty("tb.export");
        if (exportTermbaseName != null && exportTermbaseName.length() != 0)
        {
            String[] nameArray = exportTermbaseName.split("\\|");
            for (String name : nameArray)
            {
                terminologyFuncs.export(selenium, name);
            }
        }
    }
}
