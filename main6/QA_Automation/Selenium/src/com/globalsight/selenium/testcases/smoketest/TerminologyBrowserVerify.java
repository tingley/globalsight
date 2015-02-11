package com.globalsight.selenium.testcases.smoketest;

import junit.framework.Assert;

import org.testng.annotations.Test;
import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.TerminologyFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.TerminologyElements;
import com.globalsight.selenium.testcases.BaseTestCase;

/*
 * TestCaseName: TerminologyBrowserVerify.java
 * Author:Jester
 * Tests:verifyTerminologyBrowser()
 * 
 * History:
 * Date       Comments       Updater
 * 2011-6-30  First Version  Jester
 */
public class TerminologyBrowserVerify extends BaseTestCase
{

    /*
     * Common Variables.
     */
    TerminologyFuncs terminologyFuncs = new TerminologyFuncs();

    @Test
    public void verifyTerminologyBrowser() throws Exception
    {

        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.TERMINOLOGY_SUBMENU);

        terminologyFuncs.selectRadioButtonFromTable(selenium,
                TerminologyElements.MAIN_TABLE, getProperty("tb.name"));

        selenium.click(TerminologyElements.MAIN_BROWSER_BUTTON);
        selenium.waitForPopUp(TerminologyElements.TermbaseViewer_TAG,
                CommonFuncs.SHORT_WAIT);

        selenium.selectWindow("name=" + TerminologyElements.TermbaseViewer_TAG);
        selenium.select(TerminologyElements.Source_SELECT,
                getProperty("tb.browser.sourceLocale"));
        selenium.select(TerminologyElements.Target_SELECT,
                getProperty("tb.browser.targetLocale"));
        selenium.type(TerminologyElements.Query_TEXT_FIELD,
                getProperty("tb.browser.search"));
        selenium.select(TerminologyElements.SearchType_SELECT,
                getProperty("tb.browser.search.type"));
        selenium.click(TerminologyElements.EXECUTE_BUTTON);

        Thread.sleep(100);

        selenium.click(TerminologyElements.TermsFound_LIST + "/li/span");
        Assert.assertTrue(selenium.getText(
                TerminologyElements.TermDetails_TEXT_FIELD).contains(
                getProperty("tb.browser.verify")));
        selenium.click(TerminologyElements.CloseTermbase_IMG);
        selenium.selectWindow(null);
    }
}
