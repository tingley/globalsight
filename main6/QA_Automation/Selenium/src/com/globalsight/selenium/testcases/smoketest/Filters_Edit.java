package com.globalsight.selenium.testcases.smoketest;

import org.testng.annotations.Test;
import com.globalsight.selenium.functions.FilterConfigurationFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.BaseTestCase;

public class Filters_Edit extends BaseTestCase
{
    /*
     * Common variables initialization.
     */
    private FilterConfigurationFuncs filterConfigurationFuncs = new FilterConfigurationFuncs();

    @Test
    public void EditFilters() throws Exception
    {

        openMenuItemAndWait(selenium, MainFrame.DATA_SOURCES_MENU,
                MainFrame.FILTER_CONFIGURATION_SUBMENU);

        filterConfigurationFuncs
                .editFilters(selenium, getDataInCase("filters"));
    }
}
