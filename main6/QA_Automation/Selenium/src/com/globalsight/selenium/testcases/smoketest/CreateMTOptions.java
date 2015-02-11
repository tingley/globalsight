package com.globalsight.selenium.testcases.smoketest;

import org.testng.annotations.Test;
import com.globalsight.selenium.functions.TMProfileFuncs;
import com.globalsight.selenium.pages.AsiaOnlineMT;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.BaseTestCase;

public class CreateMTOptions extends BaseTestCase
{

    /*
     * Common Variables
     */
    TMProfileFuncs tmProfileFuncs = new TMProfileFuncs();

    /*
     * Create a new TM.
     */
    @Test
    public void createMTOptions() throws Exception
    {
        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.TRANSLATION_MEMORY_PROFILES_SUBMENU);

        String mtOption = getProperty("mt.option");

        AsiaOnlineMT ao = new AsiaOnlineMT();
        ao.setHost(getProperty("mt.ao.host"));
        ao.setPort(getProperty("mt.ao.port"));
        ao.setUsername(getProperty("mt.ao.username"));
        ao.setPassword(getProperty("mt.ao.password"));
        ao.setAccountNumber(getProperty("mt.ao.accountNumber"));
        ao.setDomainCombination(getProperty("mt.ao.domainCombination"));

        tmProfileFuncs.createMTOptions(selenium, mtOption, ao);
    }
}
