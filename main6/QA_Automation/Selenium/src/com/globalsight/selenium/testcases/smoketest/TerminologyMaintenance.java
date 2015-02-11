package com.globalsight.selenium.testcases.smoketest;

//author : ShenYang  2011-07-04

import org.testng.annotations.Test;
import com.globalsight.selenium.functions.TerminologyFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.BaseTestCase;

public class TerminologyMaintenance extends BaseTestCase
{
    private TerminologyFuncs terminologyFuncs = new TerminologyFuncs();

    @Test
    public void tbMaintain() throws Exception
    {
        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.TERMINOLOGY_SUBMENU);

        String iTBName = getProperty("tb.name");
        String fieldName = getProperty("tb.maintenance.field");
        String searchStr = getProperty("tb.maintenance.search");
        String newStr = getProperty("tb.maintenance.newString");
        terminologyFuncs.maintenance(selenium, iTBName, fieldName, searchStr,
                newStr);
    }
}
