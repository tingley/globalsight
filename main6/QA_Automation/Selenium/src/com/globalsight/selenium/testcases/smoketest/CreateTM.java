package com.globalsight.selenium.testcases.smoketest;

/*
 * TestCaseName: CreateTM.java
 * Author:Jester
 * Tests:Create_TM()
 * 
 * History:
 * Date       Comments       Updater
 * 2011-6-1  First Version  Jester
 */

import org.testng.annotations.Test;
import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.TMFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.BaseTestCase;

public class CreateTM extends BaseTestCase
{

    /*
     * Common Variables
     */
    TMFuncs tmFuncs = new TMFuncs();

    /*
     * Create a new TM.
     */
    @Test
    public void createandimportTM() throws Exception
    {
        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.TRANSLATION_MEMORY_SUBMENU);

        tmFuncs.newTM(selenium, getProperty("tm.TM1"));
        tmFuncs.newTM(selenium, getProperty("tm.TM2"));
        tmFuncs.importTM(selenium, getProperty("tm.import"));

        Thread.sleep(Long.parseLong(CommonFuncs.SHORT_WAIT));
    }
}
