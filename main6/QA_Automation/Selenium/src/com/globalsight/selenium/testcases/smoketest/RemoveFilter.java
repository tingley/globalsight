package com.globalsight.selenium.testcases.smoketest;

import junit.framework.Assert;

import org.testng.annotations.Test;

import com.globalsight.selenium.pages.FilterConfiguration;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.BaseTestCase;

public class RemoveFilter extends BaseTestCase
{
    @Test
    public void removefilter() throws Exception
    {
        openMenuItemAndWait(selenium, MainFrame.DATA_SOURCES_MENU,
                MainFrame.FILTER_CONFIGURATION_SUBMENU);

        selenium.click(FilterConfiguration.EXPAND_ALL_BUTTON);
        selenium.uncheck(FilterConfiguration.CHECK_ALL_CHECKBOX);
        selenium.click(FilterConfiguration.CHECK_ALL_CHECKBOX);

        selenium.click(FilterConfiguration.REMOVE_VALUE_BUTTON);

        Assert.assertTrue(selenium
                .getConfirmation()
                .matches(
                        "^By clicking the 'OK' button, this filter will be deleted forever\\. Are you sure to continue[\\s\\S]$"));
        Assert.assertEquals(selenium.getAlert(),
                "The filter has been deleted sucessfully.");
    }
}
