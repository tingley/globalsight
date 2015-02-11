package com.globalsight.selenium.testcases.smoketest;

/*
 * TestCaseName: CreateFilters.java
 * Author:Jester
 * Tests:Create_Filters()
 * 
 * History:
 * Date       Comments       Updater
 * 2011-5-31  First Version  Jester
 */

import org.testng.annotations.Test;
import com.globalsight.selenium.functions.FilterConfigurationFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.BaseTestCase;

public class CreateFilters extends BaseTestCase
{
    /*
     * Common variables initialization.
     */
    private FilterConfigurationFuncs filterConfigurationFuncs = new FilterConfigurationFuncs();

    // Create the filters with the filters be provided.
    @Test
    public void createFilters() throws Exception
    {

        openMenuItemAndWait(selenium, MainFrame.DATA_SOURCES_MENU,
                MainFrame.FILTER_CONFIGURATION_SUBMENU);

        filterConfigurationFuncs.newFilters(selenium, getProperty("filter.filters"));
    }
}
